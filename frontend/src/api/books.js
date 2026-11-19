import { csrfHeaders } from './auth'

export class BookApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request(path = '', options = {}) {
  const headers = new Headers(options.headers)

  if (['POST', 'PUT', 'DELETE'].includes(options.method)) {
    const tokenHeaders = await csrfHeaders()
    for (const [name, value] of Object.entries(tokenHeaders)) {
      headers.set(name, value)
    }
  }

  const response = await fetch(`/api/books${path}`, { ...options, headers })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new BookApiError(body?.message || `请求失败（HTTP ${response.status}）`, response.status, body?.errors)
  }
  return response.status === 204 ? null : response.json()
}

export function parseBookId(raw) {
  const text = String(raw ?? '').trim()
  if (!/^[1-9]\d*$/.test(text)) return null
  const id = Number(text)
  return id <= 2147483647 ? id : null
}

const priceFormatter = new Intl.NumberFormat('zh-CN', {
  style: 'currency',
  currency: 'CNY',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

export const formatPrice = (price) => priceFormatter.format(price)

export function errorText(error, action) {
  if (error instanceof BookApiError) {
    if (error.status === 404) return '未找到这本图书，请检查 ID。'
    if (error.status === 400 && Object.keys(error.fieldErrors).length) return '请检查表单中标出的字段。'
    return error.message
  }
  return `${action}失败，请检查后端服务和网络连接。`
}

export const listBooks = () => request()
export const getBook = (id) => request(`/${id}`)
export const createBook = (book) => request('', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(book)
})
export const updateBook = (id, book) => request(`/${id}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(book)
})
export const removeBook = (id) => request(`/${id}`, { method: 'DELETE' })
