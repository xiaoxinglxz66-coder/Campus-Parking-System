<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="login-content">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <div class="logo-icon">🚗</div>
            <h1>校园停车管理系统</h1>
          </div>
          <div class="brand-description">
            <h2>智慧停车 · 便捷校园</h2>
            <p>为师生提供高效、智能的停车管理服务，让校园出行更轻松</p>
          </div>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>快速停车预约</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💰</span>
              <span>智能费用计算</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📱</span>
              <span>移动端支持</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🛡️</span>
              <span>安全可靠</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-section">
        <div class="login-card">
          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请选择登录方式继续使用系统</p>
          </div>

          <!-- 登录方式选择 -->
          <div class="login-methods">
            <button
              class="method-btn"
              :class="{ 'active': loginMethod === 'password' }"
              @click="loginMethod = 'password'"
            >
              <span class="method-icon">🔐</span>
              <span class="method-text">账号密码登录</span>
            </button>
            <button
              class="method-btn"
              :class="{ 'active': loginMethod === 'sms' }"
              @click="loginMethod = 'sms'"
            >
              <span class="method-icon">📱</span>
              <span class="method-text">手机号登录</span>
            </button>
          </div>

          <!-- 账号密码登录表单 -->
          <form v-if="loginMethod === 'password'" @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
              <label for="username">用户名</label>
              <input
                id="username"
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                required
                :class="{ 'error': errors.username }"
              />
              <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
            </div>

            <div class="form-group">
              <label for="password">密码</label>
              <input
                id="password"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                required
                :class="{ 'error': errors.password }"
              />
              <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
            </div>

            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading">
                <div class="loading-spinner"></div>
                登录中...
              </span>
              <span v-else>立即登录</span>
            </button>
          </form>

          <!-- 手机号登录表单 -->
          <form v-else @submit.prevent="handleSmsLogin" class="login-form">
            <div class="form-group">
              <label for="phone">手机号</label>
              <input
                id="phone"
                v-model="smsLoginForm.phone"
                type="tel"
                placeholder="请输入手机号"
                required
                maxlength="11"
                :class="{ 'error': errors.phone }"
                @input="onPhoneInput"
              />
              <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
            </div>

            <div class="form-group">
              <label for="smsCode">验证码</label>
              <div class="sms-code-input">
                <input
                  id="smsCode"
                  v-model="smsLoginForm.code"
                  type="text"
                  placeholder="请输入6位验证码"
                  required
                  maxlength="6"
                  :class="{ 'error': errors.smsCode }"
                />
                <button
                  type="button"
                  class="send-code-btn"
                  :disabled="countdown > 0 || sendingCode"
                  @click="sendSmsCode"
                >
                  <span v-if="countdown > 0">{{ countdown }}s后重发</span>
                  <span v-else-if="sendingCode">
                    <div class="loading-spinner small"></div>
                    发送中...
                  </span>
                  <span v-else>获取验证码</span>
                </button>
              </div>
              <span v-if="errors.smsCode" class="error-message">{{ errors.smsCode }}</span>
            </div>

            <!-- 校外用户停车须知 -->
            <div v-if="showExternalNotice" class="notice-section">
              <div class="notice-header">
                <span class="notice-icon">🅿️</span>
                <h3>校外用户停车须知</h3>
              </div>
              <div class="notice-content">
                <div class="notice-item">
                  <strong>收费标准：</strong>
                  <ul>
                    <li>首小时：5元</li>
                    <li>后续每小时：3元</li>
                    <li>24小时封顶：50元</li>
                  </ul>
                </div>
                <div class="notice-item">
                  <strong>注意事项：</strong>
                  <ul>
                    <li>请将车辆停放在指定区域</li>
                    <li>停车前请确认车位状态</li>
                    <li>离场前请完成费用支付</li>
                    <li>请妥善保管停车凭证</li>
                  </ul>
                </div>
              </div>
            </div>

            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading">
                <div class="loading-spinner"></div>
                登录中...
              </span>
              <span v-else>立即登录 / 注册</span>
            </button>

            <div class="login-tips">
              <p>📝 首次使用手机号登录将自动注册为校外用户</p>
            </div>
          </form>
          <!-- 在登录链接区域添加用户指南 -->
          <div class="login-links">
            <span>还没有账户？</span>
            <button type="button" @click="showRegister = true" class="link-btn">
              立即注册
            </button>
            <span class="link-separator">|</span>
            <button type="button" @click="goToGuide" class="link-btn">
              📚 用户指南
            </button>
            <span class="link-separator">|</span>
            <button type="button" @click="showForgotPassword = true" class="link-btn">
              🔑 忘记密码
            </button>
          </div>

          <!-- 用户类型说明 -->
          <div class="user-type-info">
            <h4>用户类型说明</h4>
            <div class="user-types">
              <div class="user-type-item">
                <div class="type-icon admin">👨‍💼</div>
                <div class="type-details">
                  <div class="type-name">管理员</div>
                  <div class="type-desc">系统管理、数据统计</div>
                </div>
              </div>
              <div class="user-type-item">
                <div class="type-icon campus">🎓</div>
                <div class="type-details">
                  <div class="type-name">校内用户</div>
                  <div class="type-desc">免费停车、信息管理</div>
                </div>
              </div>
              <div class="user-type-item">
                <div class="type-icon external">👥</div>
                <div class="type-details">
                  <div class="type-name">校外用户</div>
                  <div class="type-desc">收费停车、在线支付</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册对话框 -->
    <div v-if="showRegister" class="dialog-overlay">
      <div class="dialog">
        <div class="dialog-header">
          <h3>用户注册</h3>
          <button @click="showRegister = false" class="close-btn">×</button>
        </div>
        <form @submit.prevent="handleRegister" class="register-form">
          <div class="form-row">
            <div class="form-group">
              <label>用户名</label>
              <input v-model="registerForm.username" placeholder="请输入用户名" required />
            </div>
            <div class="form-group">
              <label>用户类型</label>
              <select v-model="registerForm.userType">
                <option value="STUDENT">学生</option>
                <option value="TEACHER">教师</option>
                <option value="STAFF">职工</option>
                <option value="EXTERNAL_USER">校外用户</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>密码</label>
              <input v-model="registerForm.password" type="password" placeholder="请输入密码" required />
            </div>
            <div class="form-group">
              <label>确认密码</label>
              <input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" required />
            </div>
          </div>
          <div class="form-group">
            <label>真实姓名</label>
            <input v-model="registerForm.realName" placeholder="请输入真实姓名" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>手机号</label>
              <input v-model="registerForm.phone" placeholder="请输入手机号" />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input v-model="registerForm.email" type="email" placeholder="请输入邮箱" />
            </div>
          </div>
          <div class="dialog-actions">
            <button type="submit" class="btn-primary" :disabled="loading">
              <span v-if="loading">
                <div class="loading-spinner"></div>
                注册中...
              </span>
              <span v-else>立即注册</span>
            </button>
            <button type="button" @click="showRegister = false" class="btn-cancel">
              取消
            </button>
          </div>
        </form>
      </div>
    </div>
    <!-- 在注册对话框后面添加忘记密码对话框 -->
    <div v-if="showForgotPassword" class="dialog-overlay">
      <div class="dialog forgot-password-dialog">
        <div class="dialog-header">
          <h3>通过手机号找回密码</h3>
          <button @click="closeForgotPassword" class="close-btn">×</button>
        </div>

        <div class="forgot-password-content">
          <!-- 步骤1：输入手机号 -->
          <div v-if="forgotStep === 0" class="step-container">
            <div class="step-header">
              <span class="step-number">1</span>
              <h4>输入手机号</h4>
            </div>
            <div class="step-description">
              <p>请输入您注册时绑定的手机号，验证码将发送到您的手机。</p>
            </div>

            <div class="form-group">
              <label>手机号</label>
              <div class="phone-input-wrapper">
                <input v-model="forgotForm.phone"
                       type="tel"
                       maxlength="11"
                       placeholder="请输入11位手机号"
                       :class="{ 'error': forgotErrors.phone }"
                       @input="onForgotPhoneInput">
              </div>
              <div v-if="forgotErrors.phone" class="error-message">
                {{ forgotErrors.phone }}
              </div>
            </div>

            <div class="form-actions">
              <button type="button"
                      class="btn-primary"
                      @click="sendForgotPasswordCode"
                      :disabled="!forgotForm.phone || sendingCode">
                        <span v-if="sendingCode">
                            <div class="loading-spinner small"></div>
                            发送中...
                        </span>
                <span v-else>发送验证码</span>
              </button>
              <button type="button"
                      class="btn-cancel"
                      @click="closeForgotPassword">
                取消
              </button>
            </div>
          </div>

          <!-- 步骤2：输入验证码和新密码 -->
          <div v-else-if="forgotStep === 1" class="step-container">
            <div class="step-header">
              <span class="step-number">2</span>
              <h4>设置新密码</h4>
            </div>

            <div class="step-description">
              <p>验证码已发送到 <strong>{{ maskedPhone }}</strong></p>
              <p>请输入验证码并设置新密码</p>
              <div class="code-timer">
        <span v-if="forgotCodeCountdown > 0">
          验证码有效期: {{ formatTime(forgotCodeCountdown) }}
        </span>
                <button v-else type="button" class="btn-resend" @click="resendForgotCode" :disabled="sendingForgotCode">
                  {{ sendingForgotCode ? '发送中...' : '重新发送' }}
                </button>
              </div>
            </div>

            <!-- 验证码输入部分 -->
            <div class="form-group">
              <label>验证码</label>
              <div class="verification-code-wrapper">
                <input
                  v-model="forgotForm.code"
                  type="text"
                  maxlength="6"
                  placeholder="请输入6位验证码"
                  :class="{ 'error': forgotErrors.code || verificationError }"
                  @input="onVerificationCodeInput"
                  @keyup.enter="verifyCode"
                >
                <button
                  type="button"
                  class="btn-verify"
                  @click="verifyCode"
                  :disabled="!forgotForm.code || verifyingCode"
                >
          <span v-if="verifyingCode">
            <div class="loading-spinner small"></div>
            验证中...
          </span>
                  <span v-else>验证验证码</span>
                </button>
              </div>
              <div v-if="forgotErrors.code" class="error-message">
                {{ forgotErrors.code }}
              </div>
              <div v-if="verificationError" class="error-message">
                {{ verificationError }}
              </div>
              <div v-if="codeVerified" class="success-message">
                ✅ 验证码验证成功
              </div>
            </div>

            <!-- 验证成功后显示新密码输入 -->
            <div v-if="codeVerified">
              <!-- 新密码 -->
              <div class="form-group">
                <label>新密码</label>
                <div class="password-input-wrapper">
                  <input
                    v-model="forgotForm.newPassword"
                    :type="showForgotPasswordField ? 'text' : 'password'"
                    placeholder="请输入新密码（至少6位）"
                    :class="{ 'error': forgotErrors.newPassword }"
                    @input="checkPasswordStrength"
                  >
                  <button type="button" class="password-toggle" @click="showForgotPasswordField = !showForgotPasswordField">
                    {{ showForgotPasswordField ? '👁️' : '👁️‍🗨️' }}
                  </button>
                </div>
                <div v-if="forgotErrors.newPassword" class="error-message">
                  {{ forgotErrors.newPassword }}
                </div>
              </div>

              <!-- 确认密码 -->
              <div class="form-group">
                <label>确认新密码</label>
                <div class="password-input-wrapper">
                  <input
                    v-model="forgotForm.confirmPassword"
                    :type="showForgotConfirmPassword ? 'text' : 'password'"
                    placeholder="请再次输入新密码"
                    :class="{ 'error': forgotErrors.confirmPassword }"
                  >
                  <button type="button" class="password-toggle" @click="showForgotConfirmPassword = !showForgotConfirmPassword">
                    {{ showForgotConfirmPassword ? '👁️' : '👁️‍🗨️' }}
                  </button>
                </div>
                <div v-if="forgotErrors.confirmPassword" class="error-message">
                  {{ forgotErrors.confirmPassword }}
                </div>
              </div>

              <!-- 密码强度指示器 -->
              <div v-if="forgotForm.newPassword" class="password-strength">
                <div class="strength-header">
                  <span>密码强度: {{ passwordStrength }}</span>
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
            </div>

            <div class="form-actions">
              <div class="action-group">
                <button type="button" class="btn-secondary" @click="forgotStep = 0">
                  <span class="btn-icon">←</span>
                  上一步
                </button>
                <button
                  type="button"
                  class="btn-primary"
                  @click="resetPasswordWithCode"
                  :disabled="!isForgotFormValid || !codeVerified || resettingPassword"
                >
          <span v-if="resettingPassword">
            <div class="loading-spinner small"></div>
            重置中...
          </span>
                  <span v-else>重置密码</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 步骤3：完成 -->
          <div v-if="forgotStep === 2" class="step-container success-step">
            <div class="success-icon">✅</div>
            <div class="success-content">
              <h3>密码重置成功！</h3>
              <p>您的密码已成功重置。</p>
              <p class="success-tip">请使用新密码重新登录系统。</p>
            </div>

            <div class="form-actions">
              <button type="button"
                      class="btn-primary"
                      @click="redirectToLogin">
                前往登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loginMethod = ref('password') // 'password' 或 'sms'
