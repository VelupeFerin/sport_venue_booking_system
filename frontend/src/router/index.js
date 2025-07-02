import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import HomeView from '../views/HomeView.vue'
import BookingView from '../views/booking/BookingView.vue'
import OrdersView from '../views/orders/OrdersView.vue'
import UserView from '../views/user/UserView.vue'
import AdminView from '../views/admin/AdminView.vue'
import OrderVerificationView from '../views/admin/OrderVerificationView.vue'
import SessionTemplateView from '../views/admin/SessionTemplateView.vue'
import SystemConfigView from '../views/admin/SystemConfigView.vue'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/booking',
    name: 'Booking',
    component: BookingView,
    meta: { requiresAuth: false }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: OrdersView,
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: UserView,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/verification',
    name: 'OrderVerification',
    component: OrderVerificationView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/templates',
    name: 'SessionTemplate',
    component: SessionTemplateView,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/config',
    name: 'SystemConfig',
    component: SystemConfigView,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  try {
    // 初始化用户状态（如果还没有初始化）
    if (!userStore.isInitialized) {
      await userStore.initializeUser()
    }
    
    // 如果正在加载，等待加载完成
    if (userStore.isLoading) {
      console.log('User store is loading, waiting...')
      // 等待加载完成
      while (userStore.isLoading) {
        await new Promise(resolve => setTimeout(resolve, 50))
      }
    }
    
    // 检查是否需要认证
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin) {
      if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        next('/login')
        return
      }
      
      if (!userStore.isAdmin) {
        ElMessage.error('您没有管理员权限，无法访问此页面')
        next('/user')
        return
      }
      
      // 额外验证管理员权限（确保权限状态是最新的）
      try {
        const isValid = await userStore.validateToken()
        if (!isValid) {
          ElMessage.error('登录已过期，请重新登录')
          next('/login')
          return
        }
      } catch (error) {
        console.error('Error validating admin token:', error)
        ElMessage.error('验证权限时发生错误，请重新登录')
        next('/login')
        return
      }
    }
    
    // 已登录用户访问登录/注册页面时重定向到首页
    if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
      next('/home')
      return
    }
    
    next()
  } catch (error) {
    console.error('Route guard error:', error)
    ElMessage.error('系统错误，请重新登录')
    userStore.clearUserInfo()
    next('/login')
  }
})

export default router 