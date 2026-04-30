<template>
  <div class="parking-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <span class="title-icon">🅿️</span>
            校园停车
          </h1>
          <p class="page-subtitle">选择停车场，快速开始您的停车体验</p>
        </div>
        <div class="header-right">
          <div v-if="userType" class="user-badge">
            <span class="user-icon">👤</span>
            <span class="user-type">{{ getUserTypeText(userType) }}</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 当前停车状态 -->
      <section v-if="currentParkingRecord" class="current-parking-section">
        <div class="section-header">
          <h2>当前停车中</h2>
          <span class="badge-live">LIVE</span>
        </div>

        <div class="current-parking-card">
          <div class="parking-info">
            <div class="info-grid">
              <div class="info-item">
                <span class="label">停车场</span>
                <span class="value">{{ currentParkingRecord.parkingLotName || currentParkingRecord.zone || '未知区域' }}</span>
              </div>
              <div class="info-item">
                <span class="label">车牌号</span>
                <span class="value plate-badge">{{ currentParkingRecord.plateNumber }}</span>
              </div>
              <div class="info-item">
                <span class="label">开始时间</span>
                <span class="value">{{ formatTime(currentParkingRecord.startTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">停车时长</span>
                <span class="value duration">{{ calculateDuration(currentParkingRecord.startTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">停车位</span>
                <span class="value">{{ currentParkingRecord.spotNumber || currentParkingRecord.parkingSpot?.spotNumber || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">状态</span>
                <span class="value">{{ getParkingStatusText(currentParkingRecord.status) }}</span>
              </div>
            </div>

            <div class="action-panel">
              <div class="fee-display" v-if="currentParkingRecord.fee > 0">
                <span class="fee-label">当前费用</span>
                <span class="fee-amount">¥{{ currentParkingRecord.fee.toFixed(2) }}</span>
              </div>
              <div v-else class="free-tag">
                <span class="free-icon">🎓</span>
                <span>校内用户免费停车</span>
              </div>

              <div class="action-buttons">
                <button @click="endParking" class="btn-action btn-primary">
                  <span class="btn-icon">✅</span>
                  结束停车
                </button>
                <button @click="cancelParking" class="btn-action btn-secondary">
                  <span class="btn-icon">❌</span>
                  取消停车
                </button>
              </div>
            </div>
          </div>

          <div class="parking-timer">
            <div class="timer-circle">
              <span class="timer-text">{{ formatTimer(currentParkingRecord.startTime) }}</span>
              <span class="timer-label">已停时间</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 开始新停车 -->
      <section v-else class="new-parking-section">
        <!-- ========== 快捷停车：拍照验证区域 ========== -->
        <div v-if="isCampusUser" class="quick-parking-section">
          <div class="section-header">
            <h2>🚀 快捷停车（拍照验证）</h2>
            <span class="hint-text">通过拍照快速验证已注册车辆，享受免费停车</span>
          </div>

          <div class="quick-parking-card">
            <div class="card-header">
              <div class="card-icon">📸</div>
              <div class="card-title">
                <h4>拍照验证停车</h4>
                <p class="card-subtitle">已注册车辆快速入场</p>
              </div>
            </div>

            <div class="card-body">
              <!-- 1. 拍照上传区域 -->
              <div class="upload-area" @click="triggerCameraUpload" :class="{ 'has-image': campusImagePreview }">
                <input ref="cameraInput" type="file" accept="image/*" @change="handleCampusImageUpload" hidden>

                <div v-if="!campusImagePreview" class="upload-placeholder">
                  <div class="placeholder-icon">
                    <span class="icon">📷</span>
                  </div>
                  <div class="upload-text">
                    <p>点击拍摄车牌</p>
                    <small>仅限已通过审核的校内车辆</small>
                  </div>
                </div>

                <div v-else class="preview-container">
                  <img :src="campusImagePreview" alt="车牌预览" class="preview-image">
                  <button @click.stop="clearCampusImage" class="clear-btn">×</button>
                </div>
              </div>

              <!-- 2. 识别按钮 -->
              <button
                @click="verifyCampusVehicle"
                :disabled="!campusUploadedFile || isVerifying"
                class="verify-btn"
              >
                <span v-if="isVerifying" class="loading-spinner small"></span>
                <span v-else class="btn-icon">🔍</span>
                {{ isVerifying ? '验证中...' : '验证车辆' }}
              </button>

              <!-- 3. 验证结果 -->
              <div v-if="campusVerificationResult" class="verification-result">
                <div class="result-success">
                  <div class="success-icon">✅</div>
                  <div class="success-content">
                    <p class="success-title">车辆验证成功</p>
                    <div class="vehicle-details">
                      <span class="plate-badge">{{ campusVerificationResult.vehicle.plateNumber }}</span>
                      <span class="vehicle-info">
                        {{ campusVerificationResult.vehicle.brand }} ·
                        {{ campusVerificationResult.vehicle.color }}
                      </span>
                    </div>
                    <p class="success-hint">校内用户享受免费停车特权</p>
                  </div>
                </div>

                <!-- 4. 验证成功后的操作 -->
                <div class="verification-success-actions">
                  <p class="success-prompt">车辆验证成功！请继续选择停车场和停车位</p>
                  <div class="action-buttons-horizontal">
                    <button
                      @click="selectVerifiedVehicle"
                      class="btn-continue"
                    >
                      <span class="btn-icon">🚗</span>
                      继续选择停车场
                    </button>
                    <button
                      @click="clearCampusImage"
                      class="btn-change-vehicle"
                    >
                      更换车辆
                    </button>
                  </div>
                </div>
              </div>

              <!-- 5. 错误提示 -->
              <div v-if="campusVerificationError" class="verification-error">
                <div class="error-icon">❌</div>
                <div class="error-content">
                  <p>{{ campusVerificationError }}</p>
                  <button @click="campusVerificationError = ''" class="btn-dismiss">知道了</button>
                </div>
                <!-- 🆕 新增：手动输入降级区域 -->
                <div v-if="campusVerificationError && !campusVerificationResult" class="manual-fallback-section">
                  <div class="fallback-header">
                    <span class="fallback-icon">⌨️</span>
                    <h5>识别失败？请手动输入车牌号</h5>
                  </div>

                  <div class="fallback-input-group">
                    <input
                      v-model="manualPlateNumber"
                      type="text"
                      placeholder="请输入车牌号，如：粤A12345"
                      class="fallback-input"
                      @input="validateManualPlate"
                      :class="{ 'error': manualPlateError }"
                    />
                    <button
                      @click="verifyManualPlate"
                      :disabled="!isManualPlateValid || isVerifyingManual"
                      class="fallback-verify-btn"
                    >
                      <span v-if="isVerifyingManual" class="loading-spinner small"></span>
                      <span v-else class="btn-icon">🔍</span>
                      {{ isVerifyingManual ? '验证中...' : '验证车牌' }}
                    </button>
                  </div>

                  <div v-if="manualPlateError" class="fallback-error">
                    <span class="error-icon small">⚠️</span>
                    <span>{{ manualPlateError }}</span>
                  </div>

                  <div v-if="manualVerificationResult" class="fallback-success">
                    <div class="success-icon">✅</div>
                    <div class="success-content">
                      <p class="success-title">手动验证成功</p>
                      <div class="vehicle-details">
                        <span class="plate-badge">{{ manualVerificationResult.vehicle.plateNumber }}</span>
                        <span class="vehicle-info">
            {{ manualVerificationResult.vehicle.brand }} ·
            {{ manualVerificationResult.vehicle.color }}
          </span>
                      </div>
                      <p class="success-hint">校内用户享受免费停车特权</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="card-footer">
              <p class="footer-note">
                <span class="note-icon">💡</span>
                <span>此功能仅适用于已通过审核的校内注册车辆</span>
              </p>
            </div>
          </div>
        </div>

        <!-- ========== 停车场选择 ========== -->
        <div class="parking-lot-section">
          <div class="section-header">
            <h2>选择停车场</h2>
            <span class="hint-text">点击选择要停车的区域</span>
            <button @click="loadParkingLots" class="btn-refresh">
              <span class="refresh-icon">🔄</span>
              刷新
            </button>
          </div>

          <!-- 加载状态 -->
          <div v-if="loadingLots" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载停车场数据中...</p>
          </div>

          <!-- 停车场列表 -->
          <div v-else class="parking-lot-list">
            <div v-for="lot in parkingLots" :key="lot.zone"
                 :class="['parking-lot-item', { active: selectedParkingLot?.zone === lot.zone }]"
                 @click="selectParkingLot(lot)">
              <div class="lot-icon">
                <span v-if="lot.zone.includes('学生') || lot.zone.includes('活动中心')" class="icon">🎓</span>
                <span v-else-if="lot.zone.includes('食堂')" class="icon">🍴</span>
                <span v-else-if="lot.zone.includes('科大')" class="icon">🏫</span>
                <span v-else class="icon">🅿️</span>
              </div>
              <div class="lot-info">
                <h3 class="lot-name">{{ lot.zone }}</h3>
                <div class="lot-stats">
                  <span class="stat">
                    <span class="stat-icon">🔋</span>
                    <span>{{ lot.availableSpots }}/{{ lot.totalSpots }} 空闲</span>
                  </span>
                  <span class="stat rate">
                    <span class="stat-icon">💰</span>
                    <span>¥{{ getZoneRate(lot.zone) }}/小时</span>
                  </span>
                  <span v-if="isCampusUser && isFreeZone(lot.zone)" class="stat free">
                    <span class="stat-icon">🎓</span>
                    <span>校内免费</span>
                  </span>
                </div>
                <p class="lot-description">{{ getZoneDescription(lot.zone) }}</p>
              </div>
              <div class="lot-status" :class="getLotStatusClass(lot)">
                {{ getLotStatusText(lot) }}
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loadingLots && parkingLots.length === 0" class="empty-state">
            <div class="empty-icon">🅿️</div>
            <h4>暂无可用停车场</h4>
            <p>当前没有可用的停车场，请联系管理员</p>
          </div>
        </div>

        <!-- ========== 停车位选择 ========== -->
        <div v-if="selectedParkingLot && !loadingSpots" class="parking-spot-section">
          <div class="section-header">
            <h2>选择停车位 - {{ selectedParkingLot.zone }}</h2>
            <div class="spot-summary">
              <span class="summary-item">
                <span class="status-dot available"></span>
                <span>空闲 {{ availableSpotsCount }}</span>
              </span>
              <span class="summary-item">
                <span class="status-dot occupied"></span>
                <span>占用 {{ occupiedSpotsCount }}</span>
              </span>
              <span class="summary-item">
                <span class="status-dot maintenance"></span>
                <span>维护 {{ maintenanceSpotsCount }}</span>
              </span>
              <button @click="loadParkingSpots" class="btn-refresh small">
                <span class="refresh-icon">🔄</span>
              </button>
            </div>
          </div>

          <!-- 停车位加载状态 -->
          <div v-if="loadingSpots" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载停车位数据中...</p>
          </div>

          <!-- 停车位网格 -->
          <div v-else class="parking-spot-grid">
            <div v-for="spot in sortedParkingSpots" :key="spot.id"
                 :class="['parking-spot-item', spot.status.toLowerCase(),
                          { selected: selectedSpot?.id === spot.id,
                            disabled: spot.status !== 'AVAILABLE',
                            recommended: isRecommendedSpot(spot) }]"
                 @click="selectSpot(spot)">

              <div class="spot-header">
                <span class="spot-number">{{ spot.spotNumber }}</span>
                <span v-if="spot.spotType && spot.spotType !== 'REGULAR'" class="spot-type-badge">
                  {{ getSpotTypeText(spot.spotType) }}
                </span>
                <span v-if="isRecommendedSpot(spot)" class="recommended-badge">
                  <span class="badge-icon">⭐</span>
                  推荐
                </span>
              </div>

              <div class="spot-status">
                <span class="status-icon">{{ getStatusIcon(spot.status) }}</span>
                <span class="status-text">{{ getSpotStatusText(spot.status) }}</span>
              </div>

              <div class="spot-meta">
                <span v-if="spot.hourlyRate > 0 && !isCampusUser" class="spot-rate">
                  ¥{{ spot.hourlyRate }}/小时
                </span>
                <span v-else-if="isCampusUser && isFreeZone(spot.zone)" class="spot-free">校内免费</span>
                <span v-else class="spot-rate">¥{{ spot.hourlyRate || 5 }}/小时</span>

                <span v-if="spot.zone" class="spot-area">{{ spot.zone }}</span>
              </div>

              <!-- 车位状态标签 -->
              <div v-if="spot.status !== 'AVAILABLE'" class="spot-status-overlay">
                {{ getSpotStatusText(spot.status) }}
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loadingSpots && parkingSpots.length === 0" class="empty-state">
            <div class="empty-icon">🚗</div>
            <h4>该停车场暂无停车位</h4>
            <p>请联系管理员添加停车位</p>
            <button @click="selectedParkingLot = null" class="btn-change-lot">
              选择其他停车场
            </button>
          </div>

          <!-- 没有空闲车位 -->
          <div v-if="!loadingSpots && availableSpotsCount === 0 && parkingSpots.length > 0" class="no-available-spots">
            <div class="no-spots-icon">🚫</div>
            <h4>当前没有可用停车位</h4>
            <p>该停车场所有车位都被占用，请稍后再试或选择其他停车场</p>
            <button @click="selectedParkingLot = null" class="btn-change-lot">
              选择其他停车场
            </button>
          </div>
        </div>

        <!-- ========== 车辆选择 ========== -->
        <div v-if="selectedSpot && selectedParkingLot" class="vehicle-section">
          <div class="section-header">
            <h2>选择车辆</h2>
            <div class="section-actions">
              <button @click="refreshVehicles" class="btn-refresh">
                <span class="refresh-icon">🔄</span>
                刷新车辆列表
              </button>
              <router-link to="/vehicle-management" class="btn-add-vehicle">
                <span class="btn-icon">➕</span>
                添加新车辆
              </router-link>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="loadingVehicles" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载车辆数据中...</p>
          </div>

          <!-- 车辆列表 -->
          <div v-else class="vehicle-list">
            <!-- 如果通过拍照验证选择了车辆，显示已选车辆 -->
            <div v-if="selectedVehicleFromVerification" class="verified-vehicle-notice">
              <div class="notice-icon">✅</div>
              <div class="notice-content">
                <h4>已通过拍照验证选择车辆</h4>
                <div class="vehicle-display">
                  <span class="plate-badge large">{{ selectedVehicleFromVerification.plateNumber }}</span>
                  <span class="vehicle-details-text">
                    {{ selectedVehicleFromVerification.brand || '未知品牌' }} ·
                    {{ selectedVehicleFromVerification.color || '未知颜色' }}
                  </span>
                </div>
                <button @click="selectedVehicleFromVerification = null" class="btn-change-vehicle">
                  更换车辆
                </button>
              </div>
            </div>

            <!-- 其他可用车辆 -->
            <div v-if="userApprovedVehicles.length > 0 && !selectedVehicleFromVerification" class="available-vehicles">
              <h4 class="sub-section-title">我的其他车辆</h4>
              <div class="vehicle-grid">
                <div v-for="vehicle in userApprovedVehicles" :key="vehicle.id"
                     :class="['vehicle-item', { active: selectedVehicle?.id === vehicle.id }]"
                     @click="selectVehicle(vehicle)">
                  <div class="vehicle-header">
                    <span class="plate-badge">{{ vehicle.plateNumber }}</span>
                    <span class="vehicle-status approved">已审核</span>
                  </div>
                  <div class="vehicle-details">
                    <div class="vehicle-meta">
                      <span class="vehicle-brand">{{ vehicle.brand || '未知品牌' }}</span>
                      <span class="vehicle-color">{{ vehicle.color || '未知颜色' }}</span>
                      <span class="vehicle-type">{{ getVehicleTypeText(vehicle.vehicleType) }}</span>
                    </div>
                    <div class="vehicle-date">
                      注册时间: {{ formatDate(vehicle.createdAt) }}
                    </div>
                  </div>
                  <div class="vehicle-select">
                    <div class="select-indicator">
                      {{ selectedVehicle?.id === vehicle.id ? '✓ 已选择' : '选择此车' }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!loadingVehicles && userApprovedVehicles.length === 0 && !selectedVehicleFromVerification" class="empty-vehicles">
              <div class="empty-icon">🚗</div>
              <h4>暂无可用车辆</h4>
              <p>您需要先添加车辆并通过审核才能停车</p>
              <router-link to="/vehicle-management" class="btn-add-vehicle">
                <span class="btn-icon">➕</span>
                去添加车辆
              </router-link>
            </div>
          </div>
        </div>

        <!-- ========== 开始停车确认 ========== -->
        <div v-if="selectedSpot && selectedParkingLot && (selectedVehicle || selectedVehicleFromVerification)" class="start-parking-section">
          <div class="confirmation-card">
            <div class="confirmation-header">
              <h3>确认停车信息</h3>
              <span class="confirmation-badge">即将开始</span>
            </div>

            <div class="confirmation-details">
              <div class="detail-row">
                <div class="detail-item">
                  <span class="detail-label">停车场</span>
                  <span class="detail-value">{{ selectedParkingLot.zone }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">停车位</span>
                  <span class="detail-value spot-highlight">{{ selectedSpot.spotNumber }}</span>
                </div>
              </div>
              <div class="detail-row">
                <div class="detail-item">
                  <span class="detail-label">车牌号</span>
                  <span class="detail-value plate-highlight">
                    {{ (selectedVehicle || selectedVehicleFromVerification).plateNumber }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">停车类型</span>
                  <span class="detail-value type-highlight">
                    {{ isCampusUser ? '校内免费停车' : '校外收费停车' }}
                  </span>
                </div>
              </div>
              <div class="detail-row">
                <div class="detail-item">
                  <span class="detail-label">开始时间</span>
                  <span class="detail-value">{{ new Date().toLocaleString('zh-CN') }}</span>
                </div>
                <div class="detail-item" v-if="!isCampusUser">
                  <span class="detail-label">预估费率</span>
                  <span class="detail-value">¥{{ selectedSpot.hourlyRate || 5 }}/小时</span>
                </div>
              </div>
            </div>

            <div class="confirmation-notice">
              <div class="notice-icon">ℹ️</div>
              <div class="notice-content">
                <p v-if="isCampusUser && isFreeZone(selectedParkingLot.zone)">
                  <strong>校内福利：</strong>您将享受免费停车服务，请遵守停车场管理规定。
                </p>
                <p v-else-if="isCampusUser">
                  <strong>说明：</strong>您为校内用户，停车场内免费停车。
                </p>
                <p v-else>
                  <strong>收费说明：</strong>停车费为¥{{ selectedSpot.hourlyRate || 5 }}/小时。
                </p>
                <p class="small-text">
                  点击"开始停车"即表示您同意停车场使用条款
                </p>
              </div>
            </div>

            <div class="confirmation-actions">
              <button @click="clearSelection" class="btn-cancel">
                重新选择
              </button>
              <button
                @click="startParking"
                class="btn-confirm"
                :disabled="isStartingParking || !canStartParking"
              >
                <span v-if="isStartingParking" class="loading-spinner"></span>
                <span v-else class="confirm-icon">🚗</span>
                {{ isStartingParking ? '停车中...' : '开始停车' }}
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 全局加载状态 -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="error-alert">
        <div class="alert-icon">⚠️</div>
        <div class="alert-content">
          <h4>操作失败</h4>
          <p>{{ error }}</p>
          <button @click="error = ''" class="btn-dismiss">关闭</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authFetch } from '@/utils/auth'
import { getAvailableSpots } from '@/utils/api'

const router = useRouter()

// 响应式数据
const userType = ref('STUDENT')
const isCampusUser = computed(() => {
  return userType.value === 'STUDENT' ||
    userType.value === 'TEACHER' ||
    userType.value === 'STAFF' ||
    userType.value === 'ADMIN'
})

// 停车场数据
const parkingLots = ref([])
const loadingLots = ref(false)

// 停车位数据
const parkingSpots = ref([])
const loadingSpots = ref(false)

// 车辆数据
const vehicles = ref([])
const loadingVehicles = ref(false)

// 选择状态
const selectedParkingLot = ref(null)
const selectedSpot = ref(null)
const selectedVehicle = ref(null)
const selectedVehicleFromVerification = ref(null)

// 当前停车记录
const currentParkingRecord = ref(null)

// 状态管理
const loading = ref(false)
const error = ref('')
const isStartingParking = ref(false)

// ========== 拍照验证相关状态 ==========
const campusUploadedFile = ref(null)
const campusImagePreview = ref('')
const campusVerificationResult = ref(null)
const campusVerificationError = ref('')
const isVerifying = ref(false)
const cameraInput = ref(null)
// 🆕 新增状态变量
const manualPlateNumber = ref('')               // 手动输入的车牌号
const manualPlateError = ref('')                // 手动输入验证错误
const isVerifyingManual = ref(false)            // 手动验证状态
const manualVerificationResult = ref(null)      // 手动验证结果
// 计算属性
const userApprovedVehicles = computed(() => {
  return vehicles.value.filter(vehicle => vehicle.status === 'APPROVED' && !vehicle.isTemporary)
})

const availableParkingSpots = computed(() => {
  if (!selectedParkingLot.value) return []
  return parkingSpots.value.filter(spot => spot.status === 'AVAILABLE')
})

const sortedParkingSpots = computed(() => {
  const spots = [...parkingSpots.value]
  return spots.sort((a, b) => {
    const numA = parseInt(a.spotNumber?.match(/\d+/)?.[0] || 0)
    const numB = parseInt(b.spotNumber?.match(/\d+/)?.[0] || 0)
    return numA - numB
  })
})

const availableSpotsCount = computed(() => availableParkingSpots.value.length)

const occupiedSpotsCount = computed(() => {
  if (!selectedParkingLot.value) return 0
  return parkingSpots.value.filter(spot => spot.status === 'OCCUPIED').length
})

const maintenanceSpotsCount = computed(() => {
  if (!selectedParkingLot.value) return 0
  return parkingSpots.value.filter(spot => spot.status === 'MAINTENANCE').length
})

const canStartParking = computed(() => {
  return selectedSpot.value && selectedParkingLot.value &&
    (selectedVehicle.value || selectedVehicleFromVerification.value) &&
    !isStartingParking.value
})

// ========== 工具函数 ==========
const getUserTypeText = (userType) => {
  const typeMap = {
    'EXTERNAL_USER': '校外用户',
    'STUDENT': '学生',
    'TEACHER': '教师',
    'STAFF': '职工',
    'ADMIN': '管理员'
  }
  return typeMap[userType] || userType || '未知用户类型'
}

const getZoneDescription = (zone) => {
  const descriptions = {
    '学生活动中心': '靠近教学楼和图书馆，24小时免费停车',
    '一食堂停车区': '靠近食堂，用餐时间段较为繁忙',
    '科大讯飞楼': '教学办公区域，停车位充足',
    '图书馆停车场': '学术区域，安静安全',
    '体育馆停车场': '运动健身区域，车位充足',
    '南硅谷A': '学生宿舍区域',
    '创业园': '创新创业基地',
    '演艺中心': '文艺表演场所',
    '美食街': '餐饮娱乐区域'
  }
  return descriptions[zone] || '校园停车区域'
}

const getZoneRate = (zone) => {
  // 校内用户在特定区域免费
  if (isCampusUser.value && isFreeZone(zone)) {
    return 0
  }
  return 5 // 默认5元/小时
}

const isFreeZone = (zone) => {
  // 校内用户在以下区域免费
  const freeZones = ['学生活动中心', '图书馆停车场']
  return freeZones.includes(zone)
}

const getLotStatusClass = (lot) => {
  const available = lot.availableSpots || 0
  const total = lot.totalSpots || 1
  const ratio = available / total

  if (available === 0) return 'status-low'
  if (ratio > 0.5) return 'status-high'
  if (ratio > 0.2) return 'status-medium'
  return 'status-low'
}

const getLotStatusText = (lot) => {
  const available = lot.availableSpots || 0
  const total = lot.totalSpots || 1

  if (available === 0) return '已满'
  if (available / total > 0.5) return '空闲充足'
  if (available / total > 0.2) return '车位紧张'
  return '即将满员'
}

const getVehicleTypeText = (type) => {
  const typeMap = {
    'CAR': '轿车',
    'MOTORCYCLE': '摩托车',
    'ELECTRIC_CAR': '电动车'
  }
  return typeMap[type] || type
}

const getSpotStatusText = (status) => {
  const statusMap = {
    'AVAILABLE': '空闲',
    'OCCUPIED': '占用',
    'MAINTENANCE': '维护'
  }
  return statusMap[status] || status
}

const getParkingStatusText = (status) => {
  const statusMap = {
    'PARKING': '停车中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusIcon = (status) => {
  const icons = {
    'AVAILABLE': '✅',
    'OCCUPIED': '🚫',
    'MAINTENANCE': '🔧'
  }
  return icons[status] || '❓'
}

const getSpotTypeText = (type) => {
  const typeMap = {
    'REGULAR': '普通',
    'DISABLED': '残疾人',
    'RESERVED': '预留'
  }
  return typeMap[type] || type
}

const isRecommendedSpot = (spot) => {
  return spot.spotNumber.includes('01') || spot.spotNumber.includes('A01') ||
    spot.spotNumber.includes('001') || spot.spotType === 'DISABLED'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const calculateDuration = (startTime) => {
  if (!startTime) return '0分钟'
  const start = new Date(startTime)
  const now = new Date()
  const duration = now - start

  const hours = Math.floor(duration / (1000 * 60 * 60))
  const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

const formatTimer = (startTime) => {
  if (!startTime) return '00:00'
  const start = new Date(startTime)
  const now = new Date()
  const duration = now - start

  const hours = Math.floor(duration / (1000 * 60 * 60))
  const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))

  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
}
// 🆕 车牌号格式验证正则
const plateRegex = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4,5}[A-Z0-9挂学警港澳]{1}$/

// 🆕 计算属性：手动输入是否有效
const isManualPlateValid = computed(() => {
  return manualPlateNumber.value.trim() &&
    !manualPlateError.value &&
    plateRegex.test(manualPlateNumber.value.trim())
})

// 🆕 手动输入车牌验证
const validateManualPlate = () => {
  const plate = manualPlateNumber.value.trim()

  if (!plate) {
    manualPlateError.value = ''
    return
  }

  if (!plateRegex.test(plate)) {
    manualPlateError.value = '车牌号格式不正确，请按照标准格式输入'
  } else {
    manualPlateError.value = ''
  }
}

// 🆕 手动验证车牌号
// 🆕 手动验证车牌号（修改版）
const verifyManualPlate = async () => {
  if (!isManualPlateValid.value) return

  isVerifyingManual.value = true
  manualPlateError.value = ''
  manualVerificationResult.value = null

  try {
    console.log('🔍 开始手动验证车牌:', manualPlateNumber.value)

    // 🆕 修改：使用现有的车辆验证接口
    // 方案1：先查询车牌是否存在，再验证归属
    const plateToVerify = manualPlateNumber.value.trim()

    // 步骤1：查询该车牌是否存在
    const checkResponse = await authFetch(`/api/vehicles/search?plateNumber=${encodeURIComponent(plateToVerify)}`)

    if (checkResponse.ok) {
      const vehicleData = await checkResponse.json()
      console.log('✅ 车牌查询结果:', vehicleData)

      if (vehicleData && vehicleData.id) {
        // 车牌存在，检查是否属于当前用户
        // 获取用户车辆列表，检查该车辆ID是否在用户车辆列表中
        const userVehiclesResponse = await authFetch('/api/vehicles/my-vehicles')

        if (userVehiclesResponse.ok) {
          const userVehicles = await userVehiclesResponse.json()
          console.log('👤 用户车辆列表:', userVehicles)

          // 查找该车牌是否在用户车辆列表中
          const foundVehicle = userVehicles.find(v =>
            v.id === vehicleData.id ||
            v.plateNumber === plateToVerify
          )

          if (foundVehicle) {
            // 🎯 车辆存在且属于当前用户
            if (foundVehicle.status === 'APPROVED') {
              // 创建验证结果对象，复用OCR成功的数据结构
              manualVerificationResult.value = {
                success: true,
                data: {
                  vehicle: foundVehicle
                }
              }

              // 自动选择验证成功的车辆
              selectedVehicleFromVerification.value = foundVehicle
              selectedVehicle.value = null
              campusVerificationError.value = '' // 清除OCR错误提示

              console.log('🚗 手动验证成功，自动选择车辆:', foundVehicle.plateNumber)

              // 滚动到停车场选择区域
              setTimeout(() => {
                const parkingLotSection = document.querySelector('.parking-lot-section')
                if (parkingLotSection) {
                  parkingLotSection.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                  })
                }
              }, 500)
            } else if (foundVehicle.status === 'PENDING') {
              manualPlateError.value = '该车辆正在审核中，请等待审核通过'
            } else if (foundVehicle.status === 'REJECTED') {
              manualPlateError.value = '该车辆审核未通过，请重新提交审核'
            }
          } else {
            // 车辆存在但不属于当前用户
            manualPlateError.value = '该车牌不属于您，请确认车牌号'
          }
        } else {
          manualPlateError.value = '获取用户车辆列表失败'
        }
      } else {
        // 车牌不存在
        manualPlateError.value = '该车牌不存在，请确认车牌号或先注册车辆'
      }
    } else {
      // 查询失败
      manualPlateError.value = '车牌查询失败，请稍后重试'
    }
  } catch (error) {
    console.error('💥 手动验证异常:', error)
    manualPlateError.value = '验证失败，请检查网络连接'
  } finally {
    isVerifyingManual.value = false
  }
}

// 🆕 清除手动验证状态
const clearManualVerification = () => {
  manualPlateNumber.value = ''
  manualPlateError.value = ''
  manualVerificationResult.value = null
  isVerifyingManual.value = false
}
// ========== 拍照验证方法 ==========
const triggerCameraUpload = () => {
  cameraInput.value?.click()
}

const handleCampusImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    campusVerificationError.value = '请上传图片文件'
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    campusVerificationError.value = '图片大小不能超过5MB'
    return
  }

  campusUploadedFile.value = file
  campusImagePreview.value = URL.createObjectURL(file)
  campusVerificationResult.value = null
  campusVerificationError.value = ''

  console.log('📸 车牌照片已上传:', file.name)
}

const verifyCampusVehicle = async () => {
  if (!campusUploadedFile.value) return

  isVerifying.value = true
  campusVerificationError.value = ''

  try {
    console.log('🔍 开始验证校内车辆...')

    const formData = new FormData()
    formData.append('licenseImage', campusUploadedFile.value)

    const response = await authFetch('/api/parking/entry/ocr/campus', {
      method: 'POST',
      body: formData
    })

    if (response.ok) {
      const result = await response.json()
      console.log('✅ 验证响应:', result)

      if (result.success) {
        campusVerificationResult.value = result.data

        // 自动选择验证成功的车辆
        if (result.data.vehicle && vehicles.value) {
          const vehicle = result.data.vehicle
          const matchedVehicle = vehicles.value.find(v => v.id === vehicle.id)
          if (matchedVehicle) {
            selectedVehicleFromVerification.value = matchedVehicle
            selectedVehicle.value = null
            console.log('🚗 自动选择车辆:', vehicle.plateNumber)

            // 滚动到停车场选择区域
            setTimeout(() => {
              const parkingLotSection = document.querySelector('.parking-lot-section')
              if (parkingLotSection) {
                parkingLotSection.scrollIntoView({
                  behavior: 'smooth',
                  block: 'start'
                })
              }
            }, 500)
          }
        }
      } else {
        campusVerificationError.value = result.message || '验证失败'
      }
    } else {
      const errorText = await response.text()
      console.error('❌ 验证请求失败:', response.status, errorText)
      campusVerificationError.value = `验证失败 (${response.status})`
    }
  } catch (error) {
    console.error('💥 验证异常:', error)
    campusVerificationError.value = error.message || '网络错误，请重试'
  } finally {
    isVerifying.value = false
  }
}

const selectVerifiedVehicle = () => {
  if (campusVerificationResult.value?.vehicle) {
    const vehicleId = campusVerificationResult.value.vehicle.id

    if (vehicles.value) {
      const vehicle = vehicles.value.find(v => v.id === vehicleId)
      if (vehicle) {
        selectedVehicleFromVerification.value = vehicle
        selectedVehicle.value = null

        // 滚动到停车场选择区域
        setTimeout(() => {
          const parkingLotSection = document.querySelector('.parking-lot-section')
          if (parkingLotSection) {
            parkingLotSection.scrollIntoView({
              behavior: 'smooth',
              block: 'start'
            })
          }
        }, 100)

        console.log('🚗 已选择车辆:', vehicle.plateNumber)
      }
    }
  }
}

// 🆕 修改现有的 clearCampusImage 方法，同时清除手动验证状态
const clearCampusImage = () => {
  campusUploadedFile.value = null
  campusImagePreview.value = ''
  campusVerificationResult.value = null
  campusVerificationError.value = ''
  selectedVehicleFromVerification.value = null
  clearManualVerification() // 🆕 同时清除手动验证状态

  if (cameraInput.value) {
    cameraInput.value.value = ''
  }

  console.log('🧹 已清除拍照验证和手动验证状态')
}

// ========== API 数据加载方法 ==========
const loadParkingLots = async () => {
  try {
    loadingLots.value = true
    error.value = ''

    console.log('🔄 正在加载停车场数据...')

    // 根据后端代码，停车场数据从 /api/parking-spots/parking-lots/stats 获取
    const response = await authFetch('/api/parking-spots/parking-lots/stats')

    if (response && response.ok) {
      const data = await response.json()
      console.log('✅ 停车场统计响应:', data)

      if (Array.isArray(data)) {
        parkingLots.value = data.map(lot => ({
          zone: lot.zone,
          totalSpots: lot.totalSpots || 0,
          availableSpots: lot.availableSpots || 0
        }))
        console.log('✅ 加载停车场数据成功:', parkingLots.value.length, '个停车场')
      } else {
        console.warn('停车场数据格式错误，使用默认数据')
        // 如果API格式不正确，使用模拟数据
        generateMockParkingLots()
      }
    } else {
      console.warn('停车场API请求失败，使用模拟数据')
      // 使用模拟数据
      generateMockParkingLots()
    }
  } catch (err) {
    console.error('加载停车场失败:', err)
    error.value = '加载停车场失败，请重试'

    // 使用模拟数据
    generateMockParkingLots()
  } finally {
    loadingLots.value = false
  }
}

const generateMockParkingLots = () => {
  // 使用后端定义的停车场区域常量
  const zones = [
    '学生活动中心',
    '一食堂停车区',
    '科大讯飞楼',
    '南硅谷A',
    '创业园',
    '演艺中心',
    '美食街'
  ]

  parkingLots.value = zones.map((zone, index) => ({
    zone: zone,
    totalSpots: Math.floor(Math.random() * 50) + 20, // 20-70个车位
    availableSpots: Math.floor(Math.random() * 30) + 5 // 5-35个空闲车位
  }))

  console.log('📊 生成模拟停车场数据:', parkingLots.value.length, '个停车场')
}

const loadParkingSpots = async () => {
  if (!selectedParkingLot.value) return

  try {
    loadingSpots.value = true
    error.value = ''

    const zone = selectedParkingLot.value.zone
    console.log('🔄 正在加载停车位数据，区域:', zone)

    // 根据后端代码，使用 /api/parking-spots/zone/{zone} 获取停车位
    const response = await authFetch(`/api/parking-spots/zone/${encodeURIComponent(zone)}`)

    if (response && response.ok) {
      const data = await response.json()
      console.log('✅ 停车位响应:', data)

      if (Array.isArray(data)) {
        parkingSpots.value = data
        console.log('✅ 加载停车位数据成功:', parkingSpots.value.length, '个停车位')
      } else {
        console.warn('停车位数据格式错误，使用模拟数据')
        generateMockParkingSpots(zone)
      }
    } else {
      console.warn('停车位API请求失败，使用模拟数据')
      generateMockParkingSpots(zone)
    }
  } catch (err) {
    console.error('加载停车位失败:', err)
    error.value = '加载停车位失败，请重试'

    generateMockParkingSpots(selectedParkingLot.value?.zone || '学生活动中心')
  } finally {
    loadingSpots.value = false
  }
}

const generateMockParkingSpots = (zone) => {
  const spots = []
  const totalSpots = Math.floor(Math.random() * 30) + 10 // 10-40个车位

  for (let i = 1; i <= totalSpots; i++) {
    // 随机生成状态，但确保有一定比例的空闲车位
    const statusRandom = Math.random()
    let status = 'AVAILABLE'
    if (statusRandom < 0.3) status = 'OCCUPIED'
    else if (statusRandom < 0.35) status = 'MAINTENANCE'

    // 随机生成车位类型
    const typeRandom = Math.random()
    let spotType = 'REGULAR'
    if (typeRandom < 0.05) spotType = 'DISABLED'
    else if (typeRandom < 0.1) spotType = 'RESERVED'

    spots.push({
      id: i,
      spotNumber: `${zone.substring(0, 1)}${i.toString().padStart(2, '0')}`,
      zone: zone,
      status: status,
      spotType: spotType,
      hourlyRate: zone === '学生活动中心' ? 0 : 5,
      createdAt: new Date().toISOString()
    })
  }

  parkingSpots.value = spots
  console.log('📊 生成模拟停车位数据:', spots.length, '个停车位，区域:', zone)

  // 更新停车场统计信息
  if (selectedParkingLot.value) {
    selectedParkingLot.value.totalSpots = spots.length
    selectedParkingLot.value.availableSpots = spots.filter(spot => spot.status === 'AVAILABLE').length
  }
}

const loadUserVehicles = async () => {
  try {
    loadingVehicles.value = true
    console.log('🔄 正在加载车辆数据...')

    const response = await authFetch('/api/vehicles/my-vehicles')

    if (response && response.ok) {
      const data = await response.json()
      console.log('✅ 车辆响应:', data)

      if (Array.isArray(data)) {
        vehicles.value = data
      } else if (data.data && Array.isArray(data.data)) {
        vehicles.value = data.data
      } else if (data.vehicles && Array.isArray(data.vehicles)) {
        vehicles.value = data.vehicles
      }

      console.log('✅ 加载车辆数据成功:', vehicles.value.length, '辆')
    } else {
      console.warn('车辆API请求失败，使用模拟数据')
      generateMockVehicles()
    }
  } catch (err) {
    console.error('加载车辆失败:', err)
    error.value = '加载车辆失败，请重试'

    generateMockVehicles()
  } finally {
    loadingVehicles.value = false
  }
}

const generateMockVehicles = () => {
  vehicles.value = [
    {
      id: 1,
      plateNumber: '京A12345',
      vehicleType: 'CAR',
      brand: '丰田',
      color: '黑色',
      status: 'APPROVED',
      isTemporary: false,
      createdAt: new Date().toISOString()
    },
    {
      id: 2,
      plateNumber: '京B67890',
      vehicleType: 'ELECTRIC_CAR',
      brand: '特斯拉',
      color: '白色',
      status: 'APPROVED',
      isTemporary: false,
      createdAt: new Date().toISOString()
    }
  ]
  console.log('📊 生成模拟车辆数据:', vehicles.value.length, '辆')
}

const loadCurrentParking = async () => {
  try {
    console.log('🔄 正在加载当前停车记录...')

    const response = await authFetch('/api/parking-records/my-current')
    if (response && response.ok) {
      const data = await response.json()
      console.log('✅ 当前停车记录响应:', data)

      if (data.data && data.data.length > 0) {
        currentParkingRecord.value = data.data[0]
      } else if (Array.isArray(data) && data.length > 0) {
        currentParkingRecord.value = data[0]
      } else if (data && data.id) {
        currentParkingRecord.value = data
      }

      if (currentParkingRecord.value) {
        console.log('✅ 当前停车记录:', currentParkingRecord.value)
      }
    }
  } catch (err) {
    console.error('获取当前停车记录失败:', err)
  }
}

// ========== 选择方法 ==========
const selectParkingLot = async (lot) => {
  selectedParkingLot.value = lot
  selectedSpot.value = null
  selectedVehicle.value = null

  // 加载该停车场的停车位
  await loadParkingSpots()

  // 滚动到停车位选择区域
  setTimeout(() => {
    const spotSection = document.querySelector('.parking-spot-section')
    if (spotSection) {
      spotSection.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      })
    }
  }, 300)
}

const selectVehicle = (vehicle) => {
  selectedVehicle.value = vehicle
  selectedVehicleFromVerification.value = null
}

const selectSpot = (spot) => {
  if (spot.status === 'AVAILABLE') {
    selectedSpot.value = spot

    // 滚动到车辆选择区域
    setTimeout(() => {
      const vehicleSection = document.querySelector('.vehicle-section')
      if (vehicleSection) {
        vehicleSection.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        })
      }
    }, 300)
  }
}

const clearSelection = () => {
  selectedSpot.value = null
  selectedVehicle.value = null
  selectedVehicleFromVerification.value = null
}

const refreshVehicles = async () => {
  await loadUserVehicles()
}

// ========== 停车操作 ==========
const startParking = async () => {
  if (!selectedSpot.value || !selectedParkingLot.value ||
    (!selectedVehicle.value && !selectedVehicleFromVerification.value)) {
    error.value = '请完成所有选择'
    return
  }

  try {
    isStartingParking.value = true
    error.value = ''

    const vehicleId = (selectedVehicle.value || selectedVehicleFromVerification.value).id
    const spotId = selectedSpot.value.id
    const zone = selectedParkingLot.value.zone

    // 在清除前保存需要显示的值
    const plateNumber = (selectedVehicle.value || selectedVehicleFromVerification.value).plateNumber
    const spotNumber = selectedSpot.value.spotNumber  // 保存这个值！

    console.log('🅿️ 开始停车:', {
      vehicleId: vehicleId,
      spotId: spotId,
      zone: zone,
      spotNumber: spotNumber,
      plateNumber: plateNumber
    })

    // 调用后端开始停车API
    const response = await authFetch('/api/parking-records/start', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        vehicleId: vehicleId,
        spotId: spotId
      })
    })

    if (response && response.ok) {
      const data = await response.json()

      // 更新当前停车记录
      currentParkingRecord.value = data.data || data

      // 刷新停车位状态
      await loadParkingSpots()

      // 使用保存的值显示成功消息
      alert(`✅ 停车开始成功！\n停车场：${zone}\n车位：${spotNumber}\n车牌：${plateNumber}`)

      // 现在才清除选择状态
      clearSelection()
      clearCampusImage()

      console.log('✅ 停车开始成功:', currentParkingRecord.value)
    } else {
      const errorData = await response.json().catch(() => ({ message: '未知错误' }))
      error.value = errorData.message || '开始停车失败，请重试'
    }
  } catch (err) {
    console.error('停车开始失败:', err)
    error.value = '网络错误，请重试'
  } finally {
    isStartingParking.value = false
  }
}

