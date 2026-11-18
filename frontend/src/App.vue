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
        <td>{{book.bookName}}</td>
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

const form = ref({
  name:'',
  author:'',
  price:'',
  description:''
})

const saving = ref(false)
const saveError = ref('')
const fieldErrors = ref({})

async function addBooks(){
  if(saving.value)
    return
  saving.value = true
  saveError.value = ''
  fieldErrors.value = {}

  try{
    const payload = {
      name:form.value.name,
      author:form.value.author,
      price:form.value.price === ''?null:form.value.price,
      description:form.value.description
    }

    const response = await fetch('/api/books',{
      method:'POST',
      headers:{'Context-Type':'application/json'},
      body:JSON.stringify(payload)
    })
    if(!response.ok){
      const error = await response.json().json().catch(()=>null)
      fieldErrors.value = error?.errors ?? {}
      saveError.value = error?.message ?? `新增失败(http${response.status}`
      return
    }
    form.value = {name:'',author: '',price:'',description: ''}
    await loadBooks()
  }catch (error){
    saveError.value = `提交请求失败：${error.message}`
  }finally {
    saving.value = false
  }

}


onMounted(loadBooks)
</script>
