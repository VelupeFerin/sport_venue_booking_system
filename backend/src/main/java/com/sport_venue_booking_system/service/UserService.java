package com.sport_venue_booking_system.service;

import com.sport_venue_booking_system.dto.LoginRequest;
import com.sport_venue_booking_system.dto.LoginResponse;
import com.sport_venue_booking_system.dto.RegisterRequest;
import com.sport_venue_booking_system.dto.UserUpdateRequest;
import com.sport_venue_booking_system.entity.User;
import com.sport_venue_booking_system.repository.UserRepository;
import com.sport_venue_booking_system.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.regex.Pattern;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * Spring Security 用户认证方法
     * 根据用户名加载用户信息，用于JWT认证
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        String role = user.getIsAdmin() ? "ROLE_ADMIN" : "ROLE_USER";
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    public String register(RegisterRequest request) {
        // 验证用户名
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (request.getUsername().length() < 3 || request.getUsername().length() > 20) {
            return "用户名长度必须在3-20个字符之间";
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return "用户名已存在";
        }

        // 验证密码
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return "密码不能为空";
        }
        if (request.getPassword().length() < 6) {
            return "密码长度不能少于6位";
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "两次输入的密码不一致";
        }

        // 验证手机号
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return "手机号不能为空";
        }
        if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            return "手机号格式不正确";
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setIsAdmin(false);

        userRepository.save(user);
        return null; // 返回null表示注册成功
    }

    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return new LoginResponse(token, user.getUsername(), user.getIsAdmin());
    }

    public String updateUserInfo(String currentUsername, UserUpdateRequest request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElse(null);
        
        if (user == null) {
            return "用户不存在";
        }

        boolean usernameChanged = false;
        boolean hasChanges = false;

        // 验证用户名
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (request.getUsername().length() < 3 || request.getUsername().length() > 20) {
                return "用户名长度必须在3-20个字符之间";
            }
            if (!request.getUsername().equals(currentUsername) && 
                userRepository.existsByUsername(request.getUsername())) {
                return "用户名已存在";
            }
            if (!request.getUsername().equals(currentUsername)) {
                usernameChanged = true;
                hasChanges = true;
                user.setUsername(request.getUsername());
            }
        }

        // 验证密码
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            if (request.getNewPassword().length() < 6) {
                return "新密码长度不能少于6位";
            }
            if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                return "请输入旧密码";
            }
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return "旧密码错误";
            }
            hasChanges = true;
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // 验证手机号
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                return "手机号格式不正确";
            }
            // 只有当手机号真正改变时才更新
            if (!request.getPhone().equals(user.getPhone())) {
                hasChanges = true;
                user.setPhone(request.getPhone());
            }
        }

        // 只有当有实际变化时才保存到数据库
        if (hasChanges) {
            userRepository.save(user);
        }
        
        // 如果用户名发生变化，返回新的JWT token
        if (usernameChanged) {
            String newToken = jwtUtil.generateToken(user.getUsername());
            return "NEW_TOKEN:" + newToken; // 特殊标识，表示需要更新token
        }
        
        return null; // 返回null表示更新成功
    }

    public User getUserInfo(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }
} 