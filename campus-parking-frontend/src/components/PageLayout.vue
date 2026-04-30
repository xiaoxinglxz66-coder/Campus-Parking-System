<template>
  <div class="page-container">
    <div class="page-header" v-if="$slots.header || title">
      <slot name="header">
        <h1>{{ title }}</h1>
        <p v-if="description">{{ description }}</p>
      </slot>
    </div>
    <div class="page-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  description: String
})
</script>

<style scoped>
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 24px;
  width: 100%;
  min-height: calc(100vh - 70px);
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 2.5em;
  font-weight: 700;
  color: white;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.page-header p {
  font-size: 1.2em;
  color: rgba(255, 255, 255, 0.9);
  max-width: 600px;
  margin: 0 auto;
}

.page-content {
  width: 100%;
}
</style>
