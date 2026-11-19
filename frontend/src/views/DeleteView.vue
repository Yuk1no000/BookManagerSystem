<script setup>
import { ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { errorText, formatPrice, getBook, parseBookId, removeBook } from '../api/books'

const route = useRoute()
const router = useRouter()
const idInput = ref('')
const loadedBook = ref(null)
const loading = ref(false)
const deleting = ref(false)
const confirming = ref(false)
const lookupError = ref('')
const deleteError = ref('')
const success = ref('')
let lookupVersion = 0

async function loadBook(raw) {
  const id = parseBookId(raw)
  if (id === null) {
    lookupVersion += 1
    loadedBook.value = null
    loading.value = false
    confirming.value = false
    lookupError.value = '请输入有效的正整数图书编号。'
    return
  }
  const version = ++lookupVersion
  loadedBook.value = null
  loading.value = true
  confirming.value = false
  lookupError.value = ''
  deleteError.value = ''
  success.value = ''
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
    confirming.value = false
    lookupError.value = '请输入有效的正整数图书编号。'
    return
  }
  if (String(route.query.id ?? '') === String(id)) loadBook(id)
  else router.push({ name: 'delete', query: { id } })
}

watch(() => route.query.id, (raw) => {
  idInput.value = raw == null ? '' : String(raw)
  if (raw == null) {
    lookupVersion += 1
    loadedBook.value = null
    lookupError.value = ''
    loading.value = false
    confirming.value = false
  } else loadBook(raw)
}, { immediate: true })

async function confirmDelete() {
  if (!loadedBook.value || !confirming.value || deleting.value) return
  deleting.value = true
  deleteError.value = ''
  const book = loadedBook.value
  try {
    await removeBook(book.id)
    success.value = `《${book.bookName}》已删除。`
    loadedBook.value = null
    idInput.value = ''
    router.replace({ name: 'delete' })
  } catch (error) {
    deleteError.value = errorText(error, '删除')
  } finally {
    deleting.value = false
    confirming.value = false
  }
}
</script>

<template>
  <div class="task-page">
    <div class="page-heading">
      <RouterLink class="back-link" to="/">返回工作台</RouterLink>
      <h1>删除图书</h1>
      <p>根据编号找到目标图书，核对信息后再确认删除。</p>
    </div>

    <section class="panel lookup-panel" aria-labelledby="delete-lookup-title">
      <h2 id="delete-lookup-title">找到需要删除的图书</h2>
      <form class="lookup-form" @submit.prevent="submitLookup">
        <div class="field">
          <label for="delete-id">图书编号</label>
          <input id="delete-id" v-model.trim="idInput" name="id" type="text" inputmode="numeric" pattern="[0-9]*"
                 placeholder="例如 1" autocomplete="off" :aria-invalid="Boolean(lookupError)"
                 :aria-describedby="lookupError ? 'delete-id-error' : undefined">
        </div>
        <button class="button button-primary" type="submit" :disabled="loading || deleting">
          {{ loading ? '载入中…' : '载入图书' }}
        </button>
      </form>
      <p v-if="lookupError" id="delete-id-error" class="notice notice-error" role="alert">{{ lookupError }}</p>
      <p v-if="loading" class="helper-text" role="status">正在载入图书…</p>
      <p v-if="success" class="notice notice-success" role="status">{{ success }}</p>
    </section>

    <section v-if="loadedBook" class="panel delete-panel" aria-labelledby="delete-book-title">
      <span class="result-id">编号 {{ loadedBook.id }}</span>
      <h2 id="delete-book-title">{{ loadedBook.bookName }}</h2>
      <dl class="book-facts">
        <div><dt>作者</dt><dd>{{ loadedBook.author }}</dd></div>
        <div><dt>价格</dt><dd>{{ formatPrice(loadedBook.price) }}</dd></div>
        <div><dt>简介</dt><dd>{{ loadedBook.description }}</dd></div>
      </dl>
      <div v-if="!confirming" class="form-actions">
        <button class="button button-danger-outline" type="button" @click="confirming = true">删除这本书</button>
      </div>
      <div v-else class="confirm-panel">
        <p>确定删除《{{ loadedBook.bookName }}》吗？删除后无法从页面恢复。</p>
        <div class="form-actions">
          <button class="button button-danger" type="button" :disabled="deleting" @click="confirmDelete">
            {{ deleting ? '删除中…' : '确认删除' }}
          </button>
          <button class="button button-quiet" type="button" :disabled="deleting" @click="confirming = false">取消</button>
        </div>
      </div>
      <p v-if="deleteError" class="notice notice-error" role="alert">{{ deleteError }}</p>
    </section>
  </div>
</template>
