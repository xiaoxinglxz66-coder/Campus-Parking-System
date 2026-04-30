<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-main">
          <h1 class="page-title">
            <span class="title-icon">👥</span>
            用户管理系统
          </h1>
          <p class="page-subtitle">管理用户信息和审核注册申请</p>
        </div>
        <div class="header-actions">
          <button class="refresh-btn" @click="refreshData">
            <span class="refresh-icon">🔄</span>
            刷新数据
          </button>
          <button @click="showAddDialog = true" class="btn-primary">
            <span class="btn-icon">➕</span>
            添加用户
          </button>
        </div>
      </div>
    </div>

    <!-- 上半部分：用户审核框架 -->
    <div class="approval-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-icon">✅</span>
          用户审核
          <span v-if="pendingApprovalCount > 0" class="badge">{{ pendingApprovalCount }} 待审</span>
        </h2>
        <div class="section-actions">
          <select v-model="approvalFilter" @change="loadPendingUsers" class="filter-select">
            <option value="">所有类型</option>
            <option value="STUDENT">学生</option>
            <option value="TEACHER">教师</option>
            <option value="STAFF">职工</option>
          </select>
        </div>
      </div>

      <!-- 审核统计卡片 -->
      <div class="approval-stats">
        <div class="stat-card pending">
          <div class="stat-icon">⏳</div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingApprovalCount }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
        <div class="stat-card approved">
          <div class="stat-icon">✅</div>
          <div class="stat-content">
            <div class="stat-value">{{ approvedCount }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
        <div class="stat-card rejected">
          <div class="stat-icon">❌</div>
          <div class="stat-content">
            <div class="stat-value">{{ rejectedCount }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
      </div>

      <!-- 待审核用户列表 -->
      <div class="approval-list">
        <div v-if="approvalLoading" class="loading-state">
          <div class="loading-spinner"></div>
          <span>加载审核数据中...</span>
        </div>

        <div v-else-if="pendingUsers.length === 0" class="empty-state">
          <div class="empty-icon">🎉</div>
          <h3>暂无待审核用户</h3>
          <p>所有用户申请都已处理完成</p>
        </div>

        <div v-else class="users-grid">
          <div v-for="user in pendingUsers" :key="user.id" class="approval-card">
            <div class="user-avatar">
              {{ getUserAvatar(user.userType) }}
            </div>
            <div class="user-info">
              <h4 class="user-name">{{ user.realName || '未设置姓名' }}</h4>
              <p class="user-username">@{{ user.username }}</p>
              <div class="user-meta">
                <span class="user-type">{{ getUserTypeText(user.userType) }}</span>
                <span class="user-phone" v-if="user.phone">{{ user.phone }}</span>
              </div>
              <div class="user-reg-time">
                注册时间: {{ formatTime(user.createdTime) }}
              </div>
            </div>
            <div class="approval-actions">
              <button @click="approveUser(user.id)" class="btn-approve">
                <span class="btn-icon">✅</span>
                通过
              </button>
              <button @click="rejectUser(user.id)" class="btn-reject">
                <span class="btn-icon">❌</span>
                拒绝
              </button>
              <button @click="viewUserDetails(user)" class="btn-details">
                详情
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 下半部分：用户管理框架 -->
    <div class="management-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="section-icon">👥</span>
          用户管理
          <span class="total-badge">{{ totalUsers }} 用户</span>
        </h2>
        <div class="section-actions">
          <div class="search-box">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索用户名、姓名或手机号..."
              class="search-input"
              @keyup.enter="searchUsers"
            />
            <button @click="searchUsers" class="search-btn">搜索</button>
          </div>
          <select v-model="statusFilter" @change="loadAllUsers" class="filter-select">
            <option value="">所有状态</option>
            <option value="PENDING">待审核</option>
            <option value="APPROVED">已通过</option>
            <option value="REJECTED">已拒绝</option>
          </select>
          <select v-model="userTypeFilter" @change="loadAllUsers" class="filter-select">
            <option value="">所有类型</option>
            <option value="STUDENT">学生</option>
            <option value="TEACHER">教师</option>
            <option value="STAFF">职工</option>
            <option value="ADMIN">管理员</option>
            <option value="EXTERNAL_USER">校外用户</option>
          </select>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="management-list">
        <div v-if="managementLoading" class="loading-state">
          <div class="loading-spinner"></div>
          <span>加载用户数据中...</span>
        </div>

        <div v-else-if="allUsers.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <h3>暂无用户数据</h3>
          <p>系统中还没有用户记录</p>
        </div>

        <div v-else class="users-table-container">
          <table class="users-table">
            <thead>
            <tr>
              <th>ID</th>
              <th>真实姓名</th>
              <th>用户名</th>
              <th>手机号</th>
              <th>用户类型</th>
              <th>审核状态</th>
              <th>余额</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in filteredUsers" :key="user.id">
              <td>{{ user.id }}</td>
              <td>
                <div class="user-cell">
                  <span class="user-avatar-small">{{ getUserAvatar(user.userType) }}</span>
                  {{ user.username }}
                </div>
              </td>
              <td>{{ user.realName || '-' }}</td>
              <td>{{ user.phone || '-' }}</td>
              <td>
                <span :class="['user-type-tag', user.userType?.toLowerCase()]">
                  {{ getUserTypeText(user.userType) }}
                </span>
              </td>
              <td>
                <span :class="['status-tag', getStatusClass(user.userStatus)]">
                  {{ getStatusText(user.userStatus) }}
                </span>
              </td>
              <td>¥{{ formatCurrency(user.balance || 0) }}</td>
              <td>{{ formatTime(user.createdTime) }}</td>
              <td>
                <div class="action-buttons">
                  <!-- 密码重置按钮 -->
                  <button
                    @click="resetUserPassword(user.id)"
                    class="btn-reset"
                    title="重置密码为123456"
                    :disabled="isCurrentUser(user.id)"
                    :title="isCurrentUser(user.id) ? '不能重置自己的密码' : '重置密码为123456'"
                  >
                    🔑
                  </button>
                  <button @click="editUser(user)" class="btn-edit" title="编辑">
                    ✏️
                  </button>
                  <button @click="viewUserDetails(user)" class="btn-view" title="查看详情">
                    👁️
                  </button>
                  <button
                    v-if="user.userStatus === 'PENDING' && user.userType !== 'ADMIN'"
                    @click="approveUser(user.id)"
                    class="btn-approve-small"
                    title="通过审核"
                  >
                    ✅
                  </button>
                  <button
                    v-if="user.userStatus === 'PENDING' && user.userType !== 'ADMIN'"
                    @click="rejectUser(user.id)"
                    class="btn-reject-small"
                    title="拒绝审核"
                  >
                    ❌
                  </button>
                  <button
                    v-if="user.userType !== 'ADMIN'"
                    @click="deleteUser(user.id)"
                    class="btn-delete"
                    title="删除用户"
                    :disabled="isCurrentUser(user.id)"
                    :title="isCurrentUser(user.id) ? '不能删除自己的账户' : '删除用户'"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 添加用户对话框 -->
    <div v-if="showAddDialog" class="modal-overlay" @click.self="showAddDialog = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加用户</h3>
          <button @click="showAddDialog = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitUser" class="user-form">
            <div class="form-row">
              <div class="form-group">
                <label>真实姓名 *</label>
                <input v-model="newUser.username" required placeholder="请输入用户名">
              </div>
              <div class="form-group">
                <label>密码 *</label>
                <input v-model="newUser.password" type="password" required placeholder="请输入密码">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>用户名</label>
                <input v-model="newUser.realName" placeholder="请输入真实姓名">
              </div>
              <div class="form-group">
                <label>手机号</label>
                <input v-model="newUser.phone" placeholder="请输入手机号">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="newUser.email" type="email" placeholder="请输入邮箱">
              </div>
              <div class="form-group">
                <label>用户类型 *</label>
                <select v-model="newUser.userType" required>
                  <option value="STUDENT">学生</option>
                  <option value="TEACHER">教师</option>
                  <option value="STAFF">职工</option>
                  <option value="ADMIN">管理员</option>
                  <option value="EXTERNAL_USER">校外用户</option>
                </select>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" @click="showAddDialog = false" class="btn-cancel">取消</button>
          <button type="submit" @click="submitUser" class="btn-primary">保存</button>
        </div>
      </div>
    </div>

    <!-- 用户详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>用户详情</h3>
          <button @click="closeDetailModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedUser" class="user-details">
            <div class="detail-item">
              <label>用户ID:</label>
              <span>{{ selectedUser.id }}</span>
            </div>
            <div class="detail-item">
              <label>真实姓名:</label>
              <span>{{ selectedUser.username }}</span>
            </div>
            <div class="detail-item">
              <label>用户名:</label>
              <span>{{ selectedUser.realName || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>用户类型:</label>
              <span class="user-type-badge">{{ getUserTypeText(selectedUser.userType) }}</span>
            </div>
            <div class="detail-item">
              <label>手机号:</label>
              <span>{{ selectedUser.phone || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>邮箱:</label>
              <span>{{ selectedUser.email || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>审核状态:</label>
              <span :class="['status-badge', getStatusClass(selectedUser.userStatus)]">
                {{ getStatusText(selectedUser.userStatus) }}
              </span>
            </div>
            <div class="detail-item">
              <label>账户余额:</label>
              <span>¥{{ formatCurrency(selectedUser.balance || 0) }}</span>
            </div>
            <div class="detail-item">
              <label>注册时间:</label>
              <span>{{ formatTime(selectedUser.createdTime) }}</span>
            </div>
            <div class="detail-item">
              <label>更新时间:</label>
              <span>{{ formatTime(selectedUser.updatedTime) }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeDetailModal" class="btn-close">关闭</button>
        </div>
      </div>
    </div>
    <!-- 编辑用户弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑用户信息</h3>
          <button @click="closeEditModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateUser" class="user-form" v-if="editingUser">
            <div class="form-row">
              <div class="form-group">
                <label>真实姓名</label>
                <input v-model="editingUser.username" readonly class="readonly-input">
                <small class="form-hint">真实姓名不可修改</small>
              </div>
              <div class="form-group">
                <label>用户名 *</label>
                <input v-model="editingUser.realName" required placeholder="请输入真实姓名">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>手机号</label>
                <input v-model="editingUser.phone" placeholder="请输入手机号">
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="editingUser.email" type="email" placeholder="请输入邮箱">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>用户类型</label>
                <select v-model="editingUser.userType" :disabled="!isAdminRole">
                  <option value="STUDENT">学生</option>
                  <option value="TEACHER">教师</option>
                  <option value="STAFF">职工</option>
                  <option value="ADMIN">管理员</option>
                  <option value="EXTERNAL_USER">校外用户</option>
                </select>
              </div>
              <div class="form-group">
                <label>审核状态</label>
                <select v-model="editingUser.userStatus" :disabled="!isAdminRole">
                  <option value="PENDING">待审核</option>
                  <option value="APPROVED">已通过</option>
                  <option value="REJECTED">已拒绝</option>
                </select>
              </div>
            </div>
            <div class="form-row" v-if="isAdminRole">
              <div class="form-group">
                <label>账户余额</label>
                <input v-model="editingUser.balance" type="number" step="0.01" placeholder="请输入余额">
                <small class="form-hint">单位：元</small>
              </div>
              <div class="form-group">
                <label>是否需要支付</label>
                <select v-model="editingUser.needPayment">
                  <option :value="true">是</option>
                  <option :value="false">否</option>
                </select>
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button @click="closeEditModal" class="btn-cancel">取消</button>
          <button @click="updateUser" class="btn-primary">保存修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { authFetch } from '@/utils/auth'

// 数据状态
const pendingUsers = ref([])
const allUsers = ref([])
const approvalLoading = ref(false)
const managementLoading = ref(false)
const showAddDialog = ref(false)
const showDetailModal = ref(false)
const selectedUser = ref(null)

// 筛选条件
const searchKeyword = ref('')
const statusFilter = ref('')
const userTypeFilter = ref('')
const approvalFilter = ref('')

// 统计信息
const pendingApprovalCount = ref(0)
const approvedCount = ref(0)
const rejectedCount = ref(0)
const totalUsers = ref(0)

// 新用户表单
const newUser = ref({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  userType: 'STUDENT'
})

// 计算属性：筛选用户
const filteredUsers = computed(() => {
  let filtered = allUsers.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(user =>
      user.username?.toLowerCase().includes(keyword) ||
      user.realName?.toLowerCase().includes(keyword) ||
      user.phone?.includes(keyword)
    )
  }

  if (statusFilter.value) {
    filtered = filtered.filter(user => user.userStatus === statusFilter.value)
  }

  if (userTypeFilter.value) {
    filtered = filtered.filter(user => user.userType === userTypeFilter.value)
  }

  return filtered
})

// 生命周期
onMounted(() => {
  loadPendingUsers()
  loadAllUsers()
})

// 加载待审核用户
const loadPendingUsers = async () => {
  approvalLoading.value = true
  try {
    const response = await authFetch('/api/users/pending')
    console.log('待审核用户响应状态:', response.status)

    if (response.ok) {
      const data = await response.json()
      console.log('待审核用户数据:', data)
      pendingUsers.value = data
      pendingApprovalCount.value = pendingUsers.value.length
      updateApprovalStats()
    } else {
      console.error('加载待审核用户失败，状态码:', response.status)
    }
  } catch (error) {
    console.error('加载待审核用户失败:', error)
  } finally {
    approvalLoading.value = false
  }
}

// 加载所有用户
const loadAllUsers = async () => {
  managementLoading.value = true
  try {
    console.log('开始加载所有用户')

    const response = await authFetch('/api/users')
    console.log('所有用户响应状态:', response.status)

    if (response.ok) {
      const data = await response.json()
      console.log('所有用户数据:', data)
      allUsers.value = data
      totalUsers.value = allUsers.value.length
      updateApprovalStats()
    } else {
      console.error('加载所有用户失败，状态码:', response.status)
      if (response.status === 403) {
        alert('权限不足，无法访问用户管理功能')
      }
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    managementLoading.value = false
  }
}
// 密码重置方法
const resetUserPassword = async (userId) => {
  try {
    const user = allUsers.value.find(u => u.id === userId)
    if (!user) {
      alert('未找到用户信息')
      return
    }

    // 检查是否为当前用户
    if (isCurrentUser(userId)) {
      alert('不能重置自己的密码')
      return
    }

    const confirmMessage = `确定要将用户 "${user.username}" (${user.realName || '未设置姓名'}) 的密码重置为 "123456" 吗？\n\n重置后用户需要使用新密码登录。`

    if (!confirm(confirmMessage)) {
      return
    }

    console.log(`🔑 重置用户密码: ${userId} (${user.username})`)

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/users/${userId}/reset-password`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const result = await response.json()
      console.log('✅ 密码重置成功:', result)

      alert(`✅ 用户 "${user.username}" 的密码已重置为 "123456"\n\n请通知用户使用新密码登录。`)

      // 可选：记录操作日志
      console.log(`📝 管理员重置了用户 ${user.username} 的密码`)

    } else {
      const errorText = await response.text()
      console.error('❌ 密码重置失败:', response.status, errorText)

      if (response.status === 403) {
        alert('❌ 权限不足，需要管理员权限')
      } else if (response.status === 401) {
        alert('❌ 请重新登录')
      } else {
        alert(`❌ 密码重置失败: ${errorText}`)
      }
    }
  } catch (error) {
    console.error('💥 密码重置异常:', error)
    alert('💥 密码重置过程中发生异常，请稍后重试')
  }
}

// 增强版密码重置方法（带更多确认）
const resetUserPasswordEnhanced = async (userId) => {
  const user = allUsers.value.find(u => u.id === userId)
  if (!user) {
    alert('未找到用户信息')
    return
  }

  // 检查是否为当前用户
  if (isCurrentUser(userId)) {
    alert('出于安全考虑，不能重置自己的密码。请使用"修改密码"功能。')
    return
  }

  // 二次确认
  const userInfo = `用户名: ${user.username}\n姓名: ${user.realName || '未设置'}\n用户类型: ${getUserTypeText(user.userType)}`
  const confirmMessage = `确定要重置以下用户的密码吗？\n\n${userInfo}\n\n新密码将设置为: 123456\n\n此操作不可撤销！`

  if (!confirm(confirmMessage)) {
    return
  }

  // 最终确认
  const finalConfirm = confirm('⚠️ 最后确认：确定要重置该用户的密码吗？')
  if (!finalConfirm) {
    return
  }

  try {
    console.log(`🔑 开始重置用户密码: ${userId} (${user.username})`)

    const token = localStorage.getItem('token')
    const response = await fetch(`/api/users/${userId}/reset-password`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const result = await response.json()
      console.log('✅ 密码重置成功:', result)

      // 显示成功消息
      alert(`✅ 密码重置成功！\n\n用户: ${user.username}\n新密码: 123456\n\n请务必通知用户及时修改密码。`)

      // 记录操作
      logAdminAction(`重置用户密码 - ${user.username} (ID: ${user.id})`)

    } else {
      const errorData = await response.json()
      console.error('❌ 密码重置失败:', response.status, errorData)

      if (response.status === 403) {
        alert('❌ 权限不足，需要管理员权限')
      } else if (response.status === 401) {
        alert('❌ 登录已过期，请重新登录')
      } else {
        alert(`❌ 密码重置失败: ${errorData.message || errorData}`)
      }
    }
  } catch (error) {
    console.error('💥 密码重置异常:', error)
    alert('💥 密码重置过程中发生异常，请检查网络连接后重试')
  }
}

// 管理员操作日志
const logAdminAction = (action) => {
  const adminUsername = localStorage.getItem('username') || '未知管理员'
  const timestamp = new Date().toLocaleString('zh-CN')
  console.log(`👮 管理员操作: ${action} | 操作人: ${adminUsername} | 时间: ${timestamp}`)
}

// 检查是否为当前用户
const isCurrentUser = (userId) => {
  const currentUserId = localStorage.getItem('userId')
  return currentUserId && userId.toString() === currentUserId.toString()
}
// 搜索用户
const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    loadAllUsers()
    return
  }

  managementLoading.value = true
  try {
    const response = await authFetch('/api/users')

    if (response.ok) {
      const allData = await response.json()
      // 前端搜索过滤
      const keyword = searchKeyword.value.toLowerCase()
      allUsers.value = allData.filter(user =>
        user.username?.toLowerCase().includes(keyword) ||
        user.realName?.toLowerCase().includes(keyword) ||
        user.phone?.includes(keyword)
      )
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    managementLoading.value = false
  }
}

// 审核通过用户
const approveUser = async (userId) => {
  if (!confirm('确定要通过该用户的注册申请吗？')) return

  try {
    const response = await authFetch(`/api/users/${userId}/approve`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        approved: true,
        reviewComment: '审核通过'
      })
    })

    if (response.ok) {
      showSuccessMessage('用户审核通过！')
      // 重新加载数据
      loadPendingUsers()
      loadAllUsers()
    } else {
      const errorText = await response.text()
      showErrorMessage('操作失败: ' + errorText)
    }
  } catch (error) {
    console.error('审核用户失败:', error)
    showErrorMessage('操作失败，请重试')
  }
}

// 拒绝用户
const rejectUser = async (userId) => {
  if (!confirm('确定要拒绝该用户的注册申请吗？')) return

  try {
    const response = await authFetch(`/api/users/${userId}/approve`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        approved: false,
        reviewComment: '审核未通过'
      })
    })

    if (response.ok) {
      showSuccessMessage('用户审核已拒绝！')
      // 重新加载数据
      loadPendingUsers()
      loadAllUsers()
    } else {
      const errorText = await response.text()
      showErrorMessage('操作失败: ' + errorText)
    }
  } catch (error) {
    console.error('拒绝用户失败:', error)
    showErrorMessage('操作失败，请重试')
  }
}

// 添加用户
// 修改前端的 submitUser 方法
const submitUser = async () => {
  try {
    // 使用注册接口
    const response = await authFetch('/api/users/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(newUser.value)
    })

    if (response.ok) {
      showAddDialog.value = false
      resetNewUser()
      loadAllUsers()
      showSuccessMessage('用户添加成功！')
    } else {
      const errorText = await response.text()
      showErrorMessage('添加失败: ' + errorText)
    }
  } catch (error) {
    console.error('添加用户失败:', error)
    showErrorMessage('添加用户失败')
  }
}

// 删除用户 - 修复版本
const deleteUser = async (userId) => {
  if (!confirm('确定要删除这个用户吗？此操作将同时删除该用户的所有车辆和停车记录！')) {
    return;
  }

  try {
    console.log(`🗑️ 开始删除用户: ${userId}`);

    const response = await authFetch(`/api/users/${userId}`, {
      method: 'DELETE'
    });

    if (response.ok) {
      console.log('✅ 用户删除成功');
      showSuccessMessage('用户删除成功');
      await refreshData();
    } else {
      const errorData = await response.json();
      const errorMessage = errorData.message || '删除失败';
      console.error('❌ 删除用户失败:', errorMessage);

      // 友好的错误提示
      if (errorMessage.includes('正在停车')) {
        showErrorMessage('无法删除：该用户有正在停车的记录，请先结束停车');
      } else if (errorMessage.includes('不能删除自己')) {
        showErrorMessage('不能删除自己的账户');
      } else if (errorMessage.includes('权限不足')) {
        showErrorMessage('权限不足，需要管理员权限');
      } else if (errorMessage.includes('用户不存在')) {
        showErrorMessage('用户不存在');
      } else {
        showErrorMessage('删除失败: ' + errorMessage);
      }
    }
  } catch (error) {
    console.error('💥 删除用户异常:', error);
    showErrorMessage('删除失败，请重试');
  }
};

// 工具函数：显示成功/错误消息
const showSuccessMessage = (message) => {
  alert('✅ ' + message);
};

const showErrorMessage = (message) => {
  alert('❌ ' + message);
};

// 编辑用户
// 添加新的响应式变量
const showEditModal = ref(false)
const editingUser = ref(null)
const isAdminRole = ref(false) // 判断当前用户是否是管理员

// 修改 editUser 方法
const editUser = async (user) => {
  try {
    // 获取用户完整信息
    const response = await authFetch(`/api/users/${user.id}`)
    if (response.ok) {
      const userData = await response.json()
      editingUser.value = { ...userData }

      // 检查当前用户是否是管理员
      const currentUserRole = localStorage.getItem('userType')
      isAdminRole.value = currentUserRole === 'ADMIN'

      showEditModal.value = true
    } else {
      showErrorMessage('获取用户信息失败')
    }
  } catch (error) {
    console.error('编辑用户失败:', error)
    showErrorMessage('编辑用户失败')
  }
}
// 更新用户信息
const updateUser = async () => {
  if (!editingUser.value) return

  try {
    const response = await authFetch(`/api/users/${editingUser.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        realName: editingUser.value.realName,
        phone: editingUser.value.phone,
        email: editingUser.value.email,
        userType: editingUser.value.userType,
        userStatus: editingUser.value.userStatus,
        balance: editingUser.value.balance,
        needPayment: editingUser.value.needPayment
      })
    })

    if (response.ok) {
      showEditModal.value = false
      showSuccessMessage('用户信息更新成功！')
      // 重新加载数据
      loadAllUsers()
      loadPendingUsers()
    } else {
      const errorText = await response.text()
      showErrorMessage('更新失败: ' + errorText)
    }
  } catch (error) {
    console.error('更新用户失败:', error)
    showErrorMessage('更新用户失败')
  }
}
// 关闭编辑弹窗
const closeEditModal = () => {
  showEditModal.value = false
  editingUser.value = null
}
// 查看用户详情
const viewUserDetails = async (user) => {
  try {
    const response = await authFetch(`/api/users/${user.id}`)
    if (response.ok) {
      selectedUser.value = await response.json()
      showDetailModal.value = true
    } else {
      showErrorMessage('获取用户详情失败')
    }
  } catch (error) {
    console.error('查看用户详情失败:', error)
    showErrorMessage('查看用户详情失败')
  }
}

// 关闭详情弹窗
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedUser.value = null
}

// 刷新数据
const refreshData = () => {
  loadPendingUsers()
  loadAllUsers()
  showSuccessMessage('数据已刷新！')
}

// 重置新用户表单
const resetNewUser = () => {
  newUser.value = {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    userType: 'STUDENT'
  }
}

// 更新审核统计
const updateApprovalStats = () => {
  approvedCount.value = allUsers.value.filter(u => u.userStatus === 'APPROVED').length
  rejectedCount.value = allUsers.value.filter(u => u.userStatus === 'REJECTED').length
}

// 工具函数
const getUserAvatar = (userType) => {
  const types = {
    'STUDENT': '🎓',
    'TEACHER': '👨‍🏫',
    'STAFF': '👨‍💼',
    'ADMIN': '👑',
    'EXTERNAL_USER': '👥'
  }
  return types[userType] || '👤'
}

const getUserTypeText = (userType) => {
  const typeMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'STAFF': '职工',
    'ADMIN': '管理员',
    'EXTERNAL_USER': '校外用户'
  }
  return typeMap[userType] || userType
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'DELETED': '已删除'
  }
  return statusMap[status] || status
}

const getStatusClass = (status) => {
  const statusClassMap = {
    'PENDING': 'pending',
    'APPROVED': 'approved',
    'REJECTED': 'rejected',
    'DELETED': 'deleted'
  }
  return statusClassMap[status] || 'unknown'
}

const formatTime = (time) => {
  if (!time) return '未知'
  try {
    return new Date(time).toLocaleString('zh-CN')
  } catch (e) {
    return '无效日期'
  }
}

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}
</script>

<style scoped>
.user-management {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部样式 */
.page-header {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-main h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #1a1a1a;
}

.page-subtitle {
  color: #666;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.refresh-btn, .btn-primary, .btn-approve, .btn-reject, .btn-details {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.refresh-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-approve {
  background: #52c41a;
  color: white;
}

.btn-reject {
  background: #ff4d4f;
  color: white;
}

.btn-details {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

/* 区域样式 */
.approval-section, .management-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge, .total-badge {
  background: #ff4d4f;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.total-badge {
  background: #1890ff;
}

/* 审核统计卡片 */
.approval-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background: #fafafa;
}

.stat-card.pending { border-left: 4px solid #faad14; }
.stat-card.approved { border-left: 4px solid #52c41a; }
.stat-card.rejected { border-left: 4px solid #ff4d4f; }

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1a1a1a;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 用户网格和表格样式 */
.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.approval-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
}

.user-avatar {
  font-size: 32px;
  margin-right: 16px;
}

.user-info {
  flex: 1;
}

.user-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #1a1a1a;
}

.user-username {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

.user-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.user-type, .user-phone {
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.user-reg-time {
  font-size: 12px;
  color: #999;
}

.approval-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 表格样式 */
.users-table-container {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e8e8e8;
}

.users-table th {
  background: #fafafa;
  font-weight: 600;
  color: #1a1a1a;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar-small {
  font-size: 16px;
}

/* 标签样式 */
.user-type-tag, .status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.user-type-tag.student { background: #f6ffed; color: #52c41a; }
.user-type-tag.teacher { background: #fff7e6; color: #fa8c16; }
.user-type-tag.staff { background: #e6f7ff; color: #1890ff; }
.user-type-tag.admin { background: #f9f0ff; color: #722ed1; }
.user-type-tag.external_user { background: #f5f5f5; color: #666; }

.status-tag.pending { background: #fff7e6; color: #fa8c16; }
.status-tag.approved { background: #f6ffed; color: #52c41a; }
.status-tag.rejected { background: #fff2f0; color: #ff4d4f; }
.status-tag.deleted { background: #f5f5f5; color: #999; }

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-edit, .btn-view, .btn-approve-small, .btn-reject-small, .btn-delete {
  padding: 6px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  background: transparent;
}

.btn-edit:hover { background: #e6f7ff; }
.btn-view:hover { background: #f5f5f5; }
.btn-approve-small:hover { background: #f6ffed; }
.btn-reject-small:hover { background: #fff2f0; }
.btn-delete:hover { background: #fff2f0; }
.btn-delete:disabled { opacity: 0.5; cursor: not-allowed; }

/* 搜索和筛选 */
.section-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  width: 250px;
}

.search-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: white;
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
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表单样式 */
.user-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 6px;
  font-weight: 500;
  color: #1a1a1a;
}

.form-group input, .form-group select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

/* 用户详情样式 */
.user-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  font-weight: 500;
  color: #666;
}

.user-type-badge, .status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.user-type-badge {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.pending { background: #fff7e6; color: #fa8c16; }
.status-badge.approved { background: #f6ffed; color: #52c41a; }
.status-badge.rejected { background: #fff2f0; color: #ff4d4f; }

/* 加载和空状态 */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
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

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  color: #666;
}

.empty-state p {
  margin: 0;
  color: #999;
}
/* 密码重置按钮样式 */
.btn-reset {
  background: #faad14;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
  margin-right: 4px;
}

.btn-reset:hover:not(:disabled) {
  background: #ffc53d;
  transform: scale(1.1);
}

.btn-reset:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 操作按钮容器调整 */
.action-buttons {
  display: flex;
  gap: 4px;
  align-items: center;
  flex-wrap: wrap;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
    gap: 2px;
  }

  .btn-reset {
    margin-right: 0;
    margin-bottom: 2px;
  }
}
/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .section-actions {
    flex-direction: column;
    width: 100%;
  }

  .search-box {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .approval-stats {
    grid-template-columns: 1fr;
  }

  .users-grid {
    grid-template-columns: 1fr;
  }

  .approval-card {
    flex-direction: column;
    text-align: center;
  }

  .user-avatar {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .approval-actions {
    flex-direction: row;
    justify-content: center;
    margin-top: 12px;
  }
}
</style>
