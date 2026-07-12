import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ===== 公共页面 =====
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/HomeView.vue'),
    meta: { title: '爪印 - 宠物领养平台' }
  },
  {
    path: '/pets',
    name: 'PetList',
    component: () => import('@/views/pet/PetListView.vue'),
    meta: { title: '宠物列表 - 爪印' }
  },
  {
    path: '/pets/:id',
    name: 'PetDetail',
    component: () => import('@/views/pet/PetDetailView.vue'),
    meta: { title: '宠物详情 - 爪印' },
    props: true
  },
  // ===== 养宠测评 =====
  {
    path: '/assessment',
    name: 'Assessment',
    component: () => import('@/views/assessment/AssessmentView.vue'),
    meta: { title: '养宠测评 - 爪印' }
  },
  {
    path: '/assessment/result/:id',
    name: 'AssessmentResult',
    component: () => import('@/views/assessment/AssessmentResult.vue'),
    meta: { title: '测评结果 - 爪印' },
    props: true
  },
  // ===== 流浪救助 =====
  {
    path: '/rescue',
    name: 'RescueList',
    component: () => import('@/views/rescue/RescueListView.vue'),
    meta: { title: '流浪救助 - 爪印' }
  },
  {
    path: '/rescue/map',
    name: 'RescueMap',
    component: () => import('@/views/rescue/RescueMapView.vue'),
    meta: { title: '救助地图 - 爪印' }
  },
  {
    path: '/rescue/:id',
    name: 'RescueDetail',
    component: () => import('@/views/rescue/RescueDetailView.vue'),
    meta: { title: '救助详情 - 爪印' },
    props: true
  },
  {
    path: '/rescue/publish',
    name: 'RescuePublish',
    component: () => import('@/views/rescue/RescuePublish.vue'),
    meta: { title: '发布求助 - 爪印', requiresAuth: true }
  },
  {
    path: '/rescue/my',
    name: 'MyRescues',
    component: () => import('@/views/rescue/MyRescues.vue'),
    meta: { title: '我的救助 - 爪印', requiresAuth: true }
  },
  // ===== 科普内容 =====
  {
    path: '/education',
    name: 'ArticleList',
    component: () => import('@/views/education/ArticleListView.vue'),
    meta: { title: '养宠科普 - 爪印' }
  },
  {
    path: '/education/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/education/ArticleDetailView.vue'),
    meta: { title: '文章详情 - 爪印' },
    props: true
  },
  {
    path: '/education/submit',
    name: 'ArticleSubmit',
    component: () => import('@/views/education/ArticleSubmit.vue'),
    meta: { title: '投递文章 - 爪印', requiresAuth: true }
  },
  {
    path: '/education/checklist',
    name: 'Checklist',
    component: () => import('@/views/education/ChecklistView.vue'),
    meta: { title: '养前Checklist - 爪印' }
  },
  // ===== 社区互动 =====
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/community/CommunityView.vue'),
    meta: { title: '社区 - 爪印' }
  },
  {
    path: '/community/:id',
    name: 'PostDetail',
    component: () => import('@/views/community/PostDetailView.vue'),
    meta: { title: '帖子详情 - 爪印' },
    props: true
  },
  {
    path: '/community/publish',
    name: 'PostPublish',
    component: () => import('@/views/community/PostPublish.vue'),
    meta: { title: '发帖 - 爪印', requiresAuth: true }
  },
  // ===== 公益板块 =====
  {
    path: '/welfare',
    name: 'Welfare',
    component: () => import('@/views/welfare/WelfareView.vue'),
    meta: { title: '公益 - 爪印' }
  },
  {
    path: '/welfare/volunteer/apply',
    name: 'VolunteerApply',
    component: () => import('@/views/welfare/VolunteerApply.vue'),
    meta: { title: '申请志愿者 - 爪印', requiresAuth: true }
  },
  {
    path: '/welfare/volunteer/:id',
    name: 'VolunteerDetail',
    component: () => import('@/views/welfare/VolunteerDetail.vue'),
    meta: { title: '志愿者详情 - 爪印' },
    props: true
  },
  // ===== 健康档案 =====
  {
    path: '/health',
    name: 'HealthDashboard',
    component: () => import('@/views/health/HealthDashboard.vue'),
    meta: { title: '健康档案 - 爪印', requiresAuth: true }
  },
  {
    path: '/health/pets/:petId',
    name: 'HealthRecordList',
    component: () => import('@/views/health/HealthRecordList.vue'),
    meta: { title: '健康记录 - 爪印', requiresAuth: true },
    props: true
  },
  // ===== 实用工具 =====
  {
    path: '/tools/vet-map',
    name: 'VetMap',
    component: () => import('@/views/tools/VetMapView.vue'),
    meta: { title: '附近宠物医院 - 爪印' }
  },
  {
    path: '/tools/cost-calculator',
    name: 'CostCalculator',
    component: () => import('@/views/tools/CostCalculator.vue'),
    meta: { title: '养宠成本计算器 - 爪印' }
  },
  {
    path: '/tools/first-aid',
    name: 'FirstAidGuide',
    component: () => import('@/views/tools/FirstAidGuide.vue'),
    meta: { title: '急救指南 - 爪印' }
  },
  // ===== 信任体系 =====
  {
    path: '/trust/reviews/:userId',
    name: 'UserReviews',
    component: () => import('@/views/trust/UserReviews.vue'),
    meta: { title: '用户评价 - 爪印' },
    props: true
  },
  // ===== 用户 =====
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/LoginView.vue'),
    meta: { title: '登录 - 爪印', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/RegisterView.vue'),
    meta: { title: '注册 - 爪印', guest: true }
  },
  {
    path: '/user/profile',
    name: 'Profile',
    component: () => import('@/views/user/ProfileView.vue'),
    meta: { title: '个人资料 - 爪印', requiresAuth: true }
  },
  {
    path: '/user/favorites',
    name: 'MyFavorites',
    component: () => import('@/views/user/MyFavorites.vue'),
    meta: { title: '我的收藏 - 爪印', requiresAuth: true }
  },
  {
    path: '/user/applications',
    name: 'MyApplications',
    component: () => import('@/views/user/MyApplications.vue'),
    meta: { title: '我的申请 - 爪印', requiresAuth: true }
  },
  {
    path: '/user/applications/:id',
    name: 'ApplicationDetail',
    component: () => import('@/views/adoption/ApplicationDetail.vue'),
    meta: { title: '申请详情 - 爪印', requiresAuth: true },
    props: true
  },
  {
    path: '/user/reviews',
    name: 'OwnerReview',
    component: () => import('@/views/user/OwnerReview.vue'),
    meta: { title: '收到的领养申请 - 爪印', requiresAuth: true }
  },
  {
    path: '/adopt/:petId',
    name: 'AdoptionApply',
    component: () => import('@/views/adoption/AdoptionApply.vue'),
    meta: { title: '提交领养申请 - 爪印', requiresAuth: true },
    props: true
  },
  {
    path: '/pets/publish',
    name: 'PetPublish',
    component: () => import('@/views/pet/PetPublish.vue'),
    meta: { title: '发布领养信息 - 爪印', requiresAuth: true }
  },
  {
    path: '/pets/edit/:id',
    name: 'PetEdit',
    component: () => import('@/views/pet/PetPublish.vue'),
    meta: { title: '编辑宠物信息 - 爪印', requiresAuth: true },
    props: true
  },
  {
    path: '/pets/my',
    name: 'MyPets',
    component: () => import('@/views/pet/MyPets.vue'),
    meta: { title: '我发布的宠物 - 爪印', requiresAuth: true }
  },

  // ===== 管理后台 =====
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '管理仪表盘' }
      },
      {
        path: 'pets',
        name: 'AdminPetManage',
        component: () => import('@/views/admin/PetManageView.vue'),
        meta: { title: '宠物管理' }
      },
      {
        path: 'pets/add',
        name: 'AdminPetAdd',
        component: () => import('@/views/admin/PetFormView.vue'),
        meta: { title: '添加宠物' }
      },
      {
        path: 'pets/edit/:id',
        name: 'AdminPetEdit',
        component: () => import('@/views/admin/PetFormView.vue'),
        meta: { title: '编辑宠物' },
        props: true
      },
      {
        path: 'adoptions',
        name: 'AdminAdoptionReview',
        component: () => import('@/views/admin/ApplicationReview.vue'),
        meta: { title: '领养审核' }
      },
      {
        path: 'users',
        name: 'AdminUserManage',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'articles',
        name: 'AdminArticles',
        component: () => import('@/views/admin/ArticleManage.vue'),
        meta: { title: '科普文章管理' }
      },
      {
        path: 'articles/add',
        name: 'AdminArticleAdd',
        component: () => import('@/views/admin/ArticleForm.vue'),
        meta: { title: '添加文章' }
      },
      {
        path: 'articles/edit/:id',
        name: 'AdminArticleEdit',
        component: () => import('@/views/admin/ArticleForm.vue'),
        meta: { title: '编辑文章' },
        props: true
      },
      {
        path: 'community',
        name: 'AdminCommunity',
        component: () => import('@/views/admin/CommunityReview.vue'),
        meta: { title: '社区审核' }
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '举报管理' }
      },
      {
        path: 'certs',
        name: 'AdminCerts',
        component: () => import('@/views/admin/CertReview.vue'),
        meta: { title: '认证审核' }
      },
    ]
  },

  // ===== 机构工作台 =====
  {
    path: '/shelter',
    component: () => import('@/components/layout/ShelterLayout.vue'),
    meta: { requiresAuth: true, requiresShelter: true },
    redirect: '/shelter/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'ShelterDashboard',
        component: () => import('@/views/shelter/ShelterDashboard.vue'),
        meta: { title: '机构工作台' }
      },
      {
        path: 'pets',
        name: 'ShelterPets',
        component: () => import('@/views/shelter/ShelterPetManage.vue'),
        meta: { title: '宠物管理' }
      },
      {
        path: 'adoptions',
        name: 'ShelterApplications',
        component: () => import('@/views/shelter/ShelterApplicationReview.vue'),
        meta: { title: '领养审核' }
      },
      {
        path: 'cert',
        name: 'ShelterCert',
        component: () => import('@/views/shelter/ShelterCert.vue'),
        meta: { title: '机构认证' }
      }
    ]
  },

  // ===== 公开机构主页 =====
  {
    path: '/shelter/:id',
    name: 'ShelterProfile',
    component: () => import('@/views/shelter/ShelterProfile.vue'),
    meta: { title: '机构主页 - 爪印' },
    props: true
  },

  // ===== 404 =====
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404 - 页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '爪印 - 宠物领养平台'
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
    return next({ name: 'Home' })
  }
  if (to.meta.requiresShelter && user?.role !== 'SHELTER') {
    return next({ name: 'Home' })
  }
  if (to.meta.guest && token) {
    return next({ name: 'Home' })
  }
  next()
})

export default router
