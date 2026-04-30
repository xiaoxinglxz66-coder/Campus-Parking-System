import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/login',
      name: 'login-old',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    // 新增用户指南路由 - 无需登录即可访问
    {
      path: '/guide',
      name: 'guide',
      component: () => import('../views/GuestGuide.vue.vue'),
      meta: {
        requiresAuth: false,
        title: '用户指南'
      }
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/test',
      name: 'test',
      component: () => import('../views/TestView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/parking',
      name: 'parking',
      component: () => import('../views/ParkingView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UserManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/parking-spots',
      name: 'parking-spots',
      component: () => import('../views/ParkingSpotManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/vehicles',
      name: 'vehicles',
      component: () => import('../views/VehicleManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/parking-records',
      name: 'parking-records',
      component: () => import('../views/ParkingRecordManagement.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/SystemSettings.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin-dashboard',
      name: 'admin-dashboard',
      component: () => import('../views/AdminDashboard.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
        role: 'ADMIN'
      }
    },
    {
      path: '/campus-user',
      name: 'campus-user',
      component: () => import('../views/CampusUserView.vue'),
      meta: {
        requiresAuth: true,
        role: ['STUDENT', 'TEACHER', 'STAFF']
      }
    },
    {
      path: '/external-user',
      name: 'external-user',
      component: () => import('../views/ExternalUserView.vue'),
      meta: {
        requiresAuth: true,
        role: 'EXTERNAL_USER'
      }
    },
    {
      path: '/external-user/records',
      name: 'ExternalParkingRecords',
      component: () => import('../views/ExternalParkingRecords.vue'),
      meta: {
        requiresAuth: true,
        role: 'EXTERNAL_USER',
        title: '停车记录'
      }
    },
    {
      path: '/campus-user/vehicles',
      name: 'CampusUserVehicleManagement',
      component: () => import('@/views/CampusUserVehicleManagement.vue'),
      meta: {
        requiresAuth: true,
        role: ['STUDENT', 'TEACHER', 'STAFF']
      }
    },
    {
      path: '/admin/finance',
      name: 'FinanceDashboard',
      component: () => import('../views/FinanceDashboard.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
        role: 'ADMIN',
        title: '财务统计'
      }
    },
    // ✅ 修复：个人中心路由添加所有允许的角色和路由守卫
    {
      path: '/profile/:id?',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        requiresAuth: true,
        title: '个人中心',
        role: ['STUDENT', 'TEACHER', 'STAFF', 'ADMIN', 'EXTERNAL_USER']
      },
      beforeEnter: (to, from, next) => {
        const userId = localStorage.getItem('userId')
        const userRole = localStorage.getItem('role')

        console.log('🛡️ 个人中心路由守卫触发')
        console.log('路由参数ID:', to.params.id)
        console.log('当前用户ID:', userId)
        console.log('当前用户角色:', userRole)

        // 情况1：没有传递ID参数
        if (!to.params.id) {
          console.log('⚠️ 未提供用户ID参数')

          if (!userId) {
            console.error('❌ 既没有路由参数也没有本地存储的用户ID')
            alert('无法识别用户身份，请重新登录')
            next('/login')
            return
          }

          console.log('🔄 自动补充用户ID参数，重定向')
          next(`/profile/${userId}`)
          return
        }

        // 情况2：传递了ID但不是当前用户
        if (to.params.id !== userId && userRole !== 'ADMIN') {
          console.log('🚫 非管理员试图访问他人信息')
          alert('权限不足，只能查看自己的信息')

          if (userId) {
            console.log('🔄 重定向到自己的个人中心')
            next(`/profile/${userId}`)
          } else {
            next('/login')
          }
          return
        }

        // 情况3：正常访问（访问自己或管理员访问他人）
        console.log('✅ 允许访问个人中心')
        next()
      }
    },
    {
      path: '/campus-parking-records',
      name: 'CampusParkingRecords',
      component: () => import('@/views/CampusParkingRecords.vue'),
      meta: {
        requiresAuth: true,
        role: ['STUDENT', 'TEACHER', 'STAFF'],
        title: '我的停车记录'
      }
    },
    // 通配符路由放在最后
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('token')
  const userRole = localStorage.getItem('role')

  console.log('🚀 路由导航:', from.path, '->', to.path)
  console.log('🔑 登录状态:', !!isLoggedIn, '用户角色:', userRole)
  console.log('📋 目标路由meta:', to.meta)

  // 如果访问登录页且已登录，根据角色重定向
  if ((to.path === '/' || to.path === '/login') && isLoggedIn) {
    console.log('✅ 已登录用户访问登录页，重定向到对应页面')
    switch(userRole) {
      case 'ADMIN':
        next('/admin-dashboard')
        break
      case 'EXTERNAL_USER':
        next('/external-user')
        break
      default: // STUDENT, TEACHER, STAFF
        next('/campus-user')
    }
    return
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth && !isLoggedIn) {
    console.log('❌ 需要认证但未登录，重定向到登录页')
    next('/')
    return
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
    console.log('❌ 需要管理员权限但权限不足')
    if (typeof ElMessage !== 'undefined') {
      ElMessage.error('权限不足，需要管理员权限')
    } else {
      alert('权限不足，需要管理员权限')
    }

    // 根据用户角色重定向到对应首页
    switch(userRole) {
      case 'EXTERNAL_USER':
        next('/external-user')
        break
      case 'STUDENT':
      case 'TEACHER':
      case 'STAFF':
        next('/campus-user')
        break
      default:
        next('/')
    }
    return
  }

  // 统一的角色权限检查（排除个人中心，因为它有自己的守卫）
  if (to.meta.role && isLoggedIn && to.name !== 'profile') {
    const allowedRoles = Array.isArray(to.meta.role) ? to.meta.role : [to.meta.role]

    console.log('👥 角色检查: 允许的角色', allowedRoles, '当前角色', userRole)

    if (!allowedRoles.includes(userRole)) {
      console.log('❌ 角色权限不足，当前角色:', userRole, '所需角色:', to.meta.role)

      if (typeof ElMessage !== 'undefined') {
        ElMessage.error('无权访问该页面')
      } else {
        alert('无权访问该页面')
      }

      // 根据用户角色重定向到对应首页
      switch(userRole) {
        case 'ADMIN':
          next('/admin-dashboard')
          break
        case 'EXTERNAL_USER':
          next('/external-user')
          break
        case 'STUDENT':
        case 'TEACHER':
        case 'STAFF':
          next('/campus-user')
          break
        default:
          next('/')
      }
      return
    }
  }

  console.log('✅ 正常放行到:', to.path)
  next()
})

// 添加导航后日志，便于调试
router.afterEach((to, from) => {
  console.log('🏁 导航完成:', from.path, '->', to.path)
  console.log('📍 当前URL:', window.location.href)

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 校园停车管理系统`
  } else {
    document.title = '校园停车管理系统'
  }
})

export default router
