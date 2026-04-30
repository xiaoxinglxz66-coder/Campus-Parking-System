<template>
  <div class="profile-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="user-avatar-large">
          <span class="avatar-icon">👤</span>
        </div>
        <div class="user-intro">
          <h1 class="user-name">{{ currentUser.username || '用户' }}</h1>
          <p class="user-role">{{ userTypeText }}</p>
          <div class="user-badges">
            <span class="badge" :class="statusClass">{{ userStatusText }}</span>
            <span class="badge balance">余额: ¥{{ formatBalance(currentUser.balance) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <!-- 信息展示与编辑 -->
      <div class="profile-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">📋</span>
            个人信息
          </h2>
          <button class="btn-edit" @click="toggleEditMode" v-if="!editMode">
            <span class="btn-icon">✏️</span>
            编辑信息
          </button>
          <div class="edit-actions" v-else>
            <button class="btn-secondary" @click="cancelEdit">取消</button>
            <button class="btn-primary" @click="saveProfile" :disabled="!isFormChanged || saving">
              <span v-if="saving">保存中...</span>
              <span v-else>保存修改</span>
            </button>
          </div>
        </div>

        <div class="profile-form">
          <div class="form-grid">
            <!-- 用户名 -->
            <div class="form-group">
              <label class="form-label">用户名</label>
              <div v-if="!editMode" class="form-value">{{ currentUser.username || '未设置' }}</div>
              <input v-else v-model="editForm.username" type="text" class="form-input"
                     :class="{ 'error': validationErrors.username }"
                     placeholder="请输入用户名">
              <div v-if="validationErrors.username" class="error-message">{{ validationErrors.username }}</div>
            </div>

            <!-- 真实姓名 -->
            <div class="form-group">
              <label class="form-label">真实姓名</label>
              <div v-if="!editMode" class="form-value">{{ currentUser.realName || '未设置' }}</div>
              <input v-else v-model="editForm.realName" type="text" class="form-input"
                     placeholder="请输入真实姓名">
            </div>

            <!-- 手机号 -->
            <div class="form-group">
              <label class="form-label">手机号</label>
              <div v-if="!editMode" class="form-value">{{ currentUser.phone || '未设置' }}</div>
              <input v-else v-model="editForm.phone" type="tel" class="form-input"
                     :class="{ 'error': validationErrors.phone }"
                     placeholder="请输入手机号">
              <div v-if="validationErrors.phone" class="error-message">{{ validationErrors.phone }}</div>
            </div>

            <!-- 邮箱 -->
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <div v-if="!editMode" class="form-value">{{ currentUser.email || '未设置' }}</div>
              <input v-else v-model="editForm.email" type="email" class="form-input"
                     :class="{ 'error': validationErrors.email }"
                     placeholder="请输入邮箱">
              <div v-if="validationErrors.email" class="error-message">{{ validationErrors.email }}</div>
            </div>

            <!-- 用户类型 -->
            <div class="form-group">
              <label class="form-label">用户类型</label>
              <div class="form-value type-badge">{{ userTypeText }}</div>
            </div>

            <!-- 注册时间 -->
            <div class="form-group">
              <label class="form-label">注册时间</label>
              <div class="form-value">{{ formatDate(currentUser.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 密码修改部分改为分步流程 -->
      <div class="password-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🔒</span>
            密码修改
          </h2>
          <div class="step-indicator" v-if="passwordStep > 0">
            <span class="step-text">步骤 {{ passwordStep }}/2</span>
            <div class="step-dots">
              <span class="step-dot" :class="{ active: passwordStep >= 1 }"></span>
              <span class="step-dot" :class="{ active: passwordStep >= 2 }"></span>
            </div>
          </div>
        </div>

        <div class="password-form">
          <!-- 步骤1：验证原密码 -->
          <div v-if="passwordStep === 0" class="step-container">
            <div class="step-header">
              <h3 class="step-title">第一步：验证身份</h3>
              <p class="step-description">请输入您的当前密码以继续</p>
            </div>

            <div class="form-group">
              <label class="form-label">当前密码</label>
              <div class="password-input-wrapper">
                <input v-model="passwordForm.oldPassword"
                       :type="showOldPassword ? 'text' : 'password'"
                       class="form-input"
                       placeholder="请输入当前密码"
                       :class="{ 'error': validationErrors.oldPassword }"
                       @keyup.enter="verifyOldPassword">
                <button class="password-toggle" @click="showOldPassword = !showOldPassword">
                  {{ showOldPassword ? '👁️' : '👁️‍🗨️' }}
                </button>
              </div>
              <div v-if="validationErrors.oldPassword" class="error-message">
                {{ validationErrors.oldPassword }}
              </div>
            </div>

            <div class="form-actions">
              <div class="action-buttons">
                <button class="btn-primary"
                        @click="verifyOldPassword"
                        :disabled="!passwordForm.oldPassword || verifyingPassword">
                  <span v-if="verifyingPassword">验证中...</span>
                  <span v-else>验证密码</span>
                </button>
                <button class="btn-secondary" @click="resetPasswordForm">
                  取消
                </button>
                <button type="button"
                        class="btn-forgot"
                        @click="showForgotPasswordModal = true">
                  <span class="btn-icon">🔐</span>
                  忘记密码？
                </button>
              </div>
            </div>
          </div>

          <!-- 步骤2：设置新密码 -->
          <div v-else-if="passwordStep === 1" class="step-container">
            <div class="step-header">
              <h3 class="step-title">第二步：设置新密码</h3>
              <p class="step-description">请设置您的新密码</p>
            </div>

            <div class="form-grid">
              <div class="form-group">
                <label class="form-label">新密码</label>
                <div class="password-input-wrapper">
                  <input v-model="passwordForm.newPassword"
                         :type="showPassword ? 'text' : 'password'"
                         class="form-input"
                         placeholder="请输入新密码"
                         :class="{ 'error': validationErrors.newPassword }">
                  <button class="password-toggle" @click="showPassword = !showPassword">
                    {{ showPassword ? '👁️' : '👁️‍🗨️' }}
                  </button>
                </div>
                <div v-if="validationErrors.newPassword" class="error-message">
                  {{ validationErrors.newPassword }}
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">确认新密码</label>
                <div class="password-input-wrapper">
                  <input v-model="passwordForm.confirmPassword"
                         :type="showConfirmPassword ? 'text' : 'password'"
                         class="form-input"
                         placeholder="请再次输入新密码"
                         :class="{ 'error': validationErrors.confirmPassword }">
                  <button class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                    {{ showConfirmPassword ? '👁️' : '👁️‍🗨️' }}
                  </button>
                </div>
                <div v-if="validationErrors.confirmPassword" class="error-message">
                  {{ validationErrors.confirmPassword }}
                </div>
              </div>
            </div>

            <!-- 密码强度指示器 -->
            <div class="password-strength" v-if="passwordForm.newPassword">
              <div class="strength-header">
                <span class="strength-text">密码强度: {{ passwordStrength }}</span>
                <span class="strength-score">{{ passwordScore }}/5</span>
              </div>
              <div class="strength-bar">
                <div class="strength-fill" :class="strengthClass"></div>
              </div>
              <div class="strength-requirements">
                <p class="requirements-title">密码要求：</p>
                <ul class="requirements-list">
                  <li :class="{ 'met': passwordLengthValid }">
                    <span class="requirement-icon">{{ passwordLengthValid ? '✓' : '○' }}</span>
                    至少8个字符
                  </li>
                  <li :class="{ 'met': hasUpperCase }">
                    <span class="requirement-icon">{{ hasUpperCase ? '✓' : '○' }}</span>
                    包含大写字母
                  </li>
                  <li :class="{ 'met': hasLowerCase }">
                    <span class="requirement-icon">{{ hasLowerCase ? '✓' : '○' }}</span>
                    包含小写字母
                  </li>
                  <li :class="{ 'met': hasNumbers }">
                    <span class="requirement-icon">{{ hasNumbers ? '✓' : '○' }}</span>
                    包含数字
                  </li>
                  <li :class="{ 'met': hasSpecialChar }">
                    <span class="requirement-icon">{{ hasSpecialChar ? '✓' : '○' }}</span>
                    包含特殊字符 (!@#$%等)
                  </li>
                </ul>
              </div>
            </div>

            <div class="form-actions">
              <div class="action-buttons">
                <button class="btn-secondary" @click="goToPreviousStep">
                  <span class="btn-icon">←</span>
                  上一步
                </button>
                <button class="btn-primary"
                        @click="setNewPassword"
                        :disabled="!isNewPasswordValid || changingPassword">
                  <span v-if="changingPassword">设置中...</span>
                  <span v-else>设置新密码</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 步骤3：完成提示 -->
          <div v-else-if="passwordStep === 2" class="step-container success-container">
            <div class="success-icon">✅</div>
            <div class="success-content">
              <h3 class="success-title">密码修改成功！</h3>
              <p class="success-description">
                您的密码已成功修改。为了安全起见，系统将在 <strong>{{ countdown }}</strong> 秒后自动退出登录，请使用新密码重新登录。
              </p>
              <p class="success-tip">
                <span class="tip-icon">💡</span>
                建议：请妥善保管您的新密码，不要与他人共享。
              </p>
            </div>

            <div class="success-actions">
              <button class="btn-primary" @click="logoutNow">
                立即重新登录
              </button>
              <button class="btn-secondary" @click="stayLoggedIn">
                暂时保持登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <div v-if="showForgotPasswordModal" class="forgot-password-modal">
      <div class="modal-overlay" @click="closeForgotPasswordModal"></div>
      <div class="modal-content">
        <div class="modal-header">
          <div class="header-content">
            <span class="header-icon">🔐</span>
            <h3 class="modal-title">通过手机号找回密码</h3>
          </div>
          <button class="close-btn" @click="closeForgotPasswordModal">
            <span class="close-icon">×</span>
          </button>
        </div>

        <div class="modal-body">
          <!-- 步骤1：输入手机号 -->
          <div v-if="forgotPasswordStep === 0" class="step-container">
            <div class="step-header">
              <span class="step-number">1</span>
              <h4 class="step-title">输入手机号</h4>
            </div>

            <div class="step-description">
              <p>请输入您注册时绑定的手机号，验证码将发送到您的手机。</p>
              <div class="user-notice" v-if="currentUser.phone">
                <span class="notice-icon">💡</span>
                <span class="notice-text">系统检测到您的绑定手机号: <strong>{{ maskedCurrentPhone }}</strong></span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">手机号</label>
              <div class="phone-input-wrapper">
                <input
                  v-model="forgotPasswordForm.phone"
                  type="tel"
                  maxlength="11"
                  placeholder="请输入11位手机号"
                  :class="{ 'error': forgotPasswordErrors.phone }"
                  @input="onForgotPhoneInput"
                >
                <button
                  v-if="currentUser.phone"
                  type="button"
                  class="btn-use-existing"
                  @click="useCurrentPhone"
                >
                  <span class="btn-icon">📱</span>
                  使用已绑定手机号
                </button>
              </div>
              <div v-if="forgotPasswordErrors.phone" class="error-message">
                {{ forgotPasswordErrors.phone }}
              </div>
            </div>

            <div class="form-actions">
              <button
                type="button"
                class="btn-primary"
                @click="sendForgotPasswordCode"
                :disabled="!forgotPasswordForm.phone || sendingForgotCode"
              >
                <span v-if="sendingForgotCode">
                  <div class="loading-spinner small"></div>
                  发送中...
                </span>
                <span v-else>发送验证码</span>
              </button>
              <button
                type="button"
                class="btn-secondary"
                @click="closeForgotPasswordModal"
              >
                取消
              </button>
            </div>
          </div>

          <!-- 步骤2：输入验证码和新密码 -->
          <div v-else-if="forgotPasswordStep === 1" class="step-container">
            <div class="step-header">
              <span class="step-number">2</span>
              <h4 class="step-title">设置新密码</h4>
            </div>

            <div class="step-description">
              <p>验证码已发送到 <strong>{{ maskedForgotPhone }}</strong></p>
              <p>请输入验证码并设置新密码</p>
              <div class="code-timer">
                <span v-if="forgotPasswordCodeCountdown > 0">
                  验证码有效期: {{ formatTime(forgotPasswordCodeCountdown) }}
                </span>
                <button
                  v-else
                  type="button"
                  class="btn-resend"
                  @click="resendForgotPasswordCode"
                  :disabled="sendingForgotCode"
                >
                  <span v-if="sendingForgotCode">
                    <div class="loading-spinner small"></div>
                    发送中...
                  </span>
                  <span v-else>重新发送验证码</span>
                </button>
              </div>
            </div>

            <!-- 验证码输入 -->
            <div class="form-group">
              <label class="form-label">验证码</label>
              <div class="verification-code-wrapper">
                <input
                  v-model="forgotPasswordForm.code"
                  type="text"
                  maxlength="6"
                  placeholder="请输入6位验证码"
                  :class="{ 'error': forgotPasswordErrors.code || verificationError }"
                  @input="onForgotVerificationCodeInput"
                  @keyup.enter="verifyForgotPasswordCode"
                >
                <button
                  type="button"
                  class="btn-verify"
                  @click="verifyForgotPasswordCode"
                  :disabled="!forgotPasswordForm.code || verifyingForgotCode"
                >
                  <span v-if="verifyingForgotCode">
                    <div class="loading-spinner small"></div>
                    验证中...
                  </span>
                  <span v-else>验证验证码</span>
                </button>
              </div>
              <div v-if="forgotPasswordErrors.code" class="error-message">
                {{ forgotPasswordErrors.code }}
              </div>
              <div v-if="verificationError" class="error-message">
                {{ verificationError }}
              </div>
              <div v-if="forgotPasswordCodeVerified" class="success-message">
                ✅ 验证码验证成功
              </div>
            </div>

            <!-- 验证成功后显示新密码输入 -->
            <div v-if="forgotPasswordCodeVerified" class="password-fields">
              <!-- 新密码 -->
              <div class="form-group">
                <label class="form-label">新密码</label>
                <div class="password-input-wrapper">
                  <input
                    v-model="forgotPasswordForm.newPassword"
                    :type="showForgotPasswordField ? 'text' : 'password'"
                    placeholder="请输入新密码（至少6位）"
                    :class="{ 'error': forgotPasswordErrors.newPassword }"
                    @input="checkForgotPasswordStrength"
                  >
                  <button
                    type="button"
                    class="password-toggle"
                    @click="showForgotPasswordField = !showForgotPasswordField"
                  >
                    {{ showForgotPasswordField ? '👁️' : '👁️‍🗨️' }}
                  </button>
                </div>
                <div v-if="forgotPasswordErrors.newPassword" class="error-message">
                  {{ forgotPasswordErrors.newPassword }}
                </div>
              </div>

              <!-- 确认密码 -->
              <div class="form-group">
                <label class="form-label">确认新密码</label>
                <div class="password-input-wrapper">
                  <input
                    v-model="forgotPasswordForm.confirmPassword"
                    :type="showForgotConfirmPassword ? 'text' : 'password'"
                    placeholder="请再次输入新密码"
                    :class="{ 'error': forgotPasswordErrors.confirmPassword }"
                  >
                  <button
                    type="button"
                    class="password-toggle"
                    @click="showForgotConfirmPassword = !showForgotConfirmPassword"
                  >
                    {{ showForgotConfirmPassword ? '👁️' : '👁️‍🗨️' }}
                  </button>
                </div>
                <div v-if="forgotPasswordErrors.confirmPassword" class="error-message">
                  {{ forgotPasswordErrors.confirmPassword }}
                </div>
              </div>

              <!-- 密码强度指示器 -->
              <div v-if="forgotPasswordForm.newPassword" class="password-strength">
                <div class="strength-header">
                  <span>密码强度: {{ forgotPasswordStrength }}</span>
                  <span class="strength-score">{{ forgotPasswordScore }}/5</span>
                </div>
                <div class="strength-bar">
                  <div class="strength-fill" :class="forgotPasswordStrengthClass"></div>
                </div>
                <div class="strength-requirements">
                  <p class="requirements-title">密码要求：</p>
                  <ul class="requirements-list">
                    <li :class="{ 'met': forgotPasswordLengthValid }">
                      <span class="requirement-icon">{{ forgotPasswordLengthValid ? '✓' : '○' }}</span>
                      至少8个字符
                    </li>
                    <li :class="{ 'met': forgotPasswordHasUpperCase }">
                      <span class="requirement-icon">{{ forgotPasswordHasUpperCase ? '✓' : '○' }}</span>
                      包含大写字母
                    </li>
                    <li :class="{ 'met': forgotPasswordHasLowerCase }">
                      <span class="requirement-icon">{{ forgotPasswordHasLowerCase ? '✓' : '○' }}</span>
                      包含小写字母
                    </li>
                    <li :class="{ 'met': forgotPasswordHasNumbers }">
                      <span class="requirement-icon">{{ forgotPasswordHasNumbers ? '✓' : '○' }}</span>
                      包含数字
                    </li>
                    <li :class="{ 'met': forgotPasswordHasSpecialChar }">
                      <span class="requirement-icon">{{ forgotPasswordHasSpecialChar ? '✓' : '○' }}</span>
                      包含特殊字符 (!@#$%等)
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <div class="action-group">
                <button
                  type="button"
                  class="btn-secondary"
                  @click="goToPreviousForgotPasswordStep"
                >
                  <span class="btn-icon">←</span>
                  上一步
                </button>
                <button
                  type="button"
                  class="btn-primary"
                  @click="resetForgotPassword"
                  :disabled="!isForgotPasswordFormValid || !forgotPasswordCodeVerified || resettingForgotPassword"
                >
                  <span v-if="resettingForgotPassword">
                    <div class="loading-spinner small"></div>
                    重置中...
                  </span>
                  <span v-else>重置密码</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 步骤3：完成 -->
          <div v-if="forgotPasswordStep === 2" class="step-container success-step">
            <div class="success-icon">✅</div>
            <div class="success-content">
              <h3>密码重置成功！</h3>
              <p>您的密码已成功重置。</p>
              <p class="success-tip">请使用新密码重新登录系统。</p>
            </div>

            <div class="form-actions">
              <button
                type="button"
                class="btn-primary"
                @click="redirectToLoginAfterReset"
              >
                前往登录
              </button>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <div class="footer-info">
            <span class="info-icon">💡</span>
            <span class="info-text">忘记密码功能仅适用于已绑定手机号的用户</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p style="margin-top: 10px;">加载个人资料中...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
console.log('🎉 ProfileView 组件开始加载...')

// 响应式数据
const loading = ref(true)
const editMode = ref(false)
const saving = ref(false)

// 密码修改相关状态
const passwordStep = ref(0)
const verifyingPassword = ref(false)
const changingPassword = ref(false)
const showOldPassword = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const countdown = ref(10)
const countdownTimer = ref(null)

// 忘记密码相关状态
const showForgotPasswordModal = ref(false)
const forgotPasswordStep = ref(0)
const sendingForgotCode = ref(false)
const resettingForgotPassword = ref(false)
const forgotPasswordCodeCountdown = ref(0)
const forgotPasswordCodeVerified = ref(false)
const verifyingForgotCode = ref(false)
const showForgotPasswordField = ref(false)
const showForgotConfirmPassword = ref(false)
const verificationError = ref('')

const currentUser = ref({
  id: '',
  username: '',
  realName: '',
  studentId: '',
  phone: '',
  email: '',
  userType: '',
  userStatus: '',
  balance: 0,
  createdAt: '',
  updatedAt: ''
})

const editForm = reactive({
  username: '',
  realName: '',
  studentId: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const forgotPasswordForm = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validationErrors = reactive({
  username: '',
  phone: '',
  email: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const forgotPasswordErrors = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算属性
const userTypeText = computed(() => {
  const type = currentUser.value.userType
  const map = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'STAFF': '职工',
    'ADMIN': '管理员',
    'EXTERNAL_USER': '校外用户'
  }
  return map[type] || type || '未知'
})

const userStatusText = computed(() => {
  const status = currentUser.value.userStatus
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已审核',
    'REJECTED': '已拒绝',
    'DELETED': '已删除'
  }
  return map[status] || status || '未知'
})

const statusClass = computed(() => {
  const status = currentUser.value.userStatus
  if (!status) return ''
  return status.toLowerCase()
})

const isFormChanged = computed(() => {
  return editForm.username !== currentUser.value.username ||
    editForm.realName !== (currentUser.value.realName || '') ||
    editForm.studentId !== (currentUser.value.studentId || '') ||
    editForm.phone !== (currentUser.value.phone || '') ||
    editForm.email !== (currentUser.value.email || '')
})

// 密码强度计算
const passwordLengthValid = computed(() => passwordForm.newPassword.length >= 8)
const hasUpperCase = computed(() => /[A-Z]/.test(passwordForm.newPassword))
const hasLowerCase = computed(() => /[a-z]/.test(passwordForm.newPassword))
const hasNumbers = computed(() => /[0-9]/.test(passwordForm.newPassword))
const hasSpecialChar = computed(() => /[!@#$%^&*(),.?":{}|<>]/.test(passwordForm.newPassword))

const passwordScore = computed(() => {
  let score = 0
  if (passwordLengthValid.value) score++
  if (hasUpperCase.value) score++
  if (hasLowerCase.value) score++
  if (hasNumbers.value) score++
  if (hasSpecialChar.value) score++
  return score
})

const passwordStrength = computed(() => {
  const score = passwordScore.value
  if (score <= 1) return '很弱'
  if (score === 2) return '弱'
  if (score === 3) return '中等'
  if (score === 4) return '强'
  return '很强'
})

const strengthClass = computed(() => {
  const score = passwordScore.value
  if (score <= 1) return 'very-weak'
  if (score === 2) return 'weak'
  if (score === 3) return 'medium'
  if (score === 4) return 'strong'
  return 'very-strong'
})

const isNewPasswordValid = computed(() => {
  return passwordForm.newPassword &&
    passwordForm.confirmPassword &&
    passwordForm.newPassword === passwordForm.confirmPassword &&
    passwordForm.newPassword.length >= 6 &&
    passwordForm.oldPassword !== passwordForm.newPassword &&
    passwordScore.value >= 2
})

// 忘记密码相关计算属性
const maskedCurrentPhone = computed(() => {
  if (!currentUser.value.phone || currentUser.value.phone.length < 7) return currentUser.value.phone
  return currentUser.value.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

const maskedForgotPhone = computed(() => {
  if (!forgotPasswordForm.phone || forgotPasswordForm.phone.length < 7) return forgotPasswordForm.phone
  return forgotPasswordForm.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

// 忘记密码强度计算
const forgotPasswordLengthValid = computed(() => forgotPasswordForm.newPassword.length >= 8)
const forgotPasswordHasUpperCase = computed(() => /[A-Z]/.test(forgotPasswordForm.newPassword))
const forgotPasswordHasLowerCase = computed(() => /[a-z]/.test(forgotPasswordForm.newPassword))
const forgotPasswordHasNumbers = computed(() => /[0-9]/.test(forgotPasswordForm.newPassword))
const forgotPasswordHasSpecialChar = computed(() => /[!@#$%^&*(),.?":{}|<>]/.test(forgotPasswordForm.newPassword))

const forgotPasswordScore = computed(() => {
  let score = 0
  if (forgotPasswordLengthValid.value) score++
  if (forgotPasswordHasUpperCase.value) score++
  if (forgotPasswordHasLowerCase.value) score++
  if (forgotPasswordHasNumbers.value) score++
  if (forgotPasswordHasSpecialChar.value) score++
  return score
})

const forgotPasswordStrength = computed(() => {
  const score = forgotPasswordScore.value
  if (score <= 1) return '很弱'
  if (score === 2) return '弱'
  if (score === 3) return '中等'
  if (score === 4) return '强'
  return '很强'
})

const forgotPasswordStrengthClass = computed(() => {
  const score = forgotPasswordScore.value
  if (score <= 1) return 'very-weak'
  if (score === 2) return 'weak'
  if (score === 3) return 'medium'
  if (score === 4) return 'strong'
  return 'very-strong'
})

const isForgotPasswordFormValid = computed(() => {
  return forgotPasswordForm.code &&
    forgotPasswordForm.newPassword &&
    forgotPasswordForm.confirmPassword &&
    forgotPasswordForm.newPassword === forgotPasswordForm.confirmPassword &&
    forgotPasswordForm.newPassword.length >= 6 &&
    forgotPasswordScore.value >= 2 &&
    forgotPasswordCodeVerified.value
})

// ========== 个人信息方法 ==========
const loadUserProfile = async () => {
  console.log('🔍 开始加载用户资料...')
  console.log('=== 详细调试信息开始 ===')

  try {
    loading.value = true
    const token = localStorage.getItem('token')

    console.log('1. Token存在:', !!token)

    if (!token) {
      alert('请先登录')
      router.push('/')
      return
    }

    // 🔥 关键修复：从路由参数获取用户ID
    console.log('2. 路由信息:')
    console.log('  路由参数:', route.params)
    console.log('  路由路径:', route.path)
    console.log('  完整路由对象:', route)

    let userId = route.params.id
    console.log('3. 从路由参数获取的userId:', userId)

    // 🔥 紧急修复：如果路由参数为空，尝试其他方式
    if (!userId) {
      console.log('4. 路由参数为空，尝试其他方式...')

      // 先尝试localStorage
      userId = localStorage.getItem('userId')
      console.log('   从localStorage获取的userId:', userId)

      // 再尝试从token解析
      if (!userId) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]))
          console.log('   JWT Payload:', payload)
          if (payload.userId) {
            userId = payload.userId.toString()
            console.log('   从JWT解析的userId:', userId)
          }
        } catch (e) {
          console.error('   解析token失败:', e)
        }
      }

      // 如果找到了userId，重定向到正确的URL
      if (userId) {
        console.log(`🔄 重定向到正确的个人中心: /profile/${userId}`)
        router.replace(`/profile/${userId}`)
        return
      }
    }

    // 🔥 关键检查：确保userId不是1
    if (userId === '1') {
      console.warn('⚠️ 警告：userId是1，尝试获取正确的用户ID')

      // 尝试从localStorage获取正确的ID
      const correctUserId = localStorage.getItem('userId')
      if (correctUserId && correctUserId !== '1') {
        console.log(`🔄 userId为1不正确，使用正确的ID: ${correctUserId}`)
        userId = correctUserId

        // 重定向到正确的URL
        router.replace(`/profile/${correctUserId}`)
        return
      }
    }

    // 最终检查
    if (!userId) {
      console.error('❌ 无法获取用户ID')
      alert('无法获取用户信息，请重新登录')
      localStorage.clear()
      router.push('/')
      return
    }

    console.log(`5. 最终确定的userId: ${userId}`)
    console.log(`6. 将请求API: /api/users/${userId}`)

    // 发送请求
    const response = await fetch(`/api/users/${userId}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    console.log('7. API响应状态:', response.status)
    console.log('8. API响应URL:', response.url)

    if (response.ok) {
      const userData = await response.json()
      console.log('✅ 获取用户信息成功:', userData)
      currentUser.value = userData
      initEditForm()
    } else {
      console.error('❌ 获取用户信息失败，状态码:', response.status)
      const errorText = await response.text()
      console.error('错误信息:', errorText)

      // 详细错误处理
      if (response.status === 404) {
        alert('用户不存在，请联系管理员')
      } else if (response.status === 403) {
        alert('权限不足，无法访问用户信息')
        console.log('🔍 403错误详情:')
        console.log('  请求的用户ID:', userId)
        console.log('  当前localStorage userId:', localStorage.getItem('userId'))
        console.log('  请求URL:', `/api/users/${userId}`)
      } else {
        alert('获取用户信息失败，请稍后重试')
      }
    }
  } catch (error) {
    console.error('❌ 加载用户信息失败:', error)
    console.error('错误堆栈:', error.stack)
    alert('加载个人信息失败，请重试')
  } finally {
    loading.value = false
    console.log('=== 详细调试信息结束 ===')
  }
}

const toggleEditMode = () => {
  editMode.value = true
  clearValidationErrors()
}

const cancelEdit = () => {
  editMode.value = false
  initEditForm()
  clearValidationErrors()
}

const validateForm = () => {
  clearValidationErrors()
  let isValid = true

  if (!editForm.username.trim()) {
    validationErrors.username = '用户名不能为空'
    isValid = false
  } else if (editForm.username.length < 3) {
    validationErrors.username = '用户名至少3个字符'
    isValid = false
  }

  if (editForm.phone && !/^1[3-9]\d{9}$/.test(editForm.phone)) {
    validationErrors.phone = '手机号格式不正确'
    isValid = false
  }

  if (editForm.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email)) {
    validationErrors.email = '邮箱格式不正确'
    isValid = false
  }

  return isValid
}

const saveProfile = async () => {
  if (!validateForm()) return

  try {
    saving.value = true
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')

    const updateData = {
      username: editForm.username.trim(),
      realName: editForm.realName.trim() || null,
      studentId: editForm.studentId.trim() || null,
      phone: editForm.phone.trim() || null,
      email: editForm.email.trim() || null
    }

    console.log('📤 更新用户信息:', updateData)

    const response = await fetch(`/api/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    })

    if (response.ok) {
      const updatedUser = await response.json()
      currentUser.value = updatedUser
      initEditForm()
      editMode.value = false
      alert('个人信息更新成功！')

      console.log('✅ 更新后的 currentUser:', currentUser.value)
      console.log('✅ 更新后的 editForm:', editForm)
    } else {
      const error = await response.text()
      console.error('更新失败:', error)
      throw new Error(error || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    alert(`更新失败: ${error.message}`)
  } finally {
    saving.value = false
  }
}

const initEditForm = () => {
  editForm.username = currentUser.value.username || ''
  editForm.realName = currentUser.value.realName || ''
  editForm.studentId = currentUser.value.studentId || ''
  editForm.phone = currentUser.value.phone || ''
  editForm.email = currentUser.value.email || ''
}

// ========== 密码修改方法 ==========
const verifyOldPassword = async () => {
  if (!passwordForm.oldPassword) {
    validationErrors.oldPassword = '请输入当前密码'
    return
  }

  try {
    verifyingPassword.value = true
    validationErrors.oldPassword = ''

    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')

    console.log('🔐 验证原密码，用户ID:', userId)

    const response = await fetch(`/api/users/${userId}/verify-password`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        password: passwordForm.oldPassword
      })
    })

    console.log('🔐 验证响应状态:', response.status)

    if (response.ok) {
      console.log('🔐 验证成功')
      passwordStep.value = 1
    } else {
      const errorText = await response.text()
      console.error('❌ 验证失败:', errorText)

      if (response.status === 400 || response.status === 401) {
        validationErrors.oldPassword = '密码错误，请重新输入'
        passwordForm.oldPassword = ''
      } else {
        throw new Error(errorText || '验证失败')
      }
    }
  } catch (error) {
    console.error('验证密码失败:', error)
    validationErrors.oldPassword = '验证失败，请重试'
  } finally {
    verifyingPassword.value = false
  }
}

const setNewPassword = async () => {
  if (!validateNewPassword()) return

  if (!confirm('确定要设置新密码吗？设置后需要重新登录')) return

  try {
    changingPassword.value = true

    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')

    console.log('🔐 设置新密码，用户ID:', userId)

    const response = await fetch(`/api/users/${userId}/change-password`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
    })

    console.log('🔐 设置密码响应状态:', response.status)

    if (response.ok) {
      console.log('🔐 密码设置成功')
      passwordStep.value = 2
      startCountdown()
    } else {
      const errorText = await response.text()
      console.error('❌ 设置密码失败:', errorText)

      if (response.status === 400) {
        if (errorText.includes('原密码错误')) {
          validationErrors.oldPassword = '原密码错误，请重新验证'
          passwordStep.value = 0
          passwordForm.oldPassword = ''
        } else if (errorText.includes('新旧密码相同')) {
          validationErrors.newPassword = '新密码不能与原密码相同'
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          alert(errorText || '密码设置失败')
        }
      } else {
        throw new Error(errorText || '密码设置失败')
      }
    }
  } catch (error) {
    console.error('设置密码失败:', error)
    alert(`设置失败: ${error.message}`)
  } finally {
    changingPassword.value = false
  }
}

const validateNewPassword = () => {
  clearValidationErrors()
  let isValid = true

  if (!passwordForm.newPassword) {
    validationErrors.newPassword = '新密码不能为空'
    isValid = false
  } else if (passwordForm.newPassword.length < 6) {
    validationErrors.newPassword = '密码至少6位'
    isValid = false
  } else if (passwordForm.oldPassword === passwordForm.newPassword) {
    validationErrors.newPassword = '新密码不能与原密码相同'
    isValid = false
  } else if (passwordScore.value < 2) {
    validationErrors.newPassword = '密码强度不足，请满足更多要求'
    isValid = false
  }

  if (!passwordForm.confirmPassword) {
    validationErrors.confirmPassword = '请确认密码'
    isValid = false
  } else if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    validationErrors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

const goToPreviousStep = () => {
  if (passwordStep.value > 0) {
    passwordStep.value--

    if (passwordStep.value === 0) {
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  }
}

const resetPasswordForm = () => {
  passwordStep.value = 0
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  clearValidationErrors()
  clearCountdown()
}

// ========== 忘记密码方法 ==========
const closeForgotPasswordModal = () => {
  showForgotPasswordModal.value = false
  resetForgotPasswordForm()
}

const useCurrentPhone = () => {
  if (currentUser.value.phone) {
    forgotPasswordForm.phone = currentUser.value.phone
    forgotPasswordErrors.phone = ''
  }
}

const onForgotPhoneInput = () => {
  forgotPasswordForm.phone = forgotPasswordForm.phone.replace(/\D/g, '')
  forgotPasswordErrors.phone = ''
}

const onForgotVerificationCodeInput = () => {
  forgotPasswordForm.code = forgotPasswordForm.code.replace(/\D/g, '')
  forgotPasswordErrors.code = ''
  verificationError.value = ''

  if (forgotPasswordForm.code.length === 6 && !forgotPasswordCodeVerified.value) {
    verifyForgotPasswordCode()
  }
}

const sendForgotPasswordCode = async () => {
  if (!validateForgotPasswordPhone()) return

  try {
    sendingForgotCode.value = true
    forgotPasswordErrors.phone = ''

    console.log('发送忘记密码验证码...', { phone: forgotPasswordForm.phone })

    const response = await fetch('/api/auth/forgot-password/send-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ phone: forgotPasswordForm.phone })
    })

    console.log('响应状态:', response.status)

    let data
    try {
      data = await response.json()
      console.log('响应数据:', data)
    } catch (e) {
      console.error('解析响应失败:', e)
      forgotPasswordErrors.phone = '服务器响应格式错误'
      return
    }

    if (response.ok && data.success) {
      forgotPasswordStep.value = 1
      forgotPasswordCodeVerified.value = false

      forgotPasswordCodeCountdown.value = 300
      const timer = setInterval(() => {
        forgotPasswordCodeCountdown.value--
        if (forgotPasswordCodeCountdown.value <= 0) {
          clearInterval(timer)
          alert('验证码已过期，请重新获取')
        }
      }, 1000)

      alert('验证码已发送到您的手机，请在5分钟内完成验证')

    } else {
      const errorMsg = data.message || `发送失败 (状态: ${response.status})`
      forgotPasswordErrors.phone = errorMsg

      if (response.status === 403) {
        forgotPasswordErrors.phone = '权限错误：请检查后端安全配置'
      }
    }
  } catch (error) {
    console.error('发送验证码失败:', error)

    if (error.message.includes('Failed to fetch')) {
      forgotPasswordErrors.phone = '无法连接到服务器，请检查后端服务是否运行'
    } else {
      forgotPasswordErrors.phone = '网络错误，请重试'
    }
  } finally {
    sendingForgotCode.value = false
  }
}

const validateForgotPasswordPhone = () => {
  forgotPasswordErrors.phone = ''

  if (!forgotPasswordForm.phone.trim()) {
    forgotPasswordErrors.phone = '手机号不能为空'
    return false
  }

  if (!/^1[3-9]\d{9}$/.test(forgotPasswordForm.phone)) {
    forgotPasswordErrors.phone = '手机号格式不正确'
    return false
  }

  return true
}

const verifyForgotPasswordCode = async () => {
  if (!forgotPasswordForm.code || forgotPasswordForm.code.length !== 6) {
    verificationError.value = '请输入6位验证码'
    return
  }

  try {
    verifyingForgotCode.value = true
    forgotPasswordErrors.code = ''
    verificationError.value = ''

    const response = await fetch('/api/auth/sms/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        phone: forgotPasswordForm.phone,
        code: forgotPasswordForm.code
      })
    })

    let data
    try {
      data = await response.json()
    } catch (jsonError) {
      console.error('解析验证码响应失败:', jsonError)
      verificationError.value = '服务器响应格式错误'
      return
    }

    if (response.ok) {
      forgotPasswordCodeVerified.value = true
      console.log('验证码验证成功')

      if (data.token) {
        localStorage.setItem('temp_token', data.token)
      }
    } else {
      forgotPasswordCodeVerified.value = false
      verificationError.value = data.error || '验证码错误或已过期'
    }
  } catch (error) {
    console.error('验证验证码失败:', error)
    forgotPasswordCodeVerified.value = false

    if (error.message.includes('Failed to fetch')) {
      verificationError.value = '无法连接到服务器'
    } else {
      verificationError.value = '验证失败，请重试'
    }
  } finally {
    verifyingForgotCode.value = false
  }
}

const checkForgotPasswordStrength = () => {
  forgotPasswordErrors.newPassword = ''
}

const resendForgotPasswordCode = () => {
  sendForgotPasswordCode()
}

const goToPreviousForgotPasswordStep = () => {
  if (forgotPasswordStep.value > 0) {
    forgotPasswordStep.value--
    forgotPasswordCodeVerified.value = false

    if (forgotPasswordStep.value === 0) {
      forgotPasswordForm.code = ''
      forgotPasswordForm.newPassword = ''
      forgotPasswordForm.confirmPassword = ''
    }
  }
}

const resetForgotPassword = async () => {
  if (!validateForgotPasswordForm()) {
    console.log('表单验证失败')
    return
  }

  if (!confirm('确定要重置密码吗？重置后请使用新密码登录。')) {
    return
  }

  try {
    resettingForgotPassword.value = true
    console.log('开始重置密码...', {
      phone: forgotPasswordForm.phone,
      codeLength: forgotPasswordForm.code ? forgotPasswordForm.code.length : 0
    })

    const response = await fetch('/api/auth/forgot-password/reset', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        phone: forgotPasswordForm.phone,
        code: forgotPasswordForm.code,
        newPassword: forgotPasswordForm.newPassword
      })
    })

    console.log('重置密码响应状态:', response.status)

    let data
    try {
      data = await response.json()
      console.log('重置密码响应数据:', data)
    } catch (jsonError) {
      console.error('解析JSON失败:', jsonError)
      const text = await response.text()
      alert('服务器返回格式错误: ' + text.substring(0, 100))
      return
    }

    if (response.ok && data.success) {
      forgotPasswordStep.value = 2
      localStorage.removeItem('temp_token')
      alert('密码重置成功！请使用新密码登录。')
    } else {
      const errorMsg = data.message || data.error || `重置失败 (状态: ${response.status})`
      console.error('重置失败:', errorMsg)

      if (response.status === 400 || response.status === 401) {
        if (errorMsg.includes('验证码') || errorMsg.includes('验证码无效') || errorMsg.includes('验证码错误')) {
          forgotPasswordCodeVerified.value = false
          verificationError.value = errorMsg
          forgotPasswordForm.code = ''
          alert('验证码错误，请重新获取验证码')
        } else if (errorMsg.includes('密码') || errorMsg.includes('长度') || errorMsg.includes('相同')) {
          forgotPasswordErrors.newPassword = errorMsg
          forgotPasswordForm.newPassword = ''
          forgotPasswordForm.confirmPassword = ''
          alert('密码设置错误: ' + errorMsg)
        } else if (errorMsg.includes('用户不存在') || errorMsg.includes('手机号')) {
          forgotPasswordErrors.phone = errorMsg
          alert('用户信息错误: ' + errorMsg)
        } else {
          alert('重置失败: ' + errorMsg)
        }
      } else if (response.status === 403) {
        alert('权限不足，请联系管理员')
      } else if (response.status === 404) {
        alert('重置密码接口不存在，请联系管理员')
      } else {
        alert('服务器错误: ' + errorMsg)
      }
    }
  } catch (error) {
    console.error('重置密码失败:', error)

    if (error.message.includes('Failed to fetch')) {
      alert('网络连接失败，请检查：\n1. 后端服务是否运行\n2. 网络连接是否正常')
    } else if (error.name === 'TypeError') {
      alert('请求失败: ' + error.message)
    } else {
      alert('未知错误: ' + error.message)
    }
  } finally {
    resettingForgotPassword.value = false
  }
}

const validateForgotPasswordForm = () => {
  clearForgotPasswordErrors()
  let isValid = true

  if (!forgotPasswordForm.code) {
    forgotPasswordErrors.code = '请输入验证码'
    isValid = false
  } else if (forgotPasswordForm.code.length !== 6) {
    forgotPasswordErrors.code = '验证码为6位数字'
    isValid = false
  } else if (!forgotPasswordCodeVerified.value) {
    forgotPasswordErrors.code = '请先验证验证码'
    isValid = false
  }

  if (!forgotPasswordForm.newPassword) {
    forgotPasswordErrors.newPassword = '请输入新密码'
    isValid = false
  } else if (forgotPasswordForm.newPassword.length < 6) {
    forgotPasswordErrors.newPassword = '密码至少6位'
    isValid = false
  } else if (forgotPasswordScore.value < 2) {
    forgotPasswordErrors.newPassword = '密码强度不足，请满足更多要求'
    isValid = false
  }

  if (!forgotPasswordForm.confirmPassword) {
    forgotPasswordErrors.confirmPassword = '请确认密码'
    isValid = false
  } else if (forgotPasswordForm.newPassword !== forgotPasswordForm.confirmPassword) {
    forgotPasswordErrors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

const clearForgotPasswordErrors = () => {
  forgotPasswordErrors.phone = ''
  forgotPasswordErrors.code = ''
  forgotPasswordErrors.newPassword = ''
  forgotPasswordErrors.confirmPassword = ''
  verificationError.value = ''
}

const resetForgotPasswordForm = () => {
  forgotPasswordStep.value = 0
  Object.keys(forgotPasswordForm).forEach(key => {
    forgotPasswordForm[key] = ''
  })
  clearForgotPasswordErrors()
  forgotPasswordCodeVerified.value = false
  forgotPasswordCodeCountdown.value = 0
  verifyingForgotCode.value = false
  showForgotPasswordField.value = false
  showForgotConfirmPassword.value = false

  if (forgotPasswordCodeCountdown.value > 0) {
    forgotPasswordCodeCountdown.value = 0
  }
}

const redirectToLoginAfterReset = () => {
  closeForgotPasswordModal()
  alert('密码重置成功，请使用新密码重新登录')
  localStorage.clear()
  router.push('/login')
}

// ========== 通用方法 ==========
const startCountdown = () => {
  countdown.value = 10
  countdownTimer.value = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      logoutNow()
    }
  }, 1000)
}

const clearCountdown = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const logoutNow = () => {
  clearCountdown()
  alert('密码已修改，请重新登录')
  localStorage.clear()
  router.push('/')
}

const stayLoggedIn = () => {
  clearCountdown()
  passwordStep.value = 0
  resetPasswordForm()
  alert('密码修改完成，您可以继续使用当前会话')
}

const clearValidationErrors = () => {
  Object.keys(validationErrors).forEach(key => {
    validationErrors[key] = ''
  })
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  try {
    return new Date(dateString).toLocaleString('zh-CN')
  } catch (error) {
    console.error('日期格式化错误:', error)
    return dateString
  }
}

const formatBalance = (balance) => {
  if (balance === undefined || balance === null) return '0.00'
  try {
    return parseFloat(balance).toFixed(2)
  } catch (error) {
    console.error('余额格式化错误:', error)
    return '0.00'
  }
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? '0' : ''}${secs}`
}

// 初始化
onMounted(() => {
  console.log('🚀 ProfileView 组件挂载完成')
  console.log('=== 组件挂载时检查 ===')

  // 打印所有localStorage内容
  console.log('=== localStorage 完整内容 ===')
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    console.log(`${key}: ${localStorage.getItem(key)}`)
  }

  // 检查token和用户ID
  const token = localStorage.getItem('token')
  console.log('Token存在:', !!token)

  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      console.log('🔑 JWT Token Payload:', payload)
      console.log('是否有userId字段:', 'userId' in payload)
      console.log('userId值:', payload.userId)
    } catch (e) {
      console.error('解析token失败:', e)
    }
  }

  loadUserProfile()
})

// 组件卸载时清理
onUnmounted(() => {
  clearCountdown()
  if (forgotPasswordCodeCountdown.value > 0) {
    forgotPasswordCodeCountdown.value = 0
  }
})

// 监听表单变化
watch(() => passwordForm.newPassword, clearValidationErrors)
watch(() => passwordForm.confirmPassword, clearValidationErrors)
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

/* 页面头部 */
.page-header {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 40px;
  color: white;
}

.user-intro {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.user-role {
  color: #666;
  font-size: 16px;
  margin: 0 0 12px 0;
}

.user-badges {
  display: flex;
  gap: 10px;
}

.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.badge.pending { background: #fff7e6; color: #fa8c16; }
.badge.approved { background: #f6ffed; color: #52c41a; }
.badge.rejected { background: #fff2f0; color: #ff4d4f; }
.badge.deleted { background: #f5f5f5; color: #999; }
.badge.balance { background: #e6f7ff; color: #1890ff; }

/* 主要内容 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-section,
.password-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-icon {
  font-size: 24px;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.step-text {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.step-dots {
  display: flex;
  gap: 8px;
}

.step-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #e0e0e0;
  transition: all 0.3s ease;
}

.step-dot.active {
  background: #1890ff;
  transform: scale(1.2);
}

/* 步骤容器 */
.step-container {
  padding: 20px 0;
}

.step-header {
  margin-bottom: 25px;
}

.step-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.step-description {
  color: #666;
  font-size: 14px;
  margin: 0;
}

/* 按钮样式 */
.btn-edit,
.btn-primary,
.btn-secondary,
.btn-forgot {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.btn-edit {
  background: #f0f0f0;
  color: #666;
}

.btn-edit:hover {
  background: #e0e0e0;
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
  background: white;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  background: #f5f5f5;
}

.btn-forgot {
  background: transparent;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.btn-forgot:hover {
  background: #fff7e6;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

/* 表单样式 */
.profile-form,
.password-form {
  padding-top: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px 32px;
}

.form-group {
  position: relative;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.form-value {
  min-height: 32px;
  padding: 6px 12px;
  background: #fafafa;
  border-radius: 4px;
  font-size: 14px;
  color: #1a1a1a;
  line-height: 20px;
  border: 1px solid transparent;
}

.form-value.type-badge {
  display: inline-block;
  background: #f6ffed;
  color: #52c41a;
  padding: 2px 8px;
  border-radius: 4px;
}

.form-input {
  width: 100%;
  min-height: 32px;
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-input.error {
  border-color: #ff4d4f;
}

.form-input.error:focus {
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
}

.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

/* 密码输入包装器 */
.password-input-wrapper {
  position: relative;
}

.password-input-wrapper .password-toggle {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
  font-size: 16px;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover {
  color: #1890ff;
}

.form-actions {
  margin-top: 24px;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.action-buttons .btn-primary {
  flex: 1;
  min-width: 120px;
}

.action-buttons .btn-secondary {
  flex: 1;
  min-width: 120px;
}

.action-buttons .btn-forgot {
  flex: 1;
  min-width: 120px;
  justify-content: center;
}

/* 密码强度指示器 */
.password-strength {
  margin: 25px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px solid #e9ecef;
}

.strength-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.strength-text {
  font-size: 14px;
  font-weight: 500;
  color: #495057;
}

.strength-score {
  font-size: 12px;
  color: #666;
  background: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  border: 1px solid #ddd;
}

.strength-bar {
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-fill.very-weak {
  width: 20%;
  background: #ff4d4f;
}

.strength-fill.weak {
  width: 40%;
  background: #fa8c16;
}

.strength-fill.medium {
  width: 60%;
  background: #1890ff;
}

.strength-fill.strong {
  width: 80%;
  background: #52c41a;
}

.strength-fill.very-strong {
  width: 100%;
  background: #389e0d;
}

.requirements-title {
  font-size: 14px;
  font-weight: 500;
  color: #495057;
  margin: 0 0 10px 0;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirements-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
  transition: all 0.3s ease;
}

.requirements-list li.met {
  color: #52c41a;
}

.requirement-icon {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

/* 完成步骤样式 */
.success-container {
  text-align: center;
  padding: 30px 20px;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.success-description {
  color: #666;
  font-size: 16px;
  line-height: 1.5;
  margin: 0 0 20px 0;
}

.success-description strong {
  color: #1890ff;
  font-weight: 600;
}

.success-tip {
  background: #e6f7ff;
  padding: 12px 16px;
  border-radius: 8px;
  color: #1890ff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 25px;
}

.tip-icon {
  font-size: 18px;
}

.success-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* ========== 忘记密码弹窗样式 ========== */
.forgot-password-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal-content {
  position: relative;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: white;
  font-size: 20px;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.modal-body {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  max-height: calc(90vh - 120px);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  position: relative;
  border-bottom: 2px solid #f0f0f0;
}

.step-header::before {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.step-number {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.step-title {
  margin: 0;
  color: #1a1a1a;
  font-size: 16px;
  font-weight: 600;
}

.step-description {
  margin-bottom: 25px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.step-description p {
  margin: 0 0 8px 0;
}

.step-description p:last-child {
  margin-bottom: 0;
}

.user-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 10px;
  background: linear-gradient(135deg, #e6f7ff, #bae7ff);
  border-radius: 6px;
  border: 1px solid #91d5ff;
}

.notice-icon {
  font-size: 16px;
}

.notice-text {
  font-size: 13px;
  color: #1890ff;
}

/* 手机号输入包装器 */
.phone-input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.phone-input-wrapper input {
  flex: 1;
}

.btn-use-existing {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-use-existing:hover {
  background: linear-gradient(135deg, #389e0d, #237804);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

/* 验证码计时器 */
.code-timer {
  margin-top: 15px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #ffd591;
}

.code-timer span {
  color: #fa8c16;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-resend {
  background: linear-gradient(135deg, #fa8c16, #f5222d);
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-resend:hover:not(:disabled) {
  background: linear-gradient(135deg, #f5222d, #cf1322);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(250, 140, 22, 0.3);
}

.btn-resend:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 验证码输入 */
.verification-code-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.verification-code-wrapper input {
  flex: 1;
  text-align: center;
  letter-spacing: 6px;
  font-size: 18px;
  font-weight: 600;
  padding: 12px;
}

.btn-verify {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 100px;
  justify-content: center;
}

.btn-verify:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
  background: linear-gradient(135deg, #389e0d, #237804);
}

.btn-verify:disabled {
  background: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  opacity: 0.7;
}

.success-message {
  color: #52c41a;
  font-size: 13px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  animation: fadeIn 0.3s ease;
}

/* 密码字段容器 */
.password-fields {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px solid #e9ecef;
}

/* 操作按钮组 */
.action-group {
  display: flex;
  gap: 12px;
  margin-top: 25px;
}

.action-group .btn-primary {
  flex: 1;
}

.action-group .btn-secondary {
  flex: 1;
  max-width: 120px;
}

/* 成功步骤 */
.success-step {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 20px;
  animation: scaleIn 0.6s ease-out;
}

@keyframes scaleIn {
  0% { transform: scale(0); }
  70% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.success-content {
  margin-bottom: 30px;
}

.success-content h3 {
  margin: 0 0 10px 0;
  color: #1a1a1a;
  font-size: 20px;
  font-weight: 600;
}

.success-content p {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

.success-tip {
  margin-top: 15px !important;
  padding: 10px;
  background: #f6ffed;
  border-radius: 6px;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

/* 弹窗底部 */
.modal-footer {
  padding: 16px 30px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
  color: #666;
  font-size: 12px;
}

.info-icon {
  font-size: 14px;
}

.info-text {
  opacity: 0.8;
}

/* 加载动画 */
.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.small {
  width: 14px;
  height: 14px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
  justify-content: center;
  align-items: center;
  z-index: 9999;
  flex-direction: column;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-view {
    padding: 12px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .step-indicator {
    align-items: center;
    margin-top: 10px;
  }

  .edit-actions {
    justify-content: flex-end;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .btn-primary,
  .action-buttons .btn-secondary,
  .action-buttons .btn-forgot {
    width: 100%;
    max-width: none;
  }

  .success-actions {
    flex-direction: column;
  }

  /* 弹窗响应式 */
  .modal-content {
    width: 95%;
    max-height: 95vh;
  }

  .modal-header {
    padding: 20px;
  }

  .modal-body {
    padding: 20px;
    max-height: calc(95vh - 100px);
  }

  .phone-input-wrapper {
    flex-direction: column;
  }

  .btn-use-existing {
    width: 100%;
    justify-content: center;
  }

  .verification-code-wrapper {
    flex-direction: column;
  }

  .verification-code-wrapper input {
    width: 100%;
    letter-spacing: 4px;
    font-size: 16px;
  }

  .btn-verify {
    width: 100%;
  }

  .action-group {
    flex-direction: column;
  }

  .action-group .btn-secondary {
    max-width: none;
  }
}

@media (max-width: 480px) {
  .modal-title {
    font-size: 16px;
  }

  .step-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .step-header::before {
    width: 40px;
  }

  .step-description {
    font-size: 13px;
  }

  .user-notice {
    font-size: 12px;
  }

  .password-fields {
    padding: 15px;
  }
}
</style>
