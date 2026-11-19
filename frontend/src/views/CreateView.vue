<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import BookForm from '../components/BookForm.vue'
import { BookApiError, createBook, errorText } from '../api/books'

const busy = ref(false)
const errors = ref({})
const message = ref('')
const createdBook = ref(null)
const formVersion = ref(0)

async function save(payload) {
  if (busy.value) return
  busy.value = true
  errors.value = {}
  message.value = ''
  createdBook.value = null
  try {
    createdBook.value = await createBook(payload)
    formVersion.value += 1
  } catch (error) {
    if (error instanceof BookApiError) errors.value = error.fieldErrors
    message.value = errorText(error, '新增')
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div class="task-page">
    <div class="page-heading">
      <RouterLink class="back-link" to="/">返回工作台</RouterLink>
      <h1>新增图书</h1>
      <p>填写图书信息，保存后会生成一个新的图书编号。</p>
    </div>

    <section class="panel form-panel" aria-label="新增图书表单">
      <BookForm :key="formVersion" :busy="busy" :errors="errors" submit-label="保存图书" @submit="save" />
      <p v-if="message" class="notice notice-error" role="alert">{{ message }}</p>
      <div v-if="createdBook" class="notice notice-success" role="status">
        已新增《{{ createdBook.bookName }}》，编号为 {{ createdBook.id }}。
        <RouterLink :to="{ name: 'search', query: { id: createdBook.id } }">查看这本书</RouterLink>
      </div>
    </section>
  </div>
</template>
