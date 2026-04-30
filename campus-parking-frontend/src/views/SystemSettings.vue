<template>
  <div class="system-settings">
    <h1>⚙️ 系统设置</h1>

    <div class="settings-card">
      <h3>停车费用设置</h3>
      <div class="setting-item">
        <label>基础小时费率（元）:</label>
        <input v-model="config.parkingFeeRate" type="number" step="0.01" />
      </div>
      <div class="setting-item">
        <label>最大停车时长（小时）:</label>
        <input v-model="config.maxParkingHours" type="number" />
      </div>
    </div>

    <div class="settings-card">
      <h3>系统维护</h3>
      <div class="setting-item">
        <label>系统维护模式:</label>
        <label class="switch">
          <input v-model="config.systemMaintenance" type="checkbox" />
          <span class="slider"></span>
        </label>
        <span class="status">{{ config.systemMaintenance ? '开启' : '关闭' }}</span>
      </div>
    </div>

    <div class="actions">
      <button @click="saveSettings" class="btn-save">保存设置</button>
      <button @click="resetSettings" class="btn-reset">重置</button>
    </div>

    <div class="system-info">
      <h3>系统信息</h3>
      <p>应用名称: {{ config.appName }}</p>
      <p>版本: {{ config.appVersion }}</p>
      <p>运行时间: {{ uptime }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { authFetch } from '../utils/auth'

const config = ref({})
const uptime = ref('')

onMounted(() => {
  loadSettings()
  startUptimeCounter()
})

const loadSettings = async () => {
  try {
    const response = await authFetch('/api/config')
    if (response.ok) {
      config.value = await response.json()
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const saveSettings = async () => {
  try {
    const response = await authFetch('/api/config', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(config.value)
    })

    if (response.ok) {
      alert('设置保存成功！')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    alert('保存设置失败')
  }
}

const resetSettings = () => {
  if (confirm('确定要重置所有设置吗？')) {
    loadSettings()
  }
}

const startUptimeCounter = () => {
  const startTime = Date.now()
  setInterval(() => {
    const uptimeMs = Date.now() - startTime
    const hours = Math.floor(uptimeMs / (1000 * 60 * 60))
    const minutes = Math.floor((uptimeMs % (1000 * 60 * 60)) / (1000 * 60))
    uptime.value = `${hours}小时${minutes}分钟`
  }, 1000)
}
</script>

<style scoped>
.system-settings {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.settings-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.settings-card h3 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.setting-item label {
  width: 200px;
  font-weight: 500;
}

.setting-item input[type="number"] {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 120px;
}

/* 开关样式 */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
  margin: 0 10px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: #2196F3;
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.status {
  color: #666;
}

.actions {
  display: flex;
  gap: 12px;
  margin: 20px 0;
}

.btn-save {
  background: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-reset {
  background: #ff9800;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.system-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
}

.system-info h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
}

.system-info p {
  margin: 8px 0;
  color: #666;
}
</style>
