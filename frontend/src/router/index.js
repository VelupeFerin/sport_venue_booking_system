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
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/user') // 非管理员重定向到用户页面
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/home')
  } else {
    next()
  }
})

export default router 