const endParking = async () => {
  if (!currentParkingRecord.value) return

  if (!confirm('确定要结束本次停车吗？')) return

  try {
    const response = await authFetch(`/api/parking-records/${currentParkingRecord.value.id}/end`, {
      method: 'POST'
    })

    if (response && response.ok) {
      const data = await response.json()
      const fee = data.data?.fee || data.fee || 0

      // 显示费用信息
      if (fee > 0) {
        alert(`✅ 停车结束成功！\n费用：¥${fee.toFixed(2)}`)
      } else {
        alert('✅ 停车结束成功！本次停车免费。')
      }

      // 清除当前停车记录
      currentParkingRecord.value = null

      // 刷新停车位状态
      if (selectedParkingLot.value) {
        await loadParkingSpots()
      }
    } else {
      const errorData = await response.json().catch(() => ({ message: '未知错误' }))
      error.value = errorData.message || '结束停车失败'
    }
  } catch (err) {
    console.error('结束停车失败:', err)
    error.value = '网络错误，请重试'
  }
}

const cancelParking = async () => {
  if (!currentParkingRecord.value) return

  if (!confirm('确定要取消本次停车吗？取消后不可恢复。')) return

  try {
    const response = await authFetch(`/api/parking-records/${currentParkingRecord.value.id}/cancel`, {
      method: 'POST'
    })

    if (response && response.ok) {
      // 清除当前停车记录
      currentParkingRecord.value = null

      // 刷新停车位状态
      if (selectedParkingLot.value) {
        await loadParkingSpots()
      }

      alert('✅ 停车已取消')
    } else {
      const errorData = await response.json().catch(() => ({ message: '未知错误' }))
      error.value = errorData.message || '取消停车失败'
    }
  } catch (err) {
    console.error('取消停车失败:', err)
    error.value = '网络错误，请重试'
  }
}

