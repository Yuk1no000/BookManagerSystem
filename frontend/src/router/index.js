import { createRouter, createWebHistory } from 'vue-router'
import { currentUser, hasRouteAccess, loadCurrentUser } from '../api/auth'
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    { path: '/books/search', name: 'search', component: () => import('../views/SearchView.vue') },
    {
      path: '/books/new',
      name: 'create',
      component: () => import('../views/CreateView.vue'),
      meta: { roles: ['LIBRARIAN', 'ADMIN'] },
    },
    {
      path: '/books/edit',
      name: 'edit',
      component: () => import('../views/UpdateView.vue'),
      meta: { roles: ['LIBRARIAN', 'ADMIN'] },
    },
    {
      path: '/books/delete',
      name: 'delete',
      component: () => import('../views/DeleteView.vue'),
      meta: { roles: ['ADMIN'] },
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})
router.beforeEach(async to => {
  if (to.name === 'login') return true

  if (!currentUser.value) {
    try {
      await loadCurrentUser()
    } catch {
      return { name: 'login' }
    }
  }

  if (!currentUser.value) return { name: 'login' }
  if (!hasRouteAccess(to)) return { name: 'home' }
  return true
})
export default router
