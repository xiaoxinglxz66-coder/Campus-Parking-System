<template>
  <div class="campus-vehicle-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <span class="title-icon">🚗</span>
          我的车辆管理
        </h1>
        <p class="page-subtitle">管理您的车辆信息，审核通过后可享受免费停车</p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 车辆列表 -->
      <div class="vehicle-section">
        <div class="section-header">
          <h2 class="section-title">我的车辆</h2>
          <button class="btn-primary" @click="showAddModal = true" :disabled="hasPendingVehicle">
            <span class="btn-icon">➕</span>
            添加车辆
          </button>
        </div>

        <!-- 提示信息 -->
        <div v-if="hasPendingVehicle" class="info-alert">
          <div class="alert-icon">⏳</div>
          <div class="alert-content">
            <h4>车辆审核中</h4>
            <p>您有车辆正在等待管理员审核，审核通过后方可停车</p>
          </div>
        </div>

        <!-- 车辆列表 -->
        <div class="vehicle-list" v-if="vehicles.length > 0">
          <div v-for="vehicle in vehicles" :key="vehicle.id" class="vehicle-card">
            <div class="vehicle-main">
              <div class="vehicle-header">
                <h3 class="plate-number">{{ vehicle.plateNumber }}</h3>
                <div class="vehicle-meta">
                  <span class="vehicle-type">{{ getVehicleTypeText(vehicle.vehicleType) }}</span>
                  <span class="status-badge" :class="getStatusClass(vehicle.status)">
                    {{ getStatusText(vehicle.status) }}
                  </span>
                </div>
              </div>

              <div class="vehicle-details">
                <div class="detail-group">
                  <div class="detail-item">
                    <span class="label">品牌：</span>
                    <span class="value">{{ vehicle.brand || '未设置' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">颜色：</span>
                    <span class="value">{{ vehicle.color || '未设置' }}</span>
                  </div>
                </div>
                <div class="time-info">
                  <span class="time-text">提交时间：{{ formatDate(vehicle.createdAt) }}</span>
                  <span v-if="vehicle.updatedAt && vehicle.updatedAt !== vehicle.createdAt" class="time-text">
                    | 最后更新：{{ formatDate(vehicle.updatedAt) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="vehicle-actions">
              <div class="action-info" v-if="vehicle.status === 'PENDING'">
                <span class="pending-text">⏳ 等待审核中...</span>
                <button class="btn-delete" @click="deleteVehicle(vehicle.id)">删除</button>
              </div>

              <div class="action-info" v-else-if="vehicle.status === 'APPROVED'">
                <span class="approved-text">✅ 已审核通过</span>
                <div class="action-buttons">
                  <button class="btn-edit" @click="editVehicle(vehicle)" :disabled="editingVehicleId === vehicle.id">
                    <span v-if="editingVehicleId === vehicle.id">编辑中...</span>
                    <span v-else>编辑</span>
                  </button>
                  <button class="btn-delete" @click="deleteVehicle(vehicle.id)">删除</button>
                </div>
              </div>

              <div class="action-info" v-else-if="vehicle.status === 'REJECTED'">
                <span class="rejected-text">❌ 审核未通过</span>
                <div class="action-buttons">
                  <button class="btn-delete" @click="deleteVehicle(vehicle.id)">删除</button>
                  <button class="btn-resubmit" @click="resubmitVehicle(vehicle)">重新提交</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">🚗</div>
          <h3>暂无车辆</h3>
          <p>您还没有添加任何车辆，点击上方按钮添加第一辆车</p>
        </div>
      </div>

      <!-- 使用说明 -->
      <div class="instruction-section">
        <div class="section-header">
          <h2 class="section-title">📋 使用说明</h2>
        </div>
        <div class="instruction-cards">
          <div class="instruction-card">
            <div class="card-icon">1️⃣</div>
            <div class="card-content">
              <h4>添加车辆</h4>
              <p>填写车辆信息并提交审核</p>
            </div>
          </div>
          <div class="instruction-card">
            <div class="card-icon">2️⃣</div>
            <div class="card-content">
              <h4>等待审核</h4>
              <p>管理员将在24小时内完成审核</p>
            </div>
          </div>
          <div class="instruction-card">
            <div class="card-icon">3️⃣</div>
            <div class="card-content">
              <h4>开始停车</h4>
              <p>审核通过后即可享受免费停车</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加车辆弹窗 -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加车辆</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitVehicle">
            <div class="form-group">
              <label>车牌号 <span class="required">*</span></label>
              <input
                v-model="addForm.plateNumber"
                type="text"
                required
                placeholder="请输入车牌号，如：粤A12345"
                maxlength="10"
                :disabled="addingVehicle"
              >
              <div class="form-hint">请填写完整的车牌号码</div>
            </div>

            <div class="form-group">
              <label>车辆类型</label>
              <select v-model="addForm.vehicleType" :disabled="addingVehicle">
                <option value="CAR">小型汽车</option>
                <option value="MOTORCYCLE">摩托车</option>
                <option value="ELECTRIC_CAR">电动车</option>
              </select>
            </div>

            <div class="form-group">
              <label>品牌</label>
              <input
                v-model="addForm.brand"
                type="text"
                placeholder="请输入车辆品牌，如：丰田、本田"
                :disabled="addingVehicle"
              >
            </div>

            <div class="form-group">
              <label>颜色</label>
              <input
                v-model="addForm.color"
                type="text"
                placeholder="请输入车辆颜色，如：白色、黑色"
                :disabled="addingVehicle"
              >
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal" :disabled="addingVehicle">取消</button>
          <button class="btn-primary" @click="submitVehicle" :disabled="!addForm.plateNumber || addingVehicle">
            <span v-if="addingVehicle">提交中...</span>
            <span v-else>提交审核</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑车辆弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑车辆信息</h3>
          <button class="close-btn" @click="closeEditModal" :disabled="updatingVehicle">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateVehicle">
            <div class="form-group">
              <label>车牌号 <span class="required">*</span></label>
              <input
                v-model="editForm.plateNumber"
                type="text"
                required
                placeholder="请输入车牌号"
                maxlength="10"
                :disabled="true"
              >
              <div class="form-hint">车牌号不可修改</div>
            </div>

            <div class="form-group">
              <label>车辆类型</label>
              <select v-model="editForm.vehicleType" :disabled="updatingVehicle">
                <option value="CAR">小型汽车</option>
                <option value="MOTORCYCLE">摩托车</option>
                <option value="ELECTRIC_CAR">电动车</option>
              </select>
            </div>

            <div class="form-group">
              <label>品牌</label>
              <input
                v-model="editForm.brand"
                type="text"
                placeholder="请输入车辆品牌"
                :disabled="updatingVehicle"
              >
            </div>

            <div class="form-group">
              <label>颜色</label>
              <input
                v-model="editForm.color"
                type="text"
                placeholder="请输入车辆颜色"
                :disabled="updatingVehicle"
              >
            </div>

            <div class="form-group">
              <label>当前状态</label>
              <div class="status-display">
                <span class="status-badge" :class="getStatusClass(editForm.status)">
                  {{ getStatusText(editForm.status) }}
                </span>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeEditModal" :disabled="updatingVehicle">取消</button>
          <button class="btn-primary" @click="updateVehicle" :disabled="!editForm.plateNumber || updatingVehicle || !isEditFormChanged">
            <span v-if="updatingVehicle">保存中...</span>
            <span v-else>保存修改</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const vehicles = ref([])
const loading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)
const addingVehicle = ref(false)
const updatingVehicle = ref(false)
const editingVehicleId = ref(null)

// 添加表单
const addForm = reactive({
  plateNumber: '',
  vehicleType: 'CAR',
  brand: '',
  color: ''
})

// 编辑表单
const editForm = reactive({
  id: '',
  plateNumber: '',
  vehicleType: 'CAR',
  brand: '',
  color: '',
  status: ''
})

// 原始编辑数据（用于检查是否有修改）
const originalEditData = reactive({
  vehicleType: '',
  brand: '',
  color: ''
})

// 计算属性
const hasPendingVehicle = computed(() => {
  return vehicles.value.some(vehicle => vehicle.status === 'PENDING')
})

const isEditFormChanged = computed(() => {
  return editForm.vehicleType !== originalEditData.vehicleType ||
    editForm.brand !== originalEditData.brand ||
    editForm.color !== originalEditData.color
})

// 方法
// 加载用户车辆数据
const loadVehicles = async () => {
  try {
    loading.value = true
    const token = localStorage.getItem('token')

    const response = await fetch('/api/vehicles/my-vehicles', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      vehicles.value = await response.json()
      console.log('加载车辆数据成功:', vehicles.value)
    } else {
      console.error('获取车辆数据失败:', response.status)
      await loadVehiclesFallback()
    }
  } catch (error) {
    console.error('加载车辆数据异常:', error)
    await loadVehiclesFallback()
  } finally {
    loading.value = false
  }
}

// 备选加载方法
const loadVehiclesFallback = async () => {
  try {
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')

    if (userId) {
      const response = await fetch(`/api/vehicles/user/${userId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (response.ok) {
        vehicles.value = await response.json()
      }
    }
  } catch (error) {
    console.error('备选加载也失败:', error)
  }
}

// 添加车辆
const submitVehicle = async () => {
  if (!addForm.plateNumber.trim()) {
    alert('请输入车牌号')
    return
  }

  try {
    addingVehicle.value = true
    const token = localStorage.getItem('token')

    const vehicleData = {
      plateNumber: addForm.plateNumber.trim(),
      vehicleType: addForm.vehicleType,
      brand: addForm.brand.trim(),
      color: addForm.color.trim()
    }

    const response = await fetch('/api/vehicles/my-vehicles', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(vehicleData)
    })

    if (response.ok) {
      const newVehicle = await response.json()
      alert('车辆添加成功！请等待管理员审核，审核通过后即可停车。')
      closeModal()
      await loadVehicles()
    } else {
      const error = await response.text()
      alert(`添加失败: ${error}`)
    }
  } catch (error) {
    console.error('添加车辆失败:', error)
    alert('添加失败，请重试')
  } finally {
    addingVehicle.value = false
  }
}

// 编辑车辆
const editVehicle = (vehicle) => {
  // 检查车辆状态，只有已审核的车辆可以编辑
  if (vehicle.status !== 'APPROVED') {
    alert('只有已审核通过的车辆可以编辑')
    return
  }

  editingVehicleId.value = vehicle.id

  // 填充编辑表单
  editForm.id = vehicle.id
  editForm.plateNumber = vehicle.plateNumber
  editForm.vehicleType = vehicle.vehicleType
  editForm.brand = vehicle.brand || ''
  editForm.color = vehicle.color || ''
  editForm.status = vehicle.status

  // 保存原始数据用于比较
  originalEditData.vehicleType = vehicle.vehicleType
  originalEditData.brand = vehicle.brand || ''
  originalEditData.color = vehicle.color || ''

  showEditModal.value = true
}

// 更新车辆
const updateVehicle = async () => {
  if (!isEditFormChanged.value) {
    alert('没有需要保存的修改')
    return
  }

  try {
    updatingVehicle.value = true
    const token = localStorage.getItem('token')

    const vehicleData = {
      vehicleType: editForm.vehicleType,
      brand: editForm.brand.trim(),
      color: editForm.color.trim()
    }

    const response = await fetch(`/api/vehicles/${editForm.id}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(vehicleData)
    })

    if (response.ok) {
      const updatedVehicle = await response.json()
      alert('车辆信息更新成功！')
      closeEditModal()
      await loadVehicles()
    } else {
      const error = await response.text()
      alert(`更新失败: ${error}`)
    }
  } catch (error) {
    console.error('更新车辆失败:', error)
    alert('更新失败，请重试')
  } finally {
    updatingVehicle.value = false
    editingVehicleId.value = null
  }
}

// 删除车辆
const deleteVehicle = async (vehicleId) => {
  if (!confirm('确定要删除这辆车吗？')) return

  try {
    const token = localStorage.getItem('token')

    const response = await fetch(`/api/vehicles/${vehicleId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.ok) {
      alert('车辆删除成功')
      await loadVehicles()
    } else {
      const error = await response.text()
      alert(`删除失败: ${error}`)
    }
  } catch (error) {
    console.error('删除车辆失败:', error)
    alert('删除失败，请重试')
  }
}

// 重新提交审核
const resubmitVehicle = async (vehicle) => {
  if (!confirm('确定要重新提交这辆车的审核吗？')) return

  try {
    const token = localStorage.getItem('token')

    const response = await fetch(`/api/vehicles/${vehicle.id}/resubmit`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      alert('车辆已重新提交审核')
      await loadVehicles()
    } else {
      const error = await response.text()
      alert(`重新提交失败: ${error}`)
    }
  } catch (error) {
    console.error('重新提交失败:', error)
    alert('重新提交失败，请重试')
  }
}

// 关闭添加弹窗
const closeModal = () => {
  showAddModal.value = false
  addForm.plateNumber = ''
  addForm.vehicleType = 'CAR'
  addForm.brand = ''
  addForm.color = ''
  addingVehicle.value = false
}

// 关闭编辑弹窗
const closeEditModal = () => {
  showEditModal.value = false
  editForm.id = ''
  editForm.plateNumber = ''
  editForm.vehicleType = 'CAR'
  editForm.brand = ''
  editForm.color = ''
  editForm.status = ''
  originalEditData.vehicleType = ''
  originalEditData.brand = ''
  originalEditData.color = ''
  updatingVehicle.value = false
  editingVehicleId.value = null
}

// 工具方法
const getVehicleTypeText = (type) => {
  const types = {
    'CAR': '小型汽车',
    'MOTORCYCLE': '摩托车',
    'ELECTRIC_CAR': '电动车'
  }
  return types[type] || type
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '未通过'
  }
  return statusMap[status] || status
}

const getStatusClass = (status) => {
  return status.toLowerCase()
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  try {
    return new Date(dateString).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

// 初始化加载
onMounted(() => {
  loadVehicles()
})
</script>

<style scoped>
.campus-vehicle-management {
  background: white;
  min-height: 100vh;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
/* 编辑弹窗特定的样式 */
.status-display {
  padding: 8px 0;
}

.status-display .status-badge {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

/* 编辑按钮状态 */
.btn-edit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式设计 - 保持原有样式，只添加编辑相关的响应式 */
@media (max-width: 768px) {
  .modal-content {
    width: 90vw;
    margin: 10px;
  }

  .action-buttons {
    flex-wrap: wrap;
  }
}
.page-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 32px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 区域样式 */
.vehicle-section,
.instruction-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e8e8e8;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 提示信息 */
.info-alert {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.alert-icon {
  font-size: 20px;
  margin-top: 2px;
}

.alert-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
}

.alert-content p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

/* 车辆列表 */
.vehicle-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.vehicle-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.2s ease;
}

.vehicle-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

.vehicle-main {
  flex: 1;
}

.vehicle-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.plate-number {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.vehicle-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.vehicle-type {
  background: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.rejected {
  background: #fff2f0;
  color: #ff4d4f;
}

.vehicle-details {
  margin-bottom: 12px;
}

.detail-group {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.label {
  color: #666;
}

.value {
  color: #1a1a1a;
  font-weight: 500;
}

.time-info {
  font-size: 12px;
  color: #999;
}

/* 车辆操作 */
.vehicle-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  min-width: 150px;
}

.action-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.pending-text {
  color: #fa8c16;
  font-size: 14px;
  font-weight: 500;
}

.approved-text {
  color: #52c41a;
  font-size: 14px;
  font-weight: 500;
}

.rejected-text {
  color: #ff4d4f;
  font-size: 14px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

/* 按钮样式 */
.btn-primary, .btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-primary:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  background: #e8e8e8;
}

.btn-edit, .btn-delete, .btn-resubmit {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
}

.btn-edit {
  color: #1890ff;
  border-color: #1890ff;
}

.btn-edit:hover {
  background: #e6f7ff;
}

.btn-delete {
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.btn-delete:hover {
  background: #fff2f0;
}

.btn-resubmit {
  color: #1890ff;
  border-color: #1890ff;
}

.btn-resubmit:hover {
  background: #e6f7ff;
}

/* 使用说明 */
.instruction-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.instruction-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  transition: all 0.2s ease;
}

.instruction-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-icon {
  font-size: 24px;
  margin-top: 2px;
}

.card-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-content p {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #666;
}

.empty-state p {
  font-size: 14px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 480px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #666;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.required {
  color: #ff4d4f;
}

.modal-footer {
  padding: 20px 24px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  border: 3px solid #f3f4f6;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .campus-vehicle-management {
    padding: 16px;
  }

  .vehicle-card {
    flex-direction: column;
    gap: 16px;
  }

  .vehicle-actions {
    align-items: stretch;
    width: 100%;
  }

  .action-info {
    align-items: stretch;
  }

  .action-buttons {
    justify-content: flex-end;
  }

  .instruction-cards {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
