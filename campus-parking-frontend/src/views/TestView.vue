<template>
  <div class="test-container">
    <h1>🚗 校园停车管理系统 - 代理测试</h1>

    <div class="test-section">
      <h2>后端接口测试</h2>
      <button @click="testBackend" class="test-btn">测试 /api/hello</button>
      <button @click="testBackend2" class="test-btn">测试 /api/test</button>

      <div class="result">
        <h3>测试结果：</h3>
        <pre>{{ result }}</pre>
      </div>
    </div>

    <div class="info-section">
      <h3>连接信息：</h3>
      <p>前端服务: http://localhost:8080</p>
      <p>后端服务: http://localhost:8081</p>
      <p>代理配置: /api → http://localhost:8081</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authFetch } from '../utils/auth'
const result = ref('点击按钮开始测试...')

const testBackend = async () => {
  try {
    result.value = '请求中...'
    const response = await authFetch('/api/hello')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const text = await response.text()
    result.value = `✅ 成功!\nURL: /api/hello\n响应: ${text}`
  } catch (error) {
    result.value = `❌ 失败!\n错误: ${error.message}`
  }
}

const testBackend2 = async () => {
  try {
    result.value = '请求中...'
    const response = await authFetch('/api/test')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const text = await response.text()
    result.value = `✅ 成功!\nURL: /api/test\n响应: ${text}`
  } catch (error) {
    result.value = `❌ 失败!\n错误: ${error.message}`
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-section {
  margin: 30px 0;
  padding: 20px;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
}

.test-btn {
  background-color: #42b883;
  color: white;
  border: none;
  padding: 10px 20px;
  margin: 0 10px 10px 0;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.test-btn:hover {
  background-color: #369c70;
}

.result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.result pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}

.info-section {
  background-color: #e3f2fd;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #2196f3;
}
</style>
