<script setup>
import { nextTick, ref, watch } from 'vue'

const props = defineProps({
  initialBook: { type: Object, default: null },
  busy: { type: Boolean, default: false },
  errors: { type: Object, default: () => ({}) },
  submitLabel: { type: String, required: true }
})
const emit = defineEmits(['submit'])

const emptyForm = () => ({ name: '', author: '', price: '', description: '' })
const form = ref(emptyForm())
const formElement = ref(null)

watch(() => props.initialBook, (book) => {
  form.value = book ? {
    name: book.bookName,
    author: book.author,
    price: book.price,
    description: book.description
  } : emptyForm()
}, { immediate: true })

watch(() => props.errors, async (errors) => {
  if (!Object.keys(errors).length) return
  await nextTick()
  formElement.value?.querySelector('[aria-invalid="true"]')?.focus()
})

function submit() {
  emit('submit', {
    ...form.value,
    price: form.value.price === '' ? null : form.value.price
  })
}
</script>

<template>
  <form ref="formElement" class="book-form" novalidate @submit.prevent="submit">
    <fieldset :disabled="busy">
      <div class="field-grid">
        <div class="field">
          <label for="book-name">书名</label>
          <input id="book-name" v-model.trim="form.name" name="name" maxlength="100" autocomplete="off"
                 :aria-invalid="Boolean(errors.name)" :aria-describedby="errors.name ? 'book-name-error' : undefined">
          <p v-if="errors.name" id="book-name-error" class="field-error">{{ errors.name }}</p>
        </div>
        <div class="field">
          <label for="book-author">作者</label>
          <input id="book-author" v-model.trim="form.author" name="author" maxlength="100" autocomplete="off"
                 :aria-invalid="Boolean(errors.author)" :aria-describedby="errors.author ? 'book-author-error' : undefined">
          <p v-if="errors.author" id="book-author-error" class="field-error">{{ errors.author }}</p>
        </div>
        <div class="field">
          <label for="book-price">价格 <span class="field-unit">元</span></label>
          <input id="book-price" v-model.number="form.price" name="price" type="number" min="0" step="0.01"
                 inputmode="decimal" autocomplete="off"
                 :aria-invalid="Boolean(errors.price)" :aria-describedby="errors.price ? 'book-price-error' : undefined">
          <p v-if="errors.price" id="book-price-error" class="field-error">{{ errors.price }}</p>
        </div>
        <div class="field field-wide">
          <label for="book-description">简介</label>
          <textarea id="book-description" v-model.trim="form.description" name="description" rows="4" maxlength="100"
                    autocomplete="off"
                    :aria-invalid="Boolean(errors.description)"
                    :aria-describedby="errors.description ? 'book-description-error' : undefined"></textarea>
          <p v-if="errors.description" id="book-description-error" class="field-error">{{ errors.description }}</p>
        </div>
      </div>
      <div class="form-actions">
        <button class="button button-primary" type="submit">{{ busy ? '保存中…' : submitLabel }}</button>
      </div>
    </fieldset>
  </form>
</template>
