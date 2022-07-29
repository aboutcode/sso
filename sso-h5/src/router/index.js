import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/views/Home'
import Login from '@/views/Login'
import OAuthAuthorize from '@/views/OAuthAuthorize'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    }, {
      path: '/login',
      name: 'Login',
      component: Login
    }, {
      path: '/authorize',
      name: 'OAuthAuthorize',
      component: OAuthAuthorize
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    // 保存当前路由
    localStorage.setItem('preRoute', router.currentRoute.fullPath)
  }
  const token = localStorage.getItem('token')
  const isAuthenticated = !!token
  if (to.path !== '/login' && !isAuthenticated) {
    next({ path: '/login' })
  } else {
    next()
  }
})

export default router