const loading = ref(false)
const sendingCode = ref(false)
const showRegister = ref(false)
const countdown = ref(0)
const verifyingCode = ref(false)
const verificationError = ref('')
const codeVerified = ref(false)
// 新增：忘记密码相关状态
const showForgotPassword = ref(false)
const forgotStep = ref(0) // 0:输入手机号, 1:验证码+密码, 2:完成
const sendingForgotCode = ref(false)
const resettingPassword = ref(false)
const forgotCodeCountdown = ref(0)
const showForgotPasswordField = ref(false)
const showForgotConfirmPassword = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})
const onVerificationCodeInput = () => {
  // 只允许输入数字
  forgotForm.code = forgotForm.code.replace(/\D/g, '')
  verificationError.value = ''
  forgotErrors.code = ''

  // 如果输入6位自动验证
  if (forgotForm.code.length === 6 && !codeVerified.value) {
    verifyCode()
  }
}
// 验证验证码
const verifyCode = async () => {
  if (!forgotForm.code || forgotForm.code.length !== 6) {
    verificationError.value = '请输入6位验证码'
    return
  }

  try {
    verifyingCode.value = true
    clearVerificationErrors()

    console.log('验证验证码...', {
      phone: forgotForm.phone,
      code: forgotForm.code
    })

    // 尝试使用现有的短信登录接口来验证验证码
    const response = await fetch('/api/auth/sms/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        phone: forgotForm.phone,
        code: forgotForm.code
      })
    })

    console.log('验证码验证响应状态:', response.status)

    let data
    try {
      data = await response.json()
    } catch (jsonError) {
      console.error('解析验证码响应失败:', jsonError)
      verificationError.value = '服务器响应格式错误'
      return
    }

    if (response.ok) {
      // 验证成功
      codeVerified.value = true
      console.log('验证码验证成功:', data.message)

      // 获取临时token（可选）
      if (data.token) {
        localStorage.setItem('temp_token', data.token)
      }
    } else {
      // 验证失败
      codeVerified.value = false
      verificationError.value = data.error || '验证码错误或已过期'
      console.log('验证码验证失败:', data.error)
    }
  } catch (error) {
    console.error('验证验证码失败:', error)
    codeVerified.value = false

    if (error.message.includes('Failed to fetch')) {
      verificationError.value = '无法连接到服务器'
    } else {
      verificationError.value = '验证失败，请重试'
    }
  } finally {
    verifyingCode.value = false
  }
}
const smsLoginForm = reactive({
  phone: '',
  code: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  userType: 'STUDENT'
})

