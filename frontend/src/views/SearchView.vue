<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { errorText, formatPrice, getBook, listBooks, parseBookId } from '../api/books'

const route = useRoute()
const router = useRouter()
const idInput = ref('')
const idError = ref('')
const book = ref(null)
const searching = ref(false)
const books = ref([])
const listLoading = ref(true)
const listError = ref('')
let searchVersion = 0

async function searchById(raw) {
  const id = parseBookId(raw)
  if (id === null) {
    searchVersion += 1
    searching.value = false
    idError.value = '请输入有效的正整数图书编号。'
    book.value = null
    return
  }
  const version = ++searchVersion
  searching.value = true
  idError.value = ''
  book.value = null
  try {
    const result = await getBook(id)
    if (version === searchVersion) book.value = result
  } catch (error) {
    if (version === searchVersion) idError.value = errorText(error, '查询')
  } finally {
    if (version === searchVersion) searching.value = false
  }
}

function submitSearch() {
  const id = parseBookId(idInput.value)
  if (id === null) {
    searchVersion += 1
    searching.value = false
    book.value = null
    idError.value = '请输入有效的正整数图书编号。'
    return
  }
  if (String(route.query.id ?? '') === String(id)) searchById(id)
  else router.push({ name: 'search', query: { id } })
}

watch(() => route.query.id, (raw) => {
  idInput.value = raw == null ? '' : String(raw)
  if (raw == null) {
    searchVersion += 1
    book.value = null
    idError.value = ''
    searching.value = false
  } else searchById(raw)
}, { immediate: true })

async function loadAll() {
  listLoading.value = true
  listError.value = ''
  try {
    books.value = await listBooks()
  } catch (error) {
    listError.value = errorText(error, '加载目录')
  } finally {
    listLoading.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="task-page">
    <div class="page-heading">
      <RouterLink class="back-link" to="/">返回工作台</RouterLink>
      <h1>查询图书</h1>
      <p>输入图书编号精确查询，或在下方浏览全部馆藏记录。</p>
    </div>

    <section class="panel lookup-panel" aria-labelledby="id-search-title">
      <h2 id="id-search-title">按编号查询</h2>
      <form class="lookup-form" @submit.prevent="submitSearch">
        <div class="field">
          <label for="search-id">图书编号</label>
          <input id="search-id" v-model.trim="idInput" name="id" type="text" inputmode="numeric" pattern="[0-9]*"
                 placeholder="例如 1" autocomplete="off" :aria-invalid="Boolean(idError)"
                 :aria-describedby="idError ? 'search-id-error' : undefined">
        </div>
        <button class="button button-primary" type="submit" :disabled="searching">
          {{ searching ? '查询中…' : '查询图书' }}
        </button>
      </form>
      <p v-if="idError" id="search-id-error" class="notice notice-error" role="alert">{{ idError }}</p>
      <p v-if="searching" class="helper-text" role="status">正在查找图书…</p>
      <article v-if="book" class="result-card" aria-label="查询结果">
        <span class="result-id">编号 {{ book.id }}</span>
        <h3>{{ book.bookName }}</h3>
        <dl class="book-facts">
          <div><dt>作者</dt><dd>{{ book.author }}</dd></div>
          <div><dt>价格</dt><dd>{{ formatPrice(book.price) }}</dd></div>
          <div><dt>简介</dt><dd>{{ book.description }}</dd></div>
        </dl>
        <div class="result-actions">
          <RouterLink :to="{ name: 'edit', query: { id: book.id } }">更新这本书</RouterLink>
          <RouterLink :to="{ name: 'delete', query: { id: book.id } }">删除这本书</RouterLink>
        </div>
      </article>
    </section>

    <section class="catalog-section" aria-labelledby="catalog-title">
      <div class="section-heading">
        <div>
          <h2 id="catalog-title">全部图书</h2>
          <p>选择编号可在上方查看完整信息。</p>
        </div>
        <button class="button button-quiet" type="button" :disabled="listLoading" @click="loadAll">刷新目录</button>
      </div>
      <p v-if="listLoading" class="helper-text" role="status">正在加载图书…</p>
      <p v-else-if="listError" class="notice notice-error" role="alert">{{ listError }}</p>
      <div v-else-if="books.length === 0" class="empty-state">
        暂无图书。<RouterLink to="/books/new">去新增第一本书</RouterLink>
      </div>
      <div v-else class="table-wrap">
        <table>
          <thead><tr><th scope="col">编号</th><th scope="col">书名</th><th scope="col">作者</th><th scope="col">价格</th></tr></thead>
          <tbody>
            <tr v-for="item in books" :key="item.id">
              <td><RouterLink :to="{ name: 'search', query: { id: item.id } }">{{ item.id }}</RouterLink></td>
              <td>{{ item.bookName }}</td>
              <td>{{ item.author }}</td>
              <td>{{ formatPrice(item.price) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