// ========== 初始化 ==========
onMounted(async () => {
  try {
    // 从localStorage获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    userType.value = userInfo.role || userInfo.userType || 'STUDENT'

    console.log('👤 用户类型:', userType.value, '是否为校内用户:', isCampusUser.value)

    // 加载数据
    await Promise.all([
      loadCurrentParking(),
      loadParkingLots(),
      loadUserVehicles()
    ])

    // 如果没有当前停车记录，默认选择第一个停车场
    if (!currentParkingRecord.value && parkingLots.value.length > 0) {
      selectedParkingLot.value = parkingLots.value[0]
      await loadParkingSpots()
    }

  } catch (err) {
    console.error('初始化失败:', err)
    error.value = '页面初始化失败，请刷新重试'
  }
})

// 监听停车场选择变化
watch(selectedParkingLot, (newLot) => {
  if (newLot) {
    loadParkingSpots()
  }
})

// 监听当前停车记录变化
watch(currentParkingRecord, (newRecord) => {
  if (newRecord) {
    console.log('🔄 当前停车记录更新:', newRecord)
  }
})
</script>

<style scoped>
/* 🆕 手动输入降级区域样式 */
.manual-fallback-section {
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.fallback-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.fallback-icon {
  font-size: 20px;
  margin-right: 8px;
}

.fallback-header h5 {
  margin: 0;
  font-size: 14px;
  color: #495057;
}

.fallback-input-group {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.fallback-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.fallback-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.fallback-input.error {
  border-color: #dc3545;
}

.fallback-verify-btn {
  padding: 10px 16px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.fallback-verify-btn:hover:not(:disabled) {
  background-color: #218838;
}

.fallback-verify-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.65;
}

.fallback-error {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #dc3545;
  font-size: 13px;
  margin-top: 5px;
}

.fallback-error .error-icon.small {
  font-size: 14px;
}

.fallback-success {
  margin-top: 15px;
  padding: 12px;
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.fallback-success .success-icon {
  font-size: 20px;
  color: #28a745;
}

.fallback-success .success-content {
  flex: 1;
}

.fallback-success .success-title {
  margin: 0 0 5px 0;
  color: #155724;
  font-weight: 600;
}

.fallback-success .vehicle-details {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin: 8px 0;
}

.fallback-success .success-hint {
  margin: 5px 0 0 0;
  color: #0c5460;
  font-size: 13px;
}
/* 基本样式（保留原有样式） */
.parking-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.page-header {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
}

.page-subtitle {
  color: #666;
  margin: 8px 0 0 0;
}

.user-badge {
  background: #f3f4f6;
  padding: 8px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-icon {
  font-size: 18px;
}

.user-type {
  color: #4b5563;
  font-weight: 500;
}

.main-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 当前停车状态 */
.current-parking-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.badge-live {
  background: #ef4444;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.current-parking-card {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 16px;
  padding: 24px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.parking-info {
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.value {
  font-size: 16px;
  font-weight: 500;
}

.plate-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
}

.duration {
  color: #fbbf24;
}

.action-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.fee-display {
  display: flex;
  flex-direction: column;
}

.fee-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.fee-amount {
  font-size: 24px;
  font-weight: bold;
}

.free-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.free-icon {
  font-size: 20px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn-action {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: transform 0.2s;
}

.btn-action:hover {
  transform: translateY(-2px);
}

.btn-primary {
  background: white;
  color: #059669;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.parking-timer {
  margin-left: 40px;
}

.timer-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.timer-text {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.timer-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 拍照验证样式 */
.quick-parking-section {
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border-radius: 16px;
  color: white;
}

.quick-parking-section .section-header h2 {
  color: white;
  margin-bottom: 8px;
}

.hint-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.quick-parking-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-top: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.card-icon {
  font-size: 32px;
  margin-right: 16px;
  color: #8b5cf6;
}

.card-title h4 {
  margin: 0;
  color: #333;
}

.card-subtitle {
  color: #666;
  font-size: 14px;
  margin-top: 4px;
}

/* 上传区域样式 */
.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
  margin-bottom: 16px;
}

.upload-area:hover {
  border-color: #8b5cf6;
}

.upload-area.has-image {
  border-color: #10b981;
  border-style: solid;
}

.upload-placeholder {
  color: #666;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.upload-text p {
  margin: 8px 0;
  font-weight: 500;
}

.upload-text small {
  color: #999;
  font-size: 12px;
}

.preview-container {
  position: relative;
  max-width: 300px;
  margin: 0 auto;
}

.preview-image {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.clear-btn {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #ef4444;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 验证按钮 */
.verify-btn {
  width: 100%;
  padding: 12px;
  background: #8b5cf6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.3s;
}

.verify-btn:hover:not(:disabled) {
  background: #7c3aed;
}

.verify-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 验证结果样式 */
.verification-result {
  margin-top: 20px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-success {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
}

.success-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #10b981;
}

.success-content {
  flex: 1;
}

.success-title {
  font-weight: 600;
  color: #065f46;
  margin-bottom: 8px;
}

.vehicle-details {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.vehicle-info {
  color: #4b5563;
  font-size: 14px;
}

.success-hint {
  color: #059669;
  font-size: 13px;
  margin-top: 4px;
}

/* 操作按钮 */
.quick-action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.btn-quick-park {
  flex: 2;
  padding: 12px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.2s;
}

.btn-quick-park:hover:not(:disabled) {
  transform: translateY(-2px);
}

.btn-quick-park:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-select {
  flex: 1;
  padding: 12px;
  background: white;
  color: #4b5563;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-select:hover {
  border-color: #9ca3af;
  background: #f9fafb;
}

/* 错误提示 */
.verification-error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 12px;
  margin-top: 16px;
  display: flex;
  align-items: flex-start;
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-2px); }
  20%, 40%, 60%, 80% { transform: translateX(2px); }
}

.error-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #dc2626;
}

.error-content {
  flex: 1;
}

.error-content p {
  margin: 0 0 8px 0;
  color: #7f1d1d;
}

.btn-dismiss {
  padding: 4px 12px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

/* 页脚 */
.card-footer {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  text-align: center;
}

.footer-note {
  color: #6b7280;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.note-icon {
  font-size: 16px;
}

/* 加载动画 */
.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

.loading-spinner.small {
  width: 14px;
  height: 14px;
  border-width: 2px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 传统流程样式 */
.parking-lot-section,
.vehicle-section,
.parking-spot-section,
.start-parking-section {
  margin-bottom: 32px;
}

.parking-lot-list,
.vehicle-list {
  display: grid;
  gap: 16px;
}

.parking-lot-item {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
}

.parking-lot-item:hover {
  border-color: #8b5cf6;
  transform: translateY(-2px);
}

.parking-lot-item.active {
  border-color: #8b5cf6;
  background: #f5f3ff;
}

.lot-icon {
  font-size: 40px;
  margin-right: 20px;
}

.lot-info {
  flex: 1;
}

.lot-name {
  margin: 0 0 8px 0;
  color: #333;
}

.lot-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
}

.stat.rate {
  color: #059669;
}

.stat.free {
  color: #8b5cf6;
}

.lot-description {
  color: #9ca3af;
  font-size: 13px;
  margin: 0;
}

.lot-status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-high {
  background: #d1fae5;
  color: #065f46;
}

.status-medium {
  background: #fef3c7;
  color: #92400e;
}

.status-low {
  background: #fee2e2;
  color: #991b1b;
}

.special-notice {
  background: #f5f3ff;
  border: 2px solid #8b5cf6;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
  display: flex;
  align-items: flex-start;
}

.notice-icon {
  font-size: 40px;
  margin-right: 20px;
  color: #8b5cf6;
}

.notice-content h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.notice-content p {
  color: #6b7280;
  margin: 0 0 12px 0;
}

.notice-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-features li {
  color: #059669;
  margin-bottom: 4px;
  font-size: 14px;
}

.btn-refresh {
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px 16px;
  color: #4b5563;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-refresh:hover {
  background: #e5e7eb;
}

.empty-vehicles {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-vehicles h4 {
  margin: 0 0 8px 0;
  color: #4b5563;
}

.empty-vehicles p {
  color: #9ca3af;
  margin: 0 0 20px 0;
}

.btn-add-vehicle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #8b5cf6;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-add-vehicle:hover {
  background: #7c3aed;
}

.vehicle-item {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.vehicle-item:hover {
  border-color: #8b5cf6;
  transform: translateY(-2px);
}

.vehicle-item.active {
  border-color: #8b5cf6;
  background: #f5f3ff;
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.vehicle-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.approved {
  background: #d1fae5;
  color: #065f46;
}

.vehicle-details {
  margin-bottom: 12px;
}

.vehicle-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.vehicle-brand,
.vehicle-color,
.vehicle-type {
  color: #6b7280;
  font-size: 14px;
}

.vehicle-date {
  color: #9ca3af;
  font-size: 13px;
}

.vehicle-select {
  text-align: right;
}

.select-indicator {
  color: #8b5cf6;
  font-size: 14px;
  font-weight: 500;
}

/* ========== 关键修复：停车位样式 ========== */
.parking-spot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.parking-spot-item {
  position: relative; /* 关键：添加相对定位 */
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  min-height: 120px; /* 固定高度 */
}

.parking-spot-item:hover {
  transform: translateY(-2px);
}

.parking-spot-item.available {
  border-color: #10b981;
}

.parking-spot-item.occupied {
  border-color: #ef4444;
  opacity: 0.6;
  cursor: not-allowed;
}

.parking-spot-item.maintenance {
  border-color: #f59e0b;
  opacity: 0.6;
  cursor: not-allowed;
}

.parking-spot-item.selected {
  border-color: #8b5cf6;
  background: #f5f3ff;
}

.parking-spot-item.recommended {
  border-color: #fbbf24;
}

.spot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.spot-number {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.spot-type-badge {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.recommended-badge {
  background: #fef3c7;
  color: #92400e;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;
}

.badge-icon {
  font-size: 10px;
}

.spot-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.status-text {
  font-size: 14px;
  color: #6b7280;
}

.spot-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.spot-rate {
  color: #059669;
  font-size: 13px;
  font-weight: 500;
}

.spot-free {
  color: #8b5cf6;
  font-size: 13px;
  font-weight: 500;
}

.spot-area {
  color: #9ca3af;
  font-size: 12px;
}

/* 车位状态覆盖层 - 关键修复 */
.spot-status-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border-radius: 6px;
  z-index: 1; /* 确保只在车位内部 */
}

.no-spots {
  text-align: center;
  padding: 40px 20px;
}

.no-spots-icon {
  font-size: 60px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.no-spots h4 {
  margin: 0 0 8px 0;
  color: #4b5563;
}

.no-spots p {
  color: #9ca3af;
  margin: 0 0 20px 0;
}

.btn-change-lot {
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 20px;
  color: #4b5563;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-change-lot:hover {
  background: #e5e7eb;
}

.confirmation-card {
  background: white;
  border: 2px solid #8b5cf6;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.confirmation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.confirmation-header h3 {
  margin: 0;
  color: #333;
}

.confirmation-badge {
  background: #8b5cf6;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.confirmation-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.spot-highlight {
  color: #8b5cf6;
  font-weight: bold;
}

.plate-highlight {
  color: #059669;
  font-weight: bold;
}

.type-highlight {
  color: #f59e0b;
  font-weight: bold;
}

.confirmation-notice {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
}

.notice-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #3b82f6;
}

.notice-content {
  flex: 1;
}

.notice-content p {
  margin: 0 0 8px 0;
  color: #1e40af;
}

.small-text {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.confirmation-actions {
  display: flex;
  gap: 12px;
}

.btn-cancel {
  flex: 1;
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  color: #4b5563;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-confirm {
  flex: 2;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  border-radius: 8px;
  padding: 12px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.2s;
}

.btn-confirm:hover:not(:disabled) {
  transform: translateY(-2px);
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.confirm-icon {
  font-size: 18px;
}

/* 加载和错误状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-content {
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px auto;
}

.loading-content p {
  color: #4b5563;
  margin: 0;
}

.error-alert {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #fef2f2;
  border: 2px solid #fecaca;
  border-radius: 12px;
  padding: 16px;
  max-width: 400px;
  z-index: 1000;
  animation: slideInRight 0.3s ease;
  display: flex;
  align-items: flex-start;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.alert-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #dc2626;
}

.alert-content h4 {
  margin: 0 0 8px 0;
  color: #7f1d1d;
}

.alert-content p {
  margin: 0 0 12px 0;
  color: #991b1b;
}

.btn-dismiss {
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
}

/* 验证成功后的操作区域 */
.verification-success-actions {
  margin-top: 16px;
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #bae6fd;
}

.success-prompt {
  color: #065f46;
  font-weight: 500;
  margin-bottom: 12px;
  text-align: center;
}

.action-buttons-horizontal {
  display: flex;
  gap: 12px;
}

.btn-continue {
  flex: 2;
  padding: 10px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.3s;
}

.btn-continue:hover {
  background: #059669;
}

.btn-change-vehicle {
  flex: 1;
  padding: 10px 16px;
  background: #f3f4f6;
  color: #4b5563;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-change-vehicle:hover {
  background: #e5e7eb;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.loading-state .loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px auto;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.empty-state .empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-state h4 {
  margin: 0 0 8px 0;
  color: #4b5563;
}

.empty-state p {
  margin: 0 0 20px 0;
}

/* 验证车辆通知 */
.verified-vehicle-notice {
  background: #f0f9ff;
  border: 2px solid #bae6fd;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
}

.verified-vehicle-notice .notice-icon {
  font-size: 32px;
  margin-right: 16px;
  color: #10b981;
}

.verified-vehicle-notice .notice-content h4 {
  margin: 0 0 12px 0;
  color: #065f46;
}

.vehicle-display {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.plate-badge.large {
  font-size: 18px;
  padding: 6px 16px;
}

.vehicle-details-text {
  color: #4b5563;
  font-size: 14px;
}

/* 没有空闲车位提示 */
.no-available-spots {
  text-align: center;
  padding: 40px 20px;
  background: #fef2f2;
  border: 2px solid #fecaca;
  border-radius: 12px;
  margin-top: 20px;
}

.no-available-spots .no-spots-icon {
  font-size: 60px;
  margin-bottom: 16px;
  color: #dc2626;
  opacity: 0.7;
}

.no-available-spots h4 {
  margin: 0 0 8px 0;
  color: #7f1d1d;
}

.no-available-spots p {
  color: #991b1b;
  margin: 0 0 20px 0;
}

/* 详情行布局 */
.detail-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 12px;
}

/* 小刷新按钮 */
.btn-refresh.small {
  padding: 4px 8px;
  font-size: 12px;
}

/* 节标题 */
.sub-section-title {
  color: #4b5563;
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

/* 车辆网格布局 */
.vehicle-grid {
  display: grid;
  gap: 16px;
}

/* 禁用状态的车位 */
.parking-spot-item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.parking-spot-item.disabled:hover {
  transform: none;
}

/* 确认卡片中的按钮状态 */
.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-confirm:disabled:hover {
  transform: none;
}
</style>
