<script setup>
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { currentUser, hasRouteAccess, logout } from './api/auth'

const navigation = [
  { label: '新增', to: '/books/new' },
  { label: '查询', to: '/books/search' },
  { label: '更新', to: '/books/edit' },
  { label: '删除', to: '/books/delete' }
]

const router = useRouter()
const logoutError = ref('')

const visibleNavigation = computed(() =>
    navigation.filter(item => hasRouteAccess(router.resolve(item.to)))
)

async function signOut() {
  logoutError.value = ''

  try {
    await logout()
    await router.replace({ name: 'login' })
  } catch (error) {
    logoutError.value = error.message
  }
}
</script>


<template>
  <div v-if="currentUser">
    <span>{{ currentUser.username }}</span>
    <button type="button" @click="signOut">退出</button>
    <p v-if="logoutError" role="alert">{{ logoutError }}</p>
  </div>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <header class="site-header">
      <RouterLink class="brand" to="/" aria-label="图书管理系统，返回工作台">
        <span class="brand-mark" aria-hidden="true"><i></i><i></i><i></i></span>
        <span>图书管理<span class="brand-light"> / 工作台</span></span>
      </RouterLink>
      <nav class="site-nav" aria-label="主导航" v-if="currentUser">
        <RouterLink to="/" exact-active-class="is-active">工作台</RouterLink>
        <RouterLink v-for="item in visibleNavigation" :key="item.to" :to="item.to" active-class="is-active">
          {{ item.label }}
        </RouterLink>
      </nav>
    </header>

    <main id="main-content" class="page-container">
      <RouterView />
    </main>

    <footer class="site-footer">图书管理系统 <span>·</span> 馆藏记录管理</footer>
  </div>
</template>
