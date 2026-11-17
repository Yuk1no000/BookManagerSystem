<template>
  <main>
    <h1>图书管理系统</h1>
    <h2>图书列表</h2>

    <button type="button" :disabled="loading"@click="loadBooks">
      刷新列表
    </button>

    <p v-if="loading">正在加载图书…</p>
    <p v-else-if="errorMessage" role="alert">{{ errorMessage }}</p>
    <p v-else-if="books.length === 0">暂无图书</p>

    <table v-else>
      <thead>
      <tr>
        <th>编号</th>
        <th>书名</th>
        <th>作者</th>
        <th>价格</th>
        <th>描述</th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="book in books" :key="book.id">
        <td>{{book.id}}</td>
        <td>{{book.name}}</td>
        <td>{{book.author}}</td>
        <td>{{book.price.toFixed(2)}}</td>
        <td>{{book.description}}</td>
      </tr>
      </tbody>
    </table>

  </main>
</template>

<script setup>
import{onMounted,ref} from 'vue'
const books = ref([])
const loading = ref(true)
const errorMessage = ref('')

async function loadBooks(){
  loading.value = true
  errorMessage.value = ''

  try{
    const response = await fetch('/api/books')
    if(!response.ok){
      throw new Error(`查询图书失败 status: ${response.status}`)
    }
    const data = await response.json()
    books.value = data
  }catch (error){
    errorMessage.value = error.message
  }finally {
    loading.value = false
  }
}
onMounted(loadBooks)
</script>