// 新增：忘记密码表单
const forgotForm = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const errors = reactive({
  username: '',
  password: '',
  phone: '',
  smsCode: ''
})

// 新增：忘记密码错误信息
const forgotErrors = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算属性：是否显示校外用户须知
const showExternalNotice = computed(() => {
  return loginMethod.value === 'sms' && smsLoginForm.phone.length === 11
})

// 新增：密码强度计算属性
const passwordLengthValid = computed(() => forgotForm.newPassword.length >= 8)
const hasUpperCase = computed(() => /[A-Z]/.test(forgotForm.newPassword))
const hasLowerCase = computed(() => /[a-z]/.test(forgotForm.newPassword))
const hasNumbers = computed(() => /[0-9]/.test(forgotForm.newPassword))
const hasSpecialChar = computed(() => /[!@#$%^&*(),.?":{}|<>]/.test(forgotForm.newPassword))

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

// 修改isForgotFormValid计算属性
const isForgotFormValid = computed(() => {
  return forgotForm.code &&
    forgotForm.newPassword &&
    forgotForm.confirmPassword &&
    forgotForm.newPassword === forgotForm.confirmPassword &&
    forgotForm.newPassword.length >= 6 &&
    passwordScore.value >= 2 &&
    codeVerified.value
})

const maskedPhone = computed(() => {
  if (!forgotForm.phone || forgotForm.phone.length < 7) return forgotForm.phone
  return forgotForm.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

const goToGuide = () => {
  router.push('/guide')
}

// 手机号输入处理
const onPhoneInput = () => {
  // 只允许输入数字
  smsLoginForm.phone = smsLoginForm.phone.replace(/\D/g, '')
  errors.phone = ''
}

// 新增：忘记密码手机号输入处理
const onForgotPhoneInput = () => {
  forgotForm.phone = forgotForm.phone.replace(/\D/g, '')
  forgotErrors.phone = ''
}

onMounted(() => {
  // 检查是否已登录
  const token = localStorage.getItem('token')
  if (token) {
    verifyToken(token)
  }
})

// 验证账号密码登录表单
const validateLoginForm = () => {
  let isValid = true
  errors.username = ''
  errors.password = ''

  if (!loginForm.username.trim()) {
    errors.username = '用户名不能为空'
    isValid = false
  }

  if (!loginForm.password) {
    errors.password = '密码不能为空'
    isValid = false
  } else if (loginForm.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    isValid = false
  }

  return isValid
}

// 验证手机号登录表单
const validateSmsLoginForm = () => {
  let isValid = true
  errors.phone = ''
  errors.smsCode = ''

  if (!smsLoginForm.phone.trim()) {
    errors.phone = '手机号不能为空'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(smsLoginForm.phone)) {
    errors.phone = '手机号格式不正确'
    isValid = false
  }

  if (!smsLoginForm.code.trim()) {
    errors.smsCode = '验证码不能为空'
    isValid = false
  } else if (!/^\d{6}$/.test(smsLoginForm.code)) {
    errors.smsCode = '验证码必须是6位数字'
    isValid = false
  }

  return isValid
}

const validateRegisterForm = () => {
  if (registerForm.password !== registerForm.confirmPassword) {
    alert('两次输入的密码不一致')
    return false
  }
  if (registerForm.password.length < 6) {
    alert('密码长度不能少于6位')
    return false
  }
  return true
}

// ========== 忘记密码相关方法 ==========
const validateForgotPhone = () => {
  forgotErrors.phone = ''

  if (!forgotForm.phone.trim()) {
    forgotErrors.phone = '手机号不能为空'
    return false
  }

  if (!/^1[3-9]\d{9}$/.test(forgotForm.phone)) {
    forgotErrors.phone = '手机号格式不正确'
    return false
  }

  return true
}

const validateForgotForm = () => {
  clearForgotErrors() // 现在这个函数已经定义
  let isValid = true

  // 检查验证码
  if (!forgotForm.code) {
    forgotErrors.code = '请输入验证码'
    isValid = false
  } else if (forgotForm.code.length !== 6) {
    forgotErrors.code = '验证码为6位数字'
    isValid = false
  } else if (!codeVerified.value) {
    forgotErrors.code = '请先验证验证码'
    isValid = false
  }

  // 检查新密码
  if (!forgotForm.newPassword) {
    forgotErrors.newPassword = '请输入新密码'
    isValid = false
  } else if (forgotForm.newPassword.length < 6) {
    forgotErrors.newPassword = '密码至少6位'
    isValid = false
  } else if (passwordScore.value < 2) {
    forgotErrors.newPassword = '密码强度不足，请满足更多要求'
    isValid = false
  }

  // 检查确认密码
  if (!forgotForm.confirmPassword) {
    forgotErrors.confirmPassword = '请确认密码'
    isValid = false
  } else if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    forgotErrors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}
const checkPasswordStrength = () => {
  forgotErrors.newPassword = ''
  // 当密码改变时，自动检查强度
}
// 临时修改 sendForgotPasswordCode 方法进行调试
const sendForgotPasswordCode = async () => {
  if (!validateForgotPhone()) return

  try {
    sendingForgotCode.value = true
    forgotErrors.phone = ''

    console.log('发送忘记密码验证码...', { phone: forgotForm.phone })

    const response = await fetch('/api/auth/forgot-password/send-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ phone: forgotForm.phone })
    })

    console.log('响应状态:', response.status)

    let data;
    try {
      data = await response.json()
      console.log('响应数据:', data)
    } catch (e) {
      console.error('解析响应失败:', e)
      forgotErrors.phone = '服务器响应格式错误'
      return
    }

    if (response.ok && data.success) {
      // 显示提示信息
      if (data.userExists === false) {
        alert('提示：如果这是您的校内用户账号，验证码已发送。如果长时间未收到，请确认手机号是否正确。')
      } else if (data.userExists === true) {
        alert('验证码已发送到您的手机，请在5分钟内完成验证')
      }

      // 进入下一步
      forgotStep.value = 1

      // 开始倒计时
      forgotCodeCountdown.value = 300 // 5分钟
      const timer = setInterval(() => {
        forgotCodeCountdown.value--
        if (forgotCodeCountdown.value <= 0) {
          clearInterval(timer)
          alert('验证码已过期，请重新获取')
        }
      }, 1000)

    } else {
      // 显示具体错误信息
      const errorMsg = data.message || `发送失败 (状态: ${response.status})`
      forgotErrors.phone = errorMsg

      // 如果是403错误，提示检查SecurityConfig
      if (response.status === 403) {
        console.error('403错误，请检查SecurityConfig是否允许 /api/auth/forgot-password/** 路径')
        forgotErrors.phone = '权限错误：请检查后端安全配置'
      }
    }
  } catch (error) {
    console.error('发送验证码失败:', error)

    if (error.message.includes('Failed to fetch')) {
      forgotErrors.phone = '无法连接到服务器，请检查后端服务是否运行'
    } else {
      forgotErrors.phone = '网络错误，请重试'
    }
  } finally {
    sendingForgotCode.value = false
  }
}

const resetPasswordWithCode = async () => {
  // 先验证表单
  if (!validateForgotForm()) {
    console.log('表单验证失败')
    return
  }

  if (!confirm('确定要重置密码吗？重置后请使用新密码登录。')) {
    return
  }

  try {
    resettingPassword.value = true
    console.log('开始重置密码...', {
      phone: forgotForm.phone,
      codeLength: forgotForm.code ? forgotForm.code.length : 0,
      passwordLength: forgotForm.newPassword ? forgotForm.newPassword.length : 0
    })

    const response = await fetch('/api/auth/forgot-password/reset', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        phone: forgotForm.phone,
        code: forgotForm.code,
        newPassword: forgotForm.newPassword
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
      console.error('原始响应:', text)
      alert('服务器返回格式错误: ' + text.substring(0, 100))
      return
    }

    if (response.ok && data.success) {
      // 重置成功
      forgotStep.value = 2

      // 清理临时token
      localStorage.removeItem('temp_token')

      // 显示成功消息
      alert('密码重置成功！请使用新密码登录。')

      // 清空表单
      resetForgotForm()

    } else {
      // 改进错误提示
      const errorMsg = data.message || data.error || `重置失败 (状态: ${response.status})`
      console.error('重置失败:', errorMsg)

      // 根据错误类型处理
      if (response.status === 400 || response.status === 401) {
        if (errorMsg.includes('验证码') || errorMsg.includes('验证码无效') || errorMsg.includes('验证码错误')) {
          // 验证码相关错误
          codeVerified.value = false
          verificationError.value = errorMsg
          forgotForm.code = ''
          alert('验证码错误，请重新获取验证码')
        } else if (errorMsg.includes('密码') || errorMsg.includes('长度') || errorMsg.includes('相同')) {
          // 密码相关错误
          forgotErrors.newPassword = errorMsg
          forgotForm.newPassword = ''
          forgotForm.confirmPassword = ''
          alert('密码设置错误: ' + errorMsg)
        } else if (errorMsg.includes('用户不存在') || errorMsg.includes('手机号')) {
          // 用户相关错误
          forgotErrors.phone = errorMsg
          alert('用户信息错误: ' + errorMsg)
        } else {
          // 其他错误
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

    // 根据错误类型显示不同的信息
    if (error.message.includes('Failed to fetch')) {
      alert('网络连接失败，请检查：\n1. 后端服务是否运行\n2. 网络连接是否正常')
    } else if (error.name === 'TypeError') {
      alert('请求失败: ' + error.message)
    } else {
      alert('未知错误: ' + error.message)
    }
  } finally {
    resettingPassword.value = false
  }
}

const resendForgotCode = () => {
  sendForgotPasswordCode()
}
// 修改重置表单方法，重置验证状态
const resetForgotForm = () => {
  forgotStep.value = 0
  Object.keys(forgotForm).forEach(key => {
    forgotForm[key] = ''
  })
  clearForgotErrors()
  codeVerified.value = false
  forgotCodeCountdown.value = 0
  verifyingCode.value = false
  showForgotPasswordField.value = false
  showForgotConfirmPassword.value = false
}

const closeForgotPassword = () => {
  showForgotPassword.value = false
  resetForgotForm()
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? '0' : ''}${secs}`
}

const redirectToLoginFromForgot = () => {
  closeForgotPassword()
  // 清空登录表单
  loginForm.username = ''
  loginForm.password = ''
  errors.username = ''
  errors.password = ''
}

// ========== 原有登录注册方法 ==========
// 账号密码登录
const handleLogin = async () => {
  if (!validateLoginForm()) return

  loading.value = true
  try {
    const response = await fetch('/api/auth/login-simple', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(loginForm)
    })

    const data = await response.json()

    if (response.ok) {
      console.log('✅ 登录成功，响应数据:', data)

      // 保存token和用户信息
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)

      // 🔥 关键修复：从JWT token中解析并保存userId
      try {
        const tokenParts = data.token.split('.')
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1]))
          console.log('🔑 JWT Payload:', payload)

          if (payload.userId) {
            const userId = payload.userId.toString()
            localStorage.setItem('userId', userId)
            console.log('✅ 保存用户ID到localStorage:', userId)
          } else {
            console.warn('⚠️ JWT token中没有userId字段')
            console.log('完整的payload:', payload)
          }
        }
      } catch (e) {
        console.error('❌ 解析token获取userId失败:', e)
      }

      // 🔥 强制刷新页面，确保应用状态完全重置
      console.log('🔄 强制刷新页面，跳转到:', getRoleRedirectUrl(data.role))
      window.location.href = getRoleRedirectUrl(data.role)
    } else {
      if (data.error && data.error.includes('审核')) {
        alert(data.error + ' 请耐心等待管理员审核。')
      } else {
        alert('登录失败: ' + (data.error || '未知错误'))
      }
    }
  } catch (error) {
    console.error('❌ 登录错误:', error)
    alert('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 发送短信验证码
const sendSmsCode = async () => {
  if (!smsLoginForm.phone || !/^1[3-9]\d{9}$/.test(smsLoginForm.phone)) {
    errors.phone = '请输入正确的手机号'
    return
  }

  sendingCode.value = true
  try {
    const response = await fetch('/api/auth/sms/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ phone: smsLoginForm.phone })
    })

    const data = await response.json()

    if (response.ok) {
      alert('验证码已发送，请在控制台查看')
      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      alert('发送失败: ' + data.error)
    }
  } catch (error) {
    console.error('发送验证码错误:', error)
    alert('发送失败，请检查网络连接')
  } finally {
    sendingCode.value = false
  }
}

// 手机号验证码登录
const handleSmsLogin = async () => {
  if (!validateSmsLoginForm()) return

  loading.value = true
  try {
    const response = await fetch('/api/auth/sms/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(smsLoginForm)
    })

    const data = await response.json()

    if (response.ok) {
      console.log('✅ 手机号登录成功，响应数据:', data)

      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      localStorage.setItem('role', data.role)

      if (data.phone) {
        localStorage.setItem('phone', data.phone)
      }

      // 🔥 关键修复：从JWT token中解析并保存userId
      try {
        const tokenParts = data.token.split('.')
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1]))
          console.log('🔑 JWT Payload:', payload)

          if (payload.userId) {
            const userId = payload.userId.toString()
            localStorage.setItem('userId', userId)
            console.log('✅ 保存用户ID到localStorage:', userId)
          } else {
            console.warn('⚠️ JWT token中没有userId字段')
            console.log('完整的payload:', payload)
          }
        }
      } catch (e) {
        console.error('❌ 解析token获取userId失败:', e)
      }

      alert(data.isNewUser ? '注册并登录成功！' : '登录成功！')

      // 🔥 强制刷新页面
      console.log('🔄 强制刷新页面，跳转到 /external-user')
      window.location.href = '/external-user'
    } else {
      alert('登录失败: ' + (data.error || '未知错误'))
    }
  } catch (error) {
    console.error('❌ 手机号登录错误:', error)
    alert('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
// 登录成功后
const handleLoginSuccess = (responseData) => {
  // 使用新的setLoginInfo方法
  auth.setLoginInfo(
    responseData.token,
    responseData.username,
    responseData.role
  )

  // 验证token
  const token = auth.getToken()
  console.log('登录后token验证:')
  console.log('   Token:', token ? token.substring(0, 50) + '...' : '无')
  console.log('   格式验证:', auth.validateTokenFormat(token))
  console.log('   登录状态:', auth.isLoggedIn())

  // 跳转到管理页面
  router.push('/parking-spot-management')
}
// 添加辅助函数：根据角色获取重定向URL
const getRoleRedirectUrl = (role) => {
  switch(role) {
    case 'ADMIN':
      return '/admin-dashboard'
    case 'EXTERNAL_USER':
      return '/external-user'
    default: // STUDENT, TEACHER, STAFF
      return '/campus-user'
  }
}
// 根据用户角色跳转到专属页面
const redirectByRole = (role) => {
  switch(role) {
    case 'ADMIN':
      router.push('/admin-dashboard')
      break
    case 'EXTERNAL_USER':
      router.push('/external-user')
      break
    default: // STUDENT, TEACHER, STAFF 都是校内用户
      router.push('/campus-user')
  }
}
const clearForgotErrors = () => {
  forgotErrors.phone = ''
  forgotErrors.code = ''
  forgotErrors.newPassword = ''
  forgotErrors.confirmPassword = ''
  verificationError.value = ''
}
/**
 * 清除验证码相关的错误信息
 */
const clearVerificationErrors = () => {
  forgotErrors.code = ''
  verificationError.value = ''
}
const handleRegister = async () => {
  if (!validateRegisterForm()) return

  loading.value = true
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(registerForm)
    })

    const data = await response.json()

    if (response.ok) {
      if (data.token) {
        // 审核通过的用户，保存token并跳转
        localStorage.setItem('token', data.token)
        localStorage.setItem('username', data.username)
        localStorage.setItem('role', data.role)

        alert('注册成功！')
        showRegister.value = false
        redirectByRole(data.role.replace('ROLE_', ''))
      } else {
        // 需要审核的用户
        alert('注册成功！请等待管理员审核。审核通过后即可登录。')
        showRegister.value = false
        // 清空表单
        Object.keys(registerForm).forEach(key => {
          registerForm[key] = ''
        })
        registerForm.userType = 'STUDENT'
      }
    } else {
      alert('注册失败: ' + data.error)
    }
  } catch (error) {
    console.error('注册错误:', error)
    alert('注册失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const verifyToken = async (token) => {
  try {
    const response = await fetch('/api/auth/verify', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ token })
    })

    if (response.ok) {
      const data = await response.json()
      // Token有效，根据角色跳转到对应页面
      redirectByRole(data.role.replace('ROLE_', ''))
    } else {
      // Token无效，清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('phone')
    }
  } catch (error) {
    console.error('Token验证失败:', error)
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('phone')
  }
}

// 监听密码变化，清除错误信息
watch(() => forgotForm.newPassword, () => {
  forgotErrors.newPassword = ''
})
watch(() => forgotForm.confirmPassword, () => {
  forgotErrors.confirmPassword = ''
})
watch(() => forgotForm.code, () => {
  forgotErrors.code = ''
})
</script>

<style scoped>

/* 基础样式 */
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ffb6c1 0%, #ffc0cb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}
/* 验证码输入样式 */
.verification-code-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.verification-code-wrapper input {
  flex: 1;
  text-align: center;
  letter-spacing: 8px;
  font-size: 20px;
  font-weight: 600;
  padding: 14px;
}

.btn-verify {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  color: white;
  border: none;
  padding: 14px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 120px;
  justify-content: center;
}

.btn-verify:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
  background: linear-gradient(135deg, #45a049, #3d8b40);
}

.btn-verify:disabled {
  background: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  opacity: 0.7;
}

.success-message {
  color: #4CAF50;
  font-size: 14px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .verification-code-wrapper {
    flex-direction: column;
  }

  .verification-code-wrapper input {
    width: 100%;
    letter-spacing: 6px;
    font-size: 18px;
  }

  .btn-verify {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .verification-code-wrapper input {
    letter-spacing: 4px;
    font-size: 16px;
    padding: 12px;
  }
}
/* 背景装饰元素 */
.background-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -100px;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -50px;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 10%;
}

.shape-4 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  right: 15%;
}

.login-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-height: 700px;
}

/* 左侧品牌区域 */
.brand-section {
  background: rgba(255, 255, 255, 0.2);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.brand-content {
  text-align: center;
  color: white;
}

.brand-logo {
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 4em;
  margin-bottom: 20px;
}

.brand-logo h1 {
  font-size: 2.2em;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.brand-description {
  margin-bottom: 50px;
}

.brand-description h2 {
  font-size: 1.8em;
  font-weight: 600;
  margin-bottom: 15px;
  opacity: 0.9;
}

.brand-description p {
  font-size: 1.1em;
  opacity: 0.8;
  line-height: 1.6;
  max-width: 400px;
  margin: 0 auto;
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  max-width: 400px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-icon {
  font-size: 1.4em;
}

/* 右侧登录区域 */
.login-section {
  background: white;
  padding: 50px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 2em;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.login-header p {
  color: #7f8c8d;
  font-size: 1.1em;
  margin: 0;
}

/* 登录方式选择 */
.login-methods {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  background: #f8f9fa;
  padding: 8px;
  border-radius: 12px;
}

.method-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px;
  border: 2px solid transparent;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 15px;
  color: #7f8c8d;
  font-weight: 500;
}

.method-btn:hover {
  background: white;
  border-color: #e9ecef;
  transform: translateY(-2px);
}

.method-btn.active {
  background: white;
  border-color: #3498db;
  color: #3498db;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.2);
}

.method-icon {
  font-size: 1.3em;
}

/* 表单样式 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95em;
}

.form-group input,
.form-group select {
  padding: 14px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: white;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-group input.error {
  border-color: #e74c3c;
}

.error-message {
  color: #e74c3c;
  font-size: 14px;
  margin-top: 5px;
}

/* 验证码输入样式 */
.sms-code-input {
  display: flex;
  gap: 12px;
}

.sms-code-input input {
  flex: 1;
}

.send-code-btn {
  background: linear-gradient(135deg, #95a5a6, #7f8c8d);
  color: white;
  border: none;
  padding: 14px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 130px;
  white-space: nowrap;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.send-code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(149, 165, 166, 0.3);
}

.send-code-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 停车须知样式 */
.notice-section {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  border: 1px solid #ffeaa7;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 25px;
  animation: slideDown 0.3s ease;
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.notice-icon {
  font-size: 1.4em;
}

.notice-header h3 {
  color: #856404;
  margin: 0;
  font-size: 1.1em;
  font-weight: 600;
}

.notice-content {
  font-size: 0.9em;
  color: #856404;
}

.notice-item {
  margin-bottom: 12px;
}

.notice-item:last-child {
  margin-bottom: 0;
}

.notice-item ul {
  margin: 5px 0 0 0;
  padding-left: 20px;
}

.notice-item li {
  margin: 3px 0;
}

.login-btn {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  padding: 16px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-tips {
  text-align: center;
  margin-top: 15px;
  padding: 12px;
  background: #e8f4fd;
  border-radius: 8px;
  font-size: 0.9em;
  color: #3498db;
  border: 1px solid #d6eaf8;
}

/* 登录链接区域 */
.login-links {
  text-align: center;
  margin-top: 25px;
  color: #7f8c8d;
  font-size: 0.95em;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.link-btn {
  background: none;
  border: none;
  color: #3498db;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
  text-decoration: none;
}

.link-btn:hover {
  background: #edf2f7;
  color: #2980b9;
  text-decoration: underline;
}

.link-separator {
  margin: 0 0.25rem;
  color: #cbd5e0;
  font-weight: 300;
}

/* 用户类型信息样式 */
.user-type-info {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.user-type-info h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1em;
  font-weight: 600;
  text-align: center;
}

.user-types {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-type-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.type-icon {
  font-size: 1.8em;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
}

.type-icon.admin {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.type-icon.campus {
  background: linear-gradient(135deg, #27ae60, #229954);
  color: white;
}

.type-icon.external {
  background: linear-gradient(135deg, #e67e22, #d35400);
  color: white;
}

.type-details {
  flex: 1;
}

.type-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.95em;
  margin-bottom: 2px;
}

.type-desc {
  color: #7f8c8d;
  font-size: 0.85em;
}

/* 加载动画 */
.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.small {
  width: 12px;
  height: 12px;
  border-width: 1.5px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* ==================== 忘记密码弹窗专用样式 ==================== */
.forgot-password-dialog {
  max-width: 480px;
  border-radius: 16px;
  overflow: hidden;
  animation: slideIn 0.3s ease-out;
  background: white;
}

.forgot-password-dialog .dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 25px 30px;
  border-bottom: none;
}

.forgot-password-dialog .dialog-header h3 {
  color: white;
  font-size: 1.3em;
  display: flex;
  align-items: center;
  gap: 10px;
}

.forgot-password-dialog .close-btn {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.forgot-password-dialog .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.forgot-password-content {
  padding: 30px;
  background: #f8f9fa;
  min-height: 300px;
}

/* 步骤容器 */
.step-container {
  animation: fadeIn 0.3s ease-out;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  position: relative;
  border-bottom: 2px solid #e9ecef;
}

.step-header::before {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.step-number {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.step-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2em;
  font-weight: 700;
}

.step-description {
  margin-bottom: 25px;
  color: #5a6c7d;
  font-size: 0.95em;
  line-height: 1.6;
  background: white;
  padding: 18px;
  border-radius: 10px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.step-description p {
  margin: 0 0 10px 0;
}

.step-description p:last-child {
  margin-bottom: 0;
}

.code-timer {
  margin-top: 15px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff7e6, #ffecb3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #ffd54f;
}

.code-timer span {
  color: #e65100;
  font-size: 0.9em;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-resend {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-resend:hover {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

/* 忘记密码表单 */
.forgot-form-group {
  margin-bottom: 20px;
}

.forgot-form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-size: 0.95em;
  font-weight: 600;
}

.forgot-form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e9ecef;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background: white;
  box-sizing: border-box;
}

.forgot-form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.forgot-form-group input.error {
  border-color: #ff5252;
}

.forgot-error-message {
  color: #ff5252;
  font-size: 0.85em;
  margin-top: 6px;
  padding-left: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 密码强度指示器 */
.password-strength {
  margin: 25px 0;
  padding: 20px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.strength-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.strength-text {
  color: #2c3e50;
  font-size: 0.95em;
  font-weight: 600;
}

.strength-score {
  background: #f0f4ff;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9em;
  font-weight: 700;
  color: #667eea;
  border: 1px solid #c5d3ff;
}

.strength-bar {
  height: 10px;
  background: #e9ecef;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 20px;
}

.strength-fill {
  height: 100%;
  transition: all 0.4s ease;
}

.strength-fill.very-weak {
  width: 20%;
  background: linear-gradient(90deg, #ff5252, #ff8a80);
}

.strength-fill.weak {
  width: 40%;
  background: linear-gradient(90deg, #ff9800, #ffb74d);
}

.strength-fill.medium {
  width: 60%;
  background: linear-gradient(90deg, #2196F3, #64b5f6);
}

.strength-fill.strong {
  width: 80%;
  background: linear-gradient(90deg, #4CAF50, #81c784);
}

.strength-fill.very-strong {
  width: 100%;
  background: linear-gradient(90deg, #009688, #4db6ac);
}

.strength-requirements {
  font-size: 0.85em;
  color: #666;
}

.requirements-title {
  margin: 0 0 12px 0;
  font-weight: 600;
  color: #2c3e50;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.requirements-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85em;
}

.requirement-icon {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8em;
  background: #f0f0f0;
  color: #999;
  flex-shrink: 0;
}

.requirements-list li.met .requirement-icon {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  color: white;
}

/* 操作按钮 */
.forgot-actions {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-action-group {
  display: flex;
  gap: 15px;
  width: 100%;
}

.forgot-btn-primary, .forgot-btn-secondary {
  flex: 1;
  padding: 16px 24px;
  border-radius: 10px;
  font-size: 1em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 52px;
}

.forgot-btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.forgot-btn-primary:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.forgot-btn-primary:disabled {
  background: linear-gradient(135deg, #cccccc, #aaaaaa);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  opacity: 0.7;
}

.forgot-btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.forgot-btn-secondary:hover:not(:disabled) {
  background: #f0f4ff;
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.forgot-btn-icon {
  font-size: 1.1em;
}

/* 成功步骤 */
.success-step {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-radius: 10px;
  margin: 20px 0;
}

.success-icon {
  font-size: 80px;
  margin-bottom: 25px;
  animation: scaleIn 0.6s ease-out;
}

.success-content h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.4em;
  font-weight: 700;
}

.success-description {
  color: #5a6c7d;
  font-size: 1em;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.success-description strong {
  color: #667eea;
  font-weight: 700;
}

.success-tip {
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  padding: 15px;
  border-radius: 8px;
  color: #1565c0;
  font-size: 0.9em;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: 25px 0;
  border: 1px solid #90caf9;
}

.tip-icon {
  font-size: 1.3em;
}

.success-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

/* 注册对话框样式 */
.register-form {
  padding: 30px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 25px;
}

.btn-primary {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.btn-cancel {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background: #7f8c8d;
  transform: translateY(-2px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 动画 */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

/* 密码输入包装器 */
.password-input-wrapper {
  position: relative;
}

.password-input-wrapper input {
  padding-right: 50px;
}

.password-toggle {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #7f8c8d;
  font-size: 1.3em;
  cursor: pointer;
  padding: 5px;
  transition: all 0.2s ease;
  border-radius: 4px;
}

.password-toggle:hover {
  color: #3498db;
  background: rgba(52, 152, 219, 0.1);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-content {
    grid-template-columns: 1fr;
    max-width: 500px;
    min-height: auto;
  }

  .brand-section {
    display: none;
  }

  .login-section {
    padding: 40px 30px;
  }
}

@media (max-width: 768px) {
  .login-container {
    padding: 20px;
  }

  .login-content {
    border-radius: 16px;
  }

  .login-section {
    padding: 30px 25px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .feature-list {
    grid-template-columns: 1fr;
  }

  .login-methods {
    flex-direction: column;
  }

  .requirements-list {
    grid-template-columns: 1fr;
  }

  .forgot-password-dialog {
    max-width: 95%;
    margin: 20px;
  }

  .forgot-action-group {
    flex-direction: column;
  }

  .forgot-btn-primary,
  .forgot-btn-secondary {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-section {
    padding: 25px 20px;
  }

  .login-header h2 {
    font-size: 1.6em;
  }

  .dialog {
    width: 95%;
    margin: 20px;
  }

  .register-form {
    padding: 20px;
  }

  .dialog-actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-cancel {
    width: 100%;
  }

  .forgot-password-content {
    padding: 20px;
  }

  .forgot-password-dialog .dialog-header {
    padding: 20px 25px;
  }

  .success-step {
    padding: 30px 15px;
  }

  .success-icon {
    font-size: 60px;
  }
}

/* 移动端优化 */
@media (max-width: 375px) {
  .login-methods {
    padding: 6px;
    gap: 8px;
  }

  .method-btn {
    padding: 12px 8px;
    font-size: 14px;
  }

  .forgot-password-dialog {
    max-width: 100%;
    margin: 10px;
    border-radius: 12px;
  }

  .forgot-password-content {
    padding: 15px;
  }

  .step-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .step-header h4 {
    font-size: 1.1em;
  }
}
</style>
