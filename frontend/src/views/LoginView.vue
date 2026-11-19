<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {login} from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const pending = ref(false)

async function submit() {
  if (pending.value) return
  pending.value = true
  error.value = ''

  try {
    await login(username.value, password.value)
    await router.replace('/')
  } catch (failure) {
    error.value = failure.message
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <form @submit.prevent="submit">
    <h1>登录图书管理系统</h1>
    <p><label>用户名 <input v-model.trim="username" autocomplete="username" required></label></p>
    <p><label>密码 <input v-model="password" type="password" autocomplete="current-password" required></label></p>
    <button type="submit" :disabled="pending">登录</button>
    <p v-if="error" role="alert">{{ error }}</p>
  </form>
</template>