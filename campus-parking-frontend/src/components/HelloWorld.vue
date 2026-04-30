<script setup>
import { ref } from 'vue'

defineProps({
  msg: String,
})

const count = ref(0)
</script>

<template>
  <div class="hello">
    <h1>{{ msg }}</h1>

    <div class="card">
      <button type="button" @click="count++">计数器 {{ count }}</button>
    </div>
    <p>
      校园停车管理系统开发中...
    </p>
  </div>
</template>

<style scoped>
.hello {
  text-align: center;
}

.read-the-docs {
  color: #888;
}
</style>
