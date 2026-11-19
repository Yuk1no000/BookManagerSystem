<script setup>
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { hasRouteAccess } from '../api/auth'
const actions = [
  {
    name: '新增图书',
    description: '录入书名、作者、价格与简介，建立一条新的馆藏记录。',
    to: '/books/new',
    tone: 'create',
    paths: ['M4 5.5A2.5 2.5 0 0 1 6.5 3H15v17H6.5A2.5 2.5 0 0 0 4 22V5.5Z', 'M4 20.5A2.5 2.5 0 0 1 6.5 18H15', 'M19 8v8', 'M15 12h8']
  },
  {
    name: '查询图书',
    description: '浏览全部图书，也可以输入编号快速定位一本书。',
    to: '/books/search',
    tone: 'search',
    paths: ['M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z', 'm17 17 4 4', 'M8 10h6', 'M8 13h4']
  },
  {
    name: '更新图书',
    description: '根据图书编号载入记录，再修改需要调整的信息。',
    to: '/books/edit',
    tone: 'update',
    paths: ['M4 20h16', 'M6 15.5 16.8 4.7a2 2 0 0 1 2.8 2.8L8.8 18.3 5 19l.7-3.5Z', 'm15.5 6 2.5 2.5']
  },
  {
    name: '删除图书',
    description: '先核对目标图书，再通过二次确认删除记录。',
    to: '/books/delete',
    tone: 'delete',
    paths: ['M4 7h16', 'M9 7V4h6v3', 'M6 7l1 14h10l1-14', 'M10 11v6', 'M14 11v6']
  }
]
const router = useRouter()
const visibleActions = computed(() =>
  actions.filter(action => hasRouteAccess(router.resolve(action.to)))
)
</script>

<template>
  <section class="home-intro" aria-labelledby="home-title">
    <div class="home-intro-copy">
      <p class="section-label">馆藏工作台</p>
      <h1 id="home-title">管理每一本图书，<br>从这里开始。</h1>
      <p class="lead">选择下面的一项操作，进入对应页面完成图书记录管理。</p>
    </div>
    <div class="shelf-illustration" aria-hidden="true">
      <span class="shelf-book shelf-book-one"></span>
      <span class="shelf-book shelf-book-two"></span>
      <span class="shelf-book shelf-book-three"></span>
      <span class="shelf-book shelf-book-four"></span>
      <span class="shelf-line"></span>
    </div>
  </section>

  <section class="home-actions" aria-label="图书操作">
    <RouterLink v-for="action in actions" :key="action.to" :to="action.to"
                class="action-card" :class="`action-card--${action.tone}`">
      <span class="action-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <path v-for="path in action.paths" :key="path" :d="path" />
        </svg>
      </span>
      <span class="action-content">
        <span class="action-title">{{ action.name }}</span>
        <span class="action-description">{{ action.description }}</span>
      </span>
      <span class="action-arrow" aria-hidden="true">↗</span>
    </RouterLink>
  </section>
</template>
