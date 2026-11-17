<template>
  <main>
    <h1>图书管理系统</h1>
    <h2>图书列表</h2>

    <p v-if="books.length === 0">暂无图书</p>
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
import{ref}from'vue'
const books = ref([
  {
    id:1,name:'西游记',author:'吴冠中',price:66,description:"神魔小说，四大名著之一",
  },
  {
    id:2,name:'红楼梦',author:'忘记了',price:66,description:"四大名著之一"
  }
])
</script>
