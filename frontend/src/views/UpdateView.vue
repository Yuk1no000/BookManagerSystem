<script setup>
import { ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import BookForm from '../components/BookForm.vue'
import { BookApiError, errorText, getBook, parseBookId, updateBook } from '../api/books'

const route = useRoute()
const router = useRouter()
const idInput = ref('')
const loadedBook = ref(null)
const loading = ref(false)
const saving = ref(false)
const lookupError = ref('')
const saveError = ref('')
const success = ref('')
const fieldErrors = ref({})
let lookupVersion = 0

async function loadBook(raw) {
  const id = parseBookId(raw)
  if (id === null) {
    lookupVersion += 1
    loadedBook.value = null
    loading.value = false
    lookupError.value = '请输入有效的正整数图书编号。'
    return
  }
  const version = ++lookupVersion
  loadedBook.value = null
  loading.value = true
  lookupError.value = ''
  saveError.value = ''
  success.value = ''
  fieldErrors.value = {}
  try {
    const result = await getBook(id)
    if (version === lookupVersion) loadedBook.value = result
  } catch (error) {
    if (version === lookupVersion) lookupError.value = errorText(error, '查询')
  } finally {
    if (version === lookupVersion) loading.value = false
  }
}

function submitLookup() {
  const id = parseBookId(idInput.value)
  if (id === null) {
    lookupVersion += 1
    loadedBook.value = null
    loading.value = false
    lookupError.value = '请输入有效的正整数图书编号。'
    return
  }
  if (String(route.query.id ?? '') === String(id)) loadBook(id)
  else router.push({ name: 'edit', query: { id } })
}

watch(() => route.query.id, (raw) => {
  idInput.value = raw == null ? '' : String(raw)
  if (raw == null) {
    lookupVersion += 1
    loadedBook.value = null
    lookupError.value = ''
    loading.value = false
  } else loadBook(raw)
}, { immediate: true })

async function save(payload) {
  if (!loadedBook.value || saving.value) return
  saving.value = true
  saveError.value = ''
  success.value = ''
  fieldErrors.value = {}
  try {
    loadedBook.value = await updateBook(loadedBook.value.id, payload)
    success.value = `《${loadedBook.value.bookName}》已更新。`
  } catch (error) {
    if (error instanceof BookApiError) fieldErrors.value = error.fieldErrors
    saveError.value = errorText(error, '更新')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="task-page">
    <div class="page-heading">
      <RouterLink class="back-link" to="/">返回工作台</RouterLink>
      <h1>更新图书</h1>
      <p>先输入图书编号载入记录，确认内容后再保存修改。</p>
    </div>

    <section class="panel lookup-panel" aria-labelledby="edit-lookup-title">
      <h2 id="edit-lookup-title">找到需要更新的图书</h2>
      <form class="lookup-form" @submit.prevent="submitLookup">
        <div class="field">
          <label for="edit-id">图书编号</label>
          <input id="edit-id" v-model.trim="idInput" name="id" type="text" inputmode="numeric" pattern="[0-9]*"
                 placeholder="例如 1" autocomplete="off" :aria-invalid="Boolean(lookupError)"
                 :aria-describedby="lookupError ? 'edit-id-error' : undefined">
        </div>
        <button class="button button-primary" type="submit" :disabled="loading || saving">
          {{ loading ? '载入中…' : '载入图书' }}
        </button>
      </form>
      <p v-if="lookupError" id="edit-id-error" class="notice notice-error" role="alert">{{ lookupError }}</p>
      <p v-if="loading" class="helper-text" role="status">正在载入图书…</p>
    </section>

    <section v-if="loadedBook" class="panel form-panel" aria-labelledby="edit-form-title">
      <div class="panel-heading">
        <div>
          <span class="result-id">编号 {{ loadedBook.id }}</span>
          <h2 id="edit-form-title">修改《{{ loadedBook.bookName }}》</h2>
        </div>
      </div>
      <BookForm :initial-book="loadedBook" :busy="saving" :errors="fieldErrors"
                submit-label="保存修改" @submit="save" />
      <p v-if="saveError" class="notice notice-error" role="alert">{{ saveError }}</p>
      <p v-if="success" class="notice notice-success" role="status">{{ success }}</p>
    </section>
  </div>
</template>
