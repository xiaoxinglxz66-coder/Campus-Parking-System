<template>
  <div class="parking-lot-management-container">
    <!-- 顶部导航栏 -->
    <nav class="management-navbar">
      <div class="navbar-brand">
        <h1>🏢 校园停车场管理系统</h1>
        <p class="navbar-subtitle">停车位分区管理 | 实时状态监控 | 数据分析</p>
      </div>
      <div class="navbar-actions">
        <button class="nav-btn" @click="refreshData">
          <span class="nav-icon">🔄</span> 刷新
        </button>
        <div class="user-info">
          <span class="user-avatar">👤</span>
          <span class="user-name">管理员</span>
        </div>
      </div>
    </nav>

    <!-- 停车场概览卡片 -->
    <div class="parking-lots-overview">
      <div class="overview-cards">
        <div class="overview-card total">
          <div class="overview-icon">🅿️</div>
          <div class="overview-content">
            <div class="overview-count">{{ stats.total }}</div>
            <div class="overview-label">总车位数</div>
          </div>
        </div>

        <div class="overview-card available">
          <div class="overview-icon">✅</div>
          <div class="overview-content">
            <div class="overview-count">{{ stats.available }}</div>
            <div class="overview-label">空闲车位</div>
          </div>
        </div>

        <div class="overview-card occupied">
          <div class="overview-icon">🚗</div>
          <div class="overview-content">
            <div class="overview-count">{{ stats.occupied }}</div>
            <div class="overview-label">占用车位</div>
          </div>
        </div>

        <div class="overview-card utilization">
          <div class="overview-icon">📊</div>
          <div class="overview-content">
            <div class="overview-count">{{ utilizationRate }}%</div>
            <div class="overview-label">使用率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要操作区 -->
    <div class="main-operations">
      <div class="operation-cards">
        <div class="op-card add-card" @click="showAddDialog = true">
          <div class="op-icon">➕</div>
          <div class="op-content">
            <h3>添加新车位</h3>
            <p>创建新的停车位信息</p>
          </div>
        </div>

        <div class="op-card stats-card" @click="showZoneStats = !showZoneStats">
          <div class="op-icon">📊</div>
          <div class="op-content">
            <h3>区域统计</h3>
            <p>查看各区域使用情况</p>
          </div>
          <div class="op-badge">{{ availableZones.length }}个区域</div>
        </div>

        <div class="op-card filter-card" @click="toggleFilterPanel">
          <div class="op-icon">🔍</div>
          <div class="op-content">
            <h3>智能筛选</h3>
            <p>{{ isFilterPanelOpen ? '收起' : '展开' }}筛选面板</p>
          </div>
        </div>

        <div class="op-card export-card" @click="exportData">
          <div class="op-icon">📤</div>
          <div class="op-content">
            <h3>导出数据</h3>
            <p>导出为JSON/Excel</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 区域统计柱形图面板 -->
    <div v-if="showZoneStats" class="zone-stats-panel chart-panel">
      <div class="panel-header">
        <h4>各区域车位统计柱形图</h4>
        <button class="panel-close" @click="showZoneStats = false">×</button>
      </div>

      <div class="chart-controls">
        <div class="chart-type-toggle">
          <button
            :class="['chart-type-btn', { active: chartType === 'grouped' }]"
            @click="chartType = 'grouped'"
          >
            📈 分组柱形图
          </button>
        </div>
        <div class="chart-legend">
          <div class="legend-item">
            <span class="legend-color available"></span>
            <span class="legend-label">空闲车位</span>
          </div>
          <div class="legend-item">
            <span class="legend-color occupied"></span>
            <span class="legend-label">占用车位</span>
          </div>
          <div class="legend-item">
            <span class="legend-color total"></span>
            <span class="legend-label">车位总数</span>
          </div>
        </div>
      </div>

      <div class="chart-container">
        <div class="chart-wrapper">
          <!-- Y轴标签 -->
          <div class="y-axis">
            <div v-for="(tick, index) in yAxisTicks" :key="index" class="y-tick">
              <span class="tick-label">{{ tick }}</span>
              <div class="tick-line"></div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="chart-area">
            <!-- X轴 -->
            <div class="x-axis">
              <div
                v-for="zone in chartZones"
                :key="zone.value"
                class="x-tick"
                :style="{ width: `${100 / chartZones.length}%` }"
              >
                <span class="x-label">{{ zone.label }}</span>
              </div>
            </div>

            <!-- 柱形图 -->
            <div class="bars-container">
              <div
                v-for="zone in chartZones"
                :key="zone.value"
                class="bar-group"
                :style="{ width: `${100 / chartZones.length}%` }"
              >

                <!-- 分组柱形图模式 -->
                <div v-if="chartType === 'grouped'" class="grouped-bars">
                  <div
                    class="grouped-bar available"
                    :style="{
                  height: `${Math.min(getZoneAvailableCount(zone.value) * barScale, 100)}%`,
                  width: '28%'
                }"
                    :title="`空闲: ${getZoneAvailableCount(zone.value)}`"
                  >
                <span class="bar-value" v-if="getZoneAvailableCount(zone.value) > 0">
                  {{ getZoneAvailableCount(zone.value) }}
                </span>
                  </div>
                  <div
                    class="grouped-bar occupied"
                    :style="{
                  height: `${Math.min(getZoneOccupiedCount(zone.value) * barScale, 100)}%`,
                  width: '28%'
                }"
                    :title="`占用: ${getZoneOccupiedCount(zone.value)}`"
                  >
                <span class="bar-value" v-if="getZoneOccupiedCount(zone.value) > 0">
                  {{ getZoneOccupiedCount(zone.value) }}
                </span>
                  </div>
                  <div
                    class="grouped-bar total"
                    :style="{
                  height: `${Math.min(getZoneTotalCount(zone.value) * barScale, 100)}%`,
                  width: '28%'
                }"
                    :title="`总数: ${getZoneTotalCount(zone.value)}`"
                  >
                <span class="bar-value">
                  {{ getZoneTotalCount(zone.value) }}
                </span>
                  </div>
                </div>

                <!-- 区域名称 -->
                <div class="zone-name-bottom">{{ zone.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-summary">
        <div class="summary-item">
          <span class="summary-label">统计区域:</span>
          <span class="summary-value">{{ availableZones.length }}个</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">平均使用率:</span>
          <span class="summary-value">{{ averageUtilization }}%</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">最高使用率:</span>
          <span class="summary-value">{{ maxUtilization }}%</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">最低使用率:</span>
          <span class="summary-value">{{ minUtilization }}%</span>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div v-if="isFilterPanelOpen" class="filter-toolbar">
      <div class="filter-group">
        <label>区域筛选:</label>
        <div class="zone-filters">
          <button
            v-for="zone in availableZones"
            :key="zone.value"
            :class="['zone-filter-btn', { active: filters.zone === zone.value }]"
            @click="toggleZoneFilter(zone.value)"
          >
            {{ zone.label }}
            <span class="filter-count">{{ getZoneCount(zone.value) }}</span>
          </button>
        </div>
      </div>

      <div class="filter-group">
        <label>状态筛选:</label>
        <div class="status-filters">
          <button
            v-for="status in statusOptions"
            :key="status.value"
            :class="['status-filter-btn', status.value.toLowerCase(), { active: filters.status === status.value }]"
            @click="toggleStatusFilter(status.value)"
          >
            <span class="status-icon">{{ status.icon }}</span>
            {{ status.label }}
          </button>
        </div>
      </div>

      <div class="filter-group">
        <label>搜索:</label>
        <div class="search-box">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="输入车位编号或区域..."
            @input="handleSearch"
          />
          <span class="search-icon">🔍</span>
        </div>
      </div>

      <div class="filter-actions">
        <button class="clear-filters-btn" @click="clearFilters">
          🗑️ 清除筛选
        </button>
        <button class="apply-filters-btn" @click="toggleFilterPanel">
          ✅ 应用筛选
        </button>
      </div>
    </div>

    <!-- 停车场视图切换 -->
    <div class="view-controls">
      <div class="view-tabs">
        <button
          :class="['view-tab', { active: currentView === 'cards' }]"
          @click="currentView = 'cards'"
        >
          <span class="tab-icon">🃏</span> 卡片视图
        </button>
        <button
          :class="['view-tab', { active: currentView === 'table' }]"
          @click="currentView = 'table'"
        >
          <span class="tab-icon">📋</span> 表格视图
        </button>
        <button
          :class="['view-tab', { active: currentView === 'map' }]"
          @click="currentView = 'map'"
        >
          <span class="tab-icon">🗺️</span> 区域视图
        </button>
      </div>

      <div class="view-actions">
        <div class="records-per-page">
          <label>每页显示:</label>
          <select v-model="pageSize" @change="currentPage = 1">
            <option value="12">12个</option>
            <option value="24">24个</option>
            <option value="48">48个</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 卡片视图 -->
    <div v-if="currentView === 'cards'" class="parking-lot-cards-view">
      <div class="cards-container">
        <div class="cards-grid">
          <div
            v-for="spot in paginatedSpots"
            :key="spot.id"
            :class="['parking-spot-card', spot.status.toLowerCase(), spot.zone]"
            @click="selectSpot(spot)"
          >
            <div class="spot-card-header">
              <div class="spot-checkbox">
                <input
                  type="checkbox"
                  :checked="isSelected(spot.id)"
                  @change="toggleSelectSpot(spot)"
                />
              </div>
              <div class="spot-title">
                <h4>{{ spot.spotNumber }}</h4>
                <span class="spot-id">ID: {{ spot.id }}</span>
              </div>
              <div class="spot-actions">
                <button class="spot-action-btn" @click.stop="editSpot(spot)" title="编辑">
                  ✏️
                </button>
                <button class="spot-action-btn" @click.stop="showStatusMenu(spot)" title="状态">
                  🔄
                </button>
              </div>
            </div>

            <div class="spot-card-content">
              <div class="spot-info-row">
                <span class="info-label">所属区域:</span>
                <span class="info-value">
                  <span class="zone-badge">{{ spot.zone }}</span>
                </span>
              </div>
              <div class="spot-info-row">
                <span class="info-label">车位类型:</span>
                <span class="info-value type-badge" :class="spot.spotType.toLowerCase()">
                  {{ getTypeText(spot.spotType) }}
                </span>
              </div>
              <div class="spot-info-row">
                <span class="info-label">当前状态:</span>
                <span class="info-value status-badge" :class="spot.status.toLowerCase()">
                  {{ getStatusText(spot.status) }}
                </span>
              </div>
              <div class="spot-info-row">
                <span class="info-label">停车费率:</span>
                <span class="info-value rate-value">¥{{ spot.hourlyRate }}/小时</span>
              </div>
            </div>

            <div class="spot-card-footer">
              <div class="spot-footer-info">
                <span class="footer-label">最后更新:</span>
                <span class="footer-value">{{ formatUpdateTime(spot.updated_at) }}</span>
              </div>
              <button class="spot-action-btn delete" @click.stop="confirmDelete(spot)">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格视图 -->
    <div v-else-if="currentView === 'table'" class="parking-lot-table-view">
      <div class="table-responsive">
        <table class="parking-spots-table">
          <thead>
          <tr>
            <th class="checkbox-column">
              <input
                type="checkbox"
                :checked="selectedSpots.length === paginatedSpots.length && paginatedSpots.length > 0"
                @change="toggleSelectAll"
              />
            </th>
            <th @click="sortBy('spotNumber')" class="sortable">
              车位编号
              <span v-if="sortField === 'spotNumber'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
            </th>
            <th @click="sortBy('zone')" class="sortable">
              区域
              <span v-if="sortField === 'zone'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
            </th>
            <th>类型</th>
            <th @click="sortBy('status')" class="sortable">
              状态
              <span v-if="sortField === 'status'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
            </th>
            <th @click="sortBy('hourlyRate')" class="sortable">
              费率
              <span v-if="sortField === 'hourlyRate'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
            </th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr
            v-for="spot in paginatedSpots"
            :key="spot.id"
            :class="{ selected: isSelected(spot.id), [spot.status.toLowerCase()]: true }"
          >
            <td>
              <input
                type="checkbox"
                :checked="isSelected(spot.id)"
                @change="toggleSelectSpot(spot)"
              />
            </td>
            <td class="spot-number-cell">
              <strong>{{ spot.spotNumber }}</strong>
              <div class="spot-id">ID: {{ spot.id }}</div>
            </td>
            <td>
                <span class="zone-tag" :class="'zone-' + spot.zone.charAt(0)">
                  {{ spot.zone }}
                </span>
            </td>
            <td>
                <span class="type-badge" :class="spot.spotType.toLowerCase()">
                  {{ getTypeText(spot.spotType) }}
                </span>
            </td>
            <td>
                <span class="status-badge" :class="spot.status.toLowerCase()">
                  {{ getStatusText(spot.status) }}
                </span>
            </td>
            <td class="rate-cell">
              <div class="rate-value">¥{{ spot.hourlyRate }}</div>
              <div class="rate-unit">/小时</div>
            </td>
            <td class="action-cell">
              <div class="action-buttons">
                <button
                  class="action-btn edit-btn"
                  @click="editSpot(spot)"
                  title="编辑车位"
                >
                  ✏️ 编辑
                </button>
                <button
                  class="action-btn status-btn"
                  @click="showStatusMenu(spot)"
                  title="更改状态"
                >
                  🔄 状态
                </button>
                <button
                  class="action-btn delete-btn"
                  @click="confirmDelete(spot)"
                  title="删除车位"
                >
                  🗑️ 删除
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 区域视图 -->
    <div v-else-if="currentView === 'map'" class="parking-lot-map-view">
      <div class="map-view-header">
        <h4>区域分布视图</h4>
        <p class="map-subtitle">按区域查看车位分布情况</p>
      </div>

      <div class="zones-container">
        <div v-for="zone in availableZones" :key="zone.value" class="zone-section">
          <div class="zone-header">
            <h5>{{ zone.label }}</h5>
            <div class="zone-stats">
              <span class="zone-stat available">🟢 {{ getZoneAvailableCount(zone.value) }}</span>
              <span class="zone-stat occupied">🔴 {{ getZoneOccupiedCount(zone.value) }}</span>
              <span class="zone-stat total">🅿️ {{ getZoneTotalCount(zone.value) }}</span>
            </div>
          </div>

          <div class="zone-spots-grid">
            <div
              v-for="spot in getZoneSpots(zone.value)"
              :key="spot.id"
              :class="['zone-spot-item', spot.status.toLowerCase()]"
              @click="selectSpot(spot)"
              :title="`${spot.spotNumber} - ${getStatusText(spot.status)}`"
            >
              <div class="spot-number">{{ spot.spotNumber }}</div>
              <div class="spot-status-indicator"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页控件 -->
    <div class="pagination-container">
      <div class="pagination-info">
        显示 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, filteredSpots.length) }} 条，共 {{ filteredSpots.length }} 条
      </div>
      <div class="pagination-controls">
        <button
          class="pagination-btn prev"
          @click="prevPage"
          :disabled="currentPage === 1"
        >
          ◀ 上一页
        </button>

        <div class="page-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            :class="['page-btn', { active: page === currentPage }]"
            @click="currentPage = page"
          >
            {{ page }}
          </button>
          <span v-if="showEllipsis" class="page-ellipsis">...</span>
        </div>

        <button
          class="pagination-btn next"
          @click="nextPage"
          :disabled="currentPage === totalPages"
        >
          下一页 ▶
        </button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedSpots.length > 0" class="bulk-actions-bar">
      <div class="selected-count">
        已选择 {{ selectedSpots.length }} 个车位
        <button class="clear-selection-btn" @click="clearSelection">
          清空选择
        </button>
      </div>
      <div class="bulk-actions">
        <button class="bulk-action-btn available" @click="bulkUpdateStatus('AVAILABLE')">
          ✅ 设为空闲
        </button>
        <button class="bulk-action-btn occupied" @click="bulkUpdateStatus('OCCUPIED')">
          🚗 设为占用
        </button>
        <button class="bulk-action-btn maintenance" @click="bulkUpdateStatus('MAINTENANCE')">
          🔧 设为维护
        </button>
        <button class="bulk-action-btn delete" @click="bulkDelete">
          🗑️ 批量删除
        </button>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <div v-if="showAddDialog" class="modal-overlay">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑停车位' : '添加新停车位' }}</h3>
          <button class="modal-close" @click="showAddDialog = false">×</button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="saveSpot">
            <div class="form-grid">
              <div class="form-group">
                <label>车位编号 *</label>
                <input
                  type="text"
                  v-model="editForm.spotNumber"
                  placeholder="例如: A001、B101"
                  required
                  :disabled="isEditing"
                />
                <small class="form-hint">必须唯一，格式建议：区域字母+3位数字</small>
              </div>

              <div class="form-group">
                <label>所属区域 *</label>
                <select v-model="editForm.zone" required>
                  <option value="">请选择区域</option>
                  <option v-for="zone in availableZones" :key="zone.value" :value="zone.value">
                    {{ zone.label }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>车位类型 *</label>
                <select v-model="editForm.spotType" required>
                  <option value="REGULAR">普通车位</option>
                  <option value="DISABLED">残疾人车位</option>
                  <option value="RESERVED">预留车位</option>
                </select>
              </div>

              <div class="form-group">
                <label>初始状态 *</label>
                <select v-model="editForm.status" required>
                  <option value="AVAILABLE">空闲</option>
                  <option value="OCCUPIED">占用</option>
                  <option value="MAINTENANCE">维护中</option>
                </select>
              </div>

              <div class="form-group">
                <label>小时费率 (元) *</label>
                <input
                  type="number"
                  v-model="editForm.hourlyRate"
                  step="0.5"
                  min="0"
                  max="50"
                  required
                />
                <small class="form-hint">设置该车位的每小时停车费用</small>
              </div>

              <div class="form-group full-width">
                <label>备注信息</label>
                <textarea
                  v-model="editForm.remark"
                  placeholder="可填写车位的特殊说明或备注信息..."
                  rows="3"
                ></textarea>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="showAddDialog = false">
                取消
              </button>
              <button type="submit" class="btn-primary" :disabled="isSubmitting">
                {{ isSubmitting ? '保存中...' : isEditing ? '更新车位' : '添加车位' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 状态选择菜单 -->
    <div v-if="showStatusMenuForSpot" class="status-menu-overlay" @click.self="closeStatusMenu">
      <div class="status-menu" :style="statusMenuPosition">
        <h4>更改状态: {{ currentSpot.spotNumber }}</h4>
        <div class="status-options">
          <button
            v-for="status in statusOptions"
            :key="status.value"
            :class="['status-option-btn', status.value.toLowerCase()]"
            @click="updateSpotStatus(status.value)"
          >
            <span class="status-option-icon">{{ status.icon }}</span>
            {{ status.label }}
          </button>
        </div>
        <button class="status-menu-close" @click="closeStatusMenu">
          取消
        </button>
      </div>
    </div>

    <!-- 确认删除对话框 -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="confirm-dialog">
        <div class="confirm-header">
          <h3>确认删除</h3>
        </div>
        <div class="confirm-body">
          <p>确定要删除停车位 <strong>{{ spotToDelete.spotNumber }}</strong> 吗？</p>
          <p class="confirm-warning">此操作不可撤销！</p>
        </div>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="showDeleteConfirm = false">
            取消
          </button>
          <button class="btn-danger" @click="deleteSpot">
            确认删除
          </button>
        </div>
      </div>
    </div>

    <!-- 批量操作确认对话框 -->
    <div v-if="showBulkConfirm" class="modal-overlay">
      <div class="confirm-dialog">
        <div class="confirm-header">
          <h3>确认批量操作</h3>
        </div>
        <div class="confirm-body">
          <p>确定要对选中的 {{ selectedSpots.length }} 个车位执行以下操作吗？</p>
          <p class="confirm-action">
            <strong>{{ bulkAction }}</strong>
          </p>
          <div class="selected-spots-preview">
            <div
              v-for="spot in selectedSpotsPreview"
              :key="spot.id"
              class="selected-spot-item"
            >
              {{ spot.spotNumber }}
            </div>
            <div v-if="selectedSpots.length > 5" class="more-spots">
              等 {{ selectedSpots.length - 5 }} 个车位...
            </div>
          </div>
        </div>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="showBulkConfirm = false">
            取消
          </button>
          <button :class="['btn-confirm', bulkActionClass]" @click="executeBulkAction">
            确认{{ bulkAction }}
          </button>
        </div>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="status-bar">
      <div class="status-info">
        <span class="status-indicator active"></span>
        系统状态: 正常
      </div>
      <div class="status-update">
        最后更新: {{ lastUpdateTime }}
      </div>
      <div class="status-tips">
        💡 提示: 支持三种视图模式，点击卡片可快速操作
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { authFetch } from '../utils/auth'

// 原有逻辑保持不变，只添加新的UI相关变量
const currentView = ref('cards') // cards, table, map
const showZoneStats = ref(false)
const isFilterPanelOpen = ref(true)

// 原有数据保持完全一致
const parkingSpots = ref([])
const selectedSpots = ref([])
const showAddDialog = ref(false)
const showDeleteConfirm = ref(false)
const showStatusMenuForSpot = ref(false)
const showBulkConfirm = ref(false)
const isSubmitting = ref(false)
const isEditing = ref(false)
const searchKeyword = ref('')

// 添加数据版本跟踪
const dataVersion = ref(0)

const filters = ref({
  zone: '',
  status: '',
  spotType: ''
})

const currentPage = ref(1)
const pageSize = ref(12)
const sortField = ref('spotNumber')
const sortDirection = ref('asc')

const editForm = ref({
  spotNumber: '',
  zone: '',
  spotType: 'REGULAR',
  status: 'AVAILABLE',
  hourlyRate: '5.00',
  remark: ''
})

const statusMenuPosition = ref({ top: 0, left: 0 })
const currentSpot = ref(null)
const spotToDelete = ref(null)
const bulkAction = ref('')
const bulkActionClass = ref('')

const statusOptions = [
  { value: 'AVAILABLE', label: '空闲', icon: '✅' },
  { value: 'OCCUPIED', label: '占用', icon: '🚗' },
  { value: 'MAINTENANCE', label: '维护', icon: '🔧' },
]

// 添加的柱形图相关响应式变量
const chartType = ref('stacked') // 'stacked' 或 'grouped'

// 监控 parkingSpots 数据变化，自动刷新图表数据
watch(parkingSpots, () => {
  dataVersion.value++ // 数据变化时增加版本号，触发重新计算
  console.log('parkingSpots changed, dataVersion:', dataVersion.value)
}, { deep: true })

// 计算属性 - 区域处理（修复版）
const availableZones = computed(() => {
  // 从数据库中获取所有存在的区域
  const existingZones = [...new Set(parkingSpots.value.map(spot => spot.zone))]

  // 创建基础区域映射（将数据库中的区域映射到更友好的名称）
  const zoneMapping = {
    '一食堂': '一食堂停车区',
    '二食堂': '二食堂停车区',
    '三食堂': '三食堂停车区',
    '活动中心': '学生活动中心',
    '图书馆': '图书馆停车区',
    '新闻科大楼': '新闻科大楼停车区',
    '科大讯飞楼': '科大讯飞楼停车区',
    '南硅谷': '南硅谷A停车区',
    '大学生创业园': '创业园停车区',
    '演艺中心': '演艺中心停车区',
    '美食街': '美食街停车区',
    '体育馆': '体育馆停车区',
    '训练馆': '训练馆停车区',
    '万科楼': '万科楼停车区',
    '南门': '南门停车区',
    '北门': '北门停车区',
    'A区': 'A区停车位',
    'B区': 'B区停车位',
    'C': 'C区停车位'
  }

  // 创建区域数组，使用映射后的友好名称
  const zones = existingZones.map(zone => ({
    value: zone, // 保持数据库原始值
    label: zoneMapping[zone] || zone // 显示友好名称
  }))

  // 按字母排序
  zones.sort((a, b) => a.label.localeCompare(b.label))

  return zones
})

// 计算属性 - 柱形图相关
const chartZones = computed(() => {
  const version = dataVersion.value // 添加依赖跟踪
  // 只显示有车位的区域
  return availableZones.value.filter(zone => getZoneTotalCount(zone.value) > 0)
})

const yAxisTicks = computed(() => {
  const version = dataVersion.value // 添加依赖跟踪
  const maxValue = Math.max(
    ...chartZones.value.map(zone => getZoneTotalCount(zone.value))
  )
  const step = Math.ceil(maxValue / 5)
  const ticks = []

  for (let i = 0; i <= 5; i++) {
    ticks.push(i * step)
  }

  return ticks.reverse()
})

const barScale = computed(() => {
  const version = dataVersion.value // 添加依赖跟踪
  const maxValue = Math.max(
    ...chartZones.value.map(zone => getZoneTotalCount(zone.value))
  )
  return maxValue > 0 ? 100 / maxValue : 1
})
const getZoneUtilizationPercentage = (zone) => {
  const total = getZoneTotalCount(zone)
  const available = getZoneAvailableCount(zone)
  if (total === 0) return 0
  return Math.round(((total - available) / total) * 100)
}
const getZoneAvailablePercentage = (zone) => {
  const total = getZoneTotalCount(zone)
  if (total === 0) return 100 // 如果没有车位，显示全绿
  const available = getZoneAvailableCount(zone)
  return (available / total) * 100
}

const getZoneOccupiedPercentage = (zone) => {
  const total = getZoneTotalCount(zone)
  if (total === 0) return 0
  const occupied = getZoneOccupiedCount(zone)
  return (occupied / total) * 100
}
const getZoneMaintenancePercentage = (zone) => {
  const total = getZoneTotalCount(zone)
  if (total === 0) return 0
  const maintenance = parkingSpots.value.filter(spot =>
    spot.zone === zone && spot.status === 'MAINTENANCE'
  ).length
  return (maintenance / total) * 100
}
const averageUtilization = computed(() => {
  const version = dataVersion.value // 添加依赖跟踪
  const zonesWithData = chartZones.value.filter(zone => getZoneTotalCount(zone.value) > 0)
  if (zonesWithData.length === 0) return 0

  const totalUtilization = zonesWithData.reduce((sum, zone) => {
    return sum + getZoneUtilization(zone.value)
  }, 0)

  return Math.round(totalUtilization / zonesWithData.length)
})

const maxUtilization = computed(() => {
  const version = dataVersion.value // 添加依赖跟踪
  if (chartZones.value.length === 0) return 0
  const utilizations = chartZones.value.map(zone => getZoneUtilization(zone.value))
  return Math.max(...utilizations)
})

const minUtilization = computed(() => {
  const version = dataVersion.value // 添加依赖跟踪
  if (chartZones.value.length === 0) return 0
  const utilizations = chartZones.value.map(zone => getZoneUtilization(zone.value))
  return Math.min(...utilizations)
})

const stats = computed(() => {
  const total = parkingSpots.value.length
  const available = parkingSpots.value.filter(s => s.status === 'AVAILABLE').length
  const occupied = parkingSpots.value.filter(s => s.status === 'OCCUPIED').length
  const maintenance = parkingSpots.value.filter(s => s.status === 'MAINTENANCE').length

  return { total, available, occupied, maintenance }
})

const utilizationRate = computed(() => {
  const { total, available } = stats.value
  if (total === 0) return 0
  return Math.round(((total - available) / total) * 100)
})

// 原有的计算属性保持不变，添加新的区域统计方法
const getZoneAvailableCount = (zone) => {
  const version = dataVersion.value // 添加依赖跟踪
  return parkingSpots.value.filter(spot =>
    spot.zone === zone && spot.status === 'AVAILABLE'
  ).length
}

const getZoneOccupiedCount = (zone) => {
  const version = dataVersion.value // 添加依赖跟踪
  return parkingSpots.value.filter(spot =>
    spot.zone === zone && spot.status === 'OCCUPIED'
  ).length
}

const getZoneTotalCount = (zone) => {
  const version = dataVersion.value // 添加依赖跟踪
  return parkingSpots.value.filter(spot => spot.zone === zone).length
}

const getZoneUtilization = (zone) => {
  const version = dataVersion.value // 添加依赖跟踪
  const total = getZoneTotalCount(zone)
  const available = getZoneAvailableCount(zone)
  if (total === 0) return 0
  return Math.round(((total - available) / total) * 100)
}

const getZoneSpots = (zone) => {
  const version = dataVersion.value // 添加依赖跟踪
  return parkingSpots.value.filter(spot => spot.zone === zone)
}

// 原有的计算属性
const filteredSpots = computed(() => {
  let result = parkingSpots.value

  if (filters.value.zone) {
    result = result.filter(spot => spot.zone === filters.value.zone)
  }

  if (filters.value.status) {
    result = result.filter(spot => spot.status === filters.value.status)
  }

  if (filters.value.spotType) {
    result = result.filter(spot => spot.spotType === filters.value.spotType)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(spot =>
      spot.spotNumber.toLowerCase().includes(keyword) ||
      (spot.zone && spot.zone.toLowerCase().includes(keyword))
    )
  }

  result.sort((a, b) => {
    const aValue = a[sortField.value]
    const bValue = b[sortField.value]

    if (typeof aValue === 'string' && typeof bValue === 'string') {
      return sortDirection.value === 'asc'
        ? aValue.localeCompare(bValue)
        : bValue.localeCompare(aValue)
    } else {
      return sortDirection.value === 'asc'
        ? (aValue || 0) - (bValue || 0)
        : (bValue || 0) - (aValue || 0)
    }
  })

  return result
})

const paginatedSpots = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredSpots.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredSpots.value.length / pageSize.value)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const showEllipsis = computed(() => {
  return totalPages.value > 5 && currentPage.value < totalPages.value - 2
})

const selectedSpotsPreview = computed(() => {
  return selectedSpots.value.slice(0, 5)
})

const lastUpdateTime = computed(() => {
  const now = new Date()
  return now.toLocaleString('zh-CN')
})

// 方法 - 保持原有逻辑，添加新方法
const getZoneCount = (zone) => {
  const version = dataVersion.value // 添加依赖跟踪
  return parkingSpots.value.filter(spot => spot.zone === zone).length
}

const getStatusText = (status) => {
  const statusMap = {
    'AVAILABLE': '空闲',
    'OCCUPIED': '占用',
    'MAINTENANCE': '维护',
  }
  return statusMap[status] || status
}

const getTypeText = (type) => {
  const typeMap = {
    'REGULAR': '普通',
    'DISABLED': '残疾人',
    'RESERVED': '预留'
  }
  return typeMap[type] || type
}

const formatUpdateTime = (timestamp) => {
  if (!timestamp) return '--'
  const date = new Date(timestamp)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const isSelected = (spotId) => {
  return selectedSpots.value.some(s => s.id === spotId)
}

const toggleFilterPanel = () => {
  isFilterPanelOpen.value = !isFilterPanelOpen.value
}

const selectSpot = (spot) => {
  // 可以添加点击车位的处理逻辑
  console.log('选中车位:', spot)
}

// 原有的所有方法都保持不变
onMounted(() => {
  console.log('🔄 组件挂载，开始加载数据...')
  loadParkingSpots()

  // 3秒后再次检查数据状态
  setTimeout(() => {
    console.log('⏰ 3秒后数据状态检查:')
    console.log('   停车位数量:', parkingSpots.value.length)
    console.log('   所有区域:', availableZones.value)
  }, 3000)
})

const loadParkingSpots = async () => {
  try {
    console.log('🚀 开始请求停车位数据...')
    console.log('请求URL:', '/api/parking-spots')

    // 先测试不带认证的请求
    const testResponse = await fetch('/api/parking-spots')
    console.log('测试请求状态:', testResponse.status)

    // 再使用 authFetch
    const response = await authFetch('/api/parking-spots')
    console.log('📡 authFetch响应状态:', response.status, response.statusText)

    if (response.ok) {
      const data = await response.json()
      console.log('✅ 接收到停车位数据:')
      console.log('   数据条数:', data.length)
      console.log('   数据类型:', typeof data)

      if (data.length > 0) {
        console.log('   第一条数据完整结构:', JSON.stringify(data[0], null, 2))
        console.log('   可用字段:', Object.keys(data[0]))
        console.log('   spotNumber:', data[0].spotNumber)
        console.log('   zone:', data[0].zone)
        console.log('   status:', data[0].status)
        console.log('   hourlyRate:', data[0].hourlyRate)
      }

      parkingSpots.value = data
      currentPage.value = 1
      selectedSpots.value = []
      dataVersion.value++

      console.log('🔄 前端数据已更新:')
      console.log('   parkingSpots长度:', parkingSpots.value.length)
      console.log('   availableZones:', availableZones.value)
    } else {
      console.error('❌ 加载停车位失败，状态码:', response.status)
      const errorText = await response.text()
      console.error('错误信息:', errorText)
      alert('加载停车位失败: ' + response.status)
    }
  } catch (error) {
    console.error('❌ 加载停车位失败:', error)
    console.error('错误详情:', error.message)
    alert('加载停车位失败: ' + error.message)
  }
}

const refreshData = () => {
  loadParkingSpots()
  alert('数据已刷新，图表已更新')
}

const toggleZoneFilter = (zone) => {
  filters.value.zone = filters.value.zone === zone ? '' : zone
  currentPage.value = 1
}

const toggleStatusFilter = (status) => {
  filters.value.status = filters.value.status === status ? '' : status
  currentPage.value = 1
}

const handleSearch = debounce(() => {
  currentPage.value = 1
}, 300)

const clearFilters = () => {
  filters.value = { zone: '', status: '', spotType: '' }
  searchKeyword.value = ''
  currentPage.value = 1
}

const sortBy = (field) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'asc'
  }
}

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}

const toggleSelectSpot = (spot) => {
  const index = selectedSpots.value.findIndex(s => s.id === spot.id)
  if (index > -1) {
    selectedSpots.value.splice(index, 1)
  } else {
    selectedSpots.value.push(spot)
  }
}

const toggleSelectAll = (event) => {
  if (event.target.checked) {
    selectedSpots.value = [...paginatedSpots.value]
  } else {
    selectedSpots.value = []
  }
}

const clearSelection = () => {
  selectedSpots.value = []
}

const showAddForm = () => {
  isEditing.value = false
  editForm.value = {
    spotNumber: '',
    zone: '',
    spotType: 'REGULAR',
    status: 'AVAILABLE',
    hourlyRate: '5.00',
    remark: ''
  }
  spotNumberError.value = '' // 重置错误
  showAddDialog.value = true
}

const editSpot = (spot) => {
  isEditing.value = true
  editForm.value = { ...spot }
  editForm.value.hourlyRate = editForm.value.hourlyRate || '5.00'
  editForm.value.remark = editForm.value.remark || ''
  spotNumberError.value = '' // 重置错误
  showAddDialog.value = true
}

const saveSpot = async () => {
  if (!editForm.value.spotNumber || !editForm.value.zone) {
    alert('请填写完整的车位信息')
    return
  }

  // 🔧 新增：检查车位编号是否已存在（添加时检查）
  if (!isEditing.value) {
    const existingSpot = parkingSpots.value.find(
      spot => spot.spotNumber === editForm.value.spotNumber.trim()
    )

    if (existingSpot) {
      alert(`车位编号 "${editForm.value.spotNumber}" 已存在！`)
      return
    }
  }

  // 🔧 新增：检查编辑时是否修改了编号导致重复
  if (isEditing.value) {
    const originalSpot = parkingSpots.value.find(spot => spot.id === editForm.value.id)
    if (originalSpot && originalSpot.spotNumber !== editForm.value.spotNumber) {
      const existingSpot = parkingSpots.value.find(
        spot => spot.spotNumber === editForm.value.spotNumber.trim() && spot.id !== editForm.value.id
      )

      if (existingSpot) {
        alert(`车位编号 "${editForm.value.spotNumber}" 已存在！无法修改。`)
        return
      }
    }
  }

  try {
    isSubmitting.value = true

    // 构建请求数据
    const requestData = {
      spotNumber: editForm.value.spotNumber.trim(),
      zone: editForm.value.zone,
      spotType: editForm.value.spotType || 'REGULAR',
      status: editForm.value.status || 'AVAILABLE',
      hourlyRate: editForm.value.hourlyRate, // 保持原样，后端会处理
      location: editForm.value.location || '未指定'
    }

    console.log('发送的数据:', JSON.stringify(requestData, null, 2)) // 调试用

    let url = '/api/parking-spots'
    let method = 'POST'

    if (isEditing.value) {
      url = `/api/parking-spots/${editForm.value.id}`
      method = 'PUT'
    }

    const response = await authFetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestData)
    })

    if (response.ok) {
      const savedSpot = await response.json()
      console.log('保存成功:', savedSpot)

      // 更新本地数据
      if (isEditing.value) {
        const index = parkingSpots.value.findIndex(s => s.id === savedSpot.id)
        if (index > -1) {
          parkingSpots.value[index] = savedSpot
        }
      } else {
        parkingSpots.value.push(savedSpot)
      }

      showAddDialog.value = false
      dataVersion.value++

      alert(isEditing.value ? '车位更新成功！' : '车位添加成功！')

    } else {
      const errorText = await response.text()
      console.error('服务器错误:', errorText)

      try {
        const errorJson = JSON.parse(errorText)
        alert(`错误：${errorJson.error}`)
      } catch {
        alert(`错误：${errorText}`)
      }
    }

  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败：' + error.message)
  } finally {
    isSubmitting.value = false
  }
}

const confirmDelete = (spot) => {
  spotToDelete.value = spot
  showDeleteConfirm.value = true
}
// 添加新的响应式变量
const spotNumberError = ref('')

// 添加检查方法
const checkSpotNumber = debounce(() => {
  const spotNumber = editForm.value.spotNumber.trim()

  if (!spotNumber) {
    spotNumberError.value = ''
    return
  }

  // 检查格式（可选）
  if (!/^[A-Za-z0-9\-_]+$/.test(spotNumber)) {
    spotNumberError.value = '车位编号只能包含字母、数字、中划线和下划线'
    return
  }

  // 检查是否已存在
  if (!isEditing.value) {
    const existing = parkingSpots.value.find(
      spot => spot.spotNumber.toLowerCase() === spotNumber.toLowerCase()
    )
    spotNumberError.value = existing ? `车位编号 "${spotNumber}" 已存在` : ''
  } else {
    // 编辑时检查，排除自身
    const existing = parkingSpots.value.find(
      spot => spot.spotNumber.toLowerCase() === spotNumber.toLowerCase()
        && spot.id !== editForm.value.id
    )
    spotNumberError.value = existing ? `车位编号 "${spotNumber}" 已存在` : ''
  }
}, 300)
const deleteSpot = async () => {
  try {
    const response = await authFetch(`/api/parking-spots/${spotToDelete.value.id}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      parkingSpots.value = parkingSpots.value.filter(s => s.id !== spotToDelete.value.id)
      selectedSpots.value = selectedSpots.value.filter(s => s.id !== spotToDelete.value.id)
      dataVersion.value++ // 增加版本号，触发图表刷新

      showDeleteConfirm.value = false
      alert('车位删除成功！图表已刷新')
    } else {
      const error = await response.json()
      alert('删除失败: ' + error.error)
    }
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败')
  }
}

const showStatusMenu = (spot, event) => {
  currentSpot.value = spot
  if (event) {
    statusMenuPosition.value = {
      top: event.clientY + 'px',
      left: event.clientX + 'px'
    }
  } else {
    statusMenuPosition.value = {
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)'
    }
  }
  showStatusMenuForSpot.value = true
}

const closeStatusMenu = () => {
  showStatusMenuForSpot.value = false
  currentSpot.value = null
}

const updateSpotStatus = async (newStatus) => {
  if (!currentSpot.value) return

  try {
    const response = await authFetch(`/api/parking-spots/${currentSpot.value.id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ status: newStatus })
    })

    if (response.ok) {
      const updatedSpot = await response.json()

      const index = parkingSpots.value.findIndex(s => s.id === updatedSpot.id)
      if (index > -1) {
        parkingSpots.value[index] = updatedSpot
      }

      dataVersion.value++ // 增加版本号，触发图表刷新
      closeStatusMenu()
      alert('状态更新成功！图表已刷新')
    } else {
      const error = await response.json()
      alert('状态更新失败: ' + error.error)
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    alert('状态更新失败')
  }
}

const bulkUpdateStatus = (status) => {
  if (selectedSpots.value.length === 0) {
    alert('请先选择要操作的车位')
    return
  }

  bulkAction.value = getStatusText(status)
  bulkActionClass.value = status.toLowerCase()
  showBulkConfirm.value = true

  currentBulkOperation.value = { type: 'updateStatus', data: status }
}

const bulkDelete = () => {
  if (selectedSpots.value.length === 0) {
    alert('请先选择要删除的车位')
    return
  }

  bulkAction.value = '批量删除'
  bulkActionClass.value = 'delete'
  showBulkConfirm.value = true

  currentBulkOperation.value = { type: 'delete', data: null }
}

const currentBulkOperation = ref(null)

const executeBulkAction = async () => {
  if (!currentBulkOperation.value) return

  try {
    if (currentBulkOperation.value.type === 'updateStatus') {
      const status = currentBulkOperation.value.data
      const spotIds = selectedSpots.value.map(spot => spot.id)

      const promises = spotIds.map(id =>
        authFetch(`/api/parking-spots/${id}/status`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ status: status })
        })
      )

      const responses = await Promise.allSettled(promises)
      let successCount = 0
      let failedIds = []

      responses.forEach((result, index) => {
        if (result.status === 'fulfilled' && result.value.ok) {
          successCount++
          const spotId = spotIds[index]
          const spotIndex = parkingSpots.value.findIndex(s => s.id === spotId)
          if (spotIndex > -1) {
            parkingSpots.value[spotIndex].status = status
          }
        } else {
          failedIds.push(spotIds[index])
        }
      })

      dataVersion.value++ // 增加版本号，触发图表刷新

      if (successCount === spotIds.length) {
        alert(`成功更新 ${successCount} 个车位的状态！图表已刷新`)
      } else if (successCount > 0) {
        alert(`成功更新 ${successCount} 个车位，失败 ${spotIds.length - successCount} 个，图表已刷新`)
      } else {
        alert('批量更新失败')
      }

    } else if (currentBulkOperation.value.type === 'delete') {
      const spotIds = selectedSpots.value.map(spot => spot.id)
      const promises = spotIds.map(id =>
        authFetch(`/api/parking-spots/${id}`, {
          method: 'DELETE'
        })
      )

      const responses = await Promise.allSettled(promises)
      let successCount = 0
      let failedIds = []

      responses.forEach((result, index) => {
        if (result.status === 'fulfilled' && result.value.ok) {
          successCount++
          const spotId = spotIds[index]
          parkingSpots.value = parkingSpots.value.filter(s => s.id !== spotId)
        } else {
          failedIds.push(spotIds[index])
        }
      })

      selectedSpots.value = []
      dataVersion.value++ // 增加版本号，触发图表刷新

      if (successCount === spotIds.length) {
        alert(`成功删除 ${successCount} 个车位！图表已刷新`)
      } else if (successCount > 0) {
        alert(`成功删除 ${successCount} 个车位，失败 ${spotIds.length - successCount} 个，图表已刷新`)
      } else {
        alert('批量删除失败')
      }
    }

    showBulkConfirm.value = false
    currentBulkOperation.value = null

  } catch (error) {
    console.error('批量操作失败:', error)
    alert('批量操作失败')
  }
}

const exportData = () => {
  const dataStr = JSON.stringify(parkingSpots.value, null, 2)
  const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr)

  const exportFileDefaultName = `parking-spots-${new Date().toISOString().split('T')[0]}.json`

  const linkElement = document.createElement('a')
  linkElement.setAttribute('href', dataUri)
  linkElement.setAttribute('download', exportFileDefaultName)
  linkElement.click()
}

function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}
</script>

<style scoped>
/* 柱形图面板样式 */
.zone-stats-panel.chart-panel {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
}

.chart-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.chart-type-toggle {
  display: flex;
  gap: 8px;
}

.chart-type-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  color: #6b7280;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.chart-type-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.chart-type-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.chart-legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-color.available {
  background: #10b981;
}

.legend-color.occupied {
  background: #ef4444;
}

.legend-color.total {
  background: #3b82f6;
}

.legend-label {
  font-size: 14px;
  color: #6b7280;
}

.chart-container {
  height: 320px;
  margin-bottom: 24px;
}

.chart-wrapper {
  display: flex;
  height: 100%;
  position: relative;
}

.y-axis {
  width: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-right: 12px;
  border-right: 1px solid #e5e7eb;
}

.y-tick {
  display: flex;
  align-items: center;
  position: relative;
  height: calc(100% / 6);
}

.tick-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.tick-line {
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 1px;
  background: #e5e7eb;
}

.chart-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.x-axis {
  display: flex;
  height: 40px;
  border-top: 1px solid #e5e7eb;
}

.x-tick {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.x-label {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
  text-align: center;
  transform: rotate(-45deg);
  white-space: nowrap;
  position: absolute;
  top: 8px;
}

.bars-container {
  flex: 1;
  display: flex;
  align-items: flex-end;
  position: relative;
  padding-bottom: 40px;
}

.bar-group {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  height: 100%;
  position: relative;
}

.stacked-bar {
  width: 60%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  border-radius: 6px 6px 0 0;
  overflow: hidden;
  background: #f3f4f6;
}

.bar-segment {
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.bar-segment.available {
  background: #10b981;
}

.bar-segment.occupied {
  background: #ef4444;
}

.bar-value {
  color: white;
  font-size: 12px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.grouped-bars {
  width: 80%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  position: relative;
}

.grouped-bar {
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
  position: relative;
}

.grouped-bar.available {
  background: #10b981;
  opacity: 0.9;
}

.grouped-bar.occupied {
  background: #ef4444;
  opacity: 0.9;
}

.grouped-bar.total {
  background: #3b82f6;
  opacity: 0.9;
}

.grouped-bar:hover {
  opacity: 1;
  transform: translateY(-2px);
}

.zone-name-bottom {
  position: absolute;
  bottom: -28px;
  width: 100%;
  text-align: center;
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chart-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.summary-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.summary-value {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.summary-item:nth-child(1) .summary-value {
  color: #3b82f6;
}

.summary-item:nth-child(2) .summary-value {
  color: #059669;
}

.summary-item:nth-child(3) .summary-value {
  color: #dc2626;
}

.summary-item:nth-child(4) .summary-value {
  color: #d97706;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .chart-controls {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .chart-type-toggle {
    justify-content: center;
  }

  .chart-legend {
    justify-content: center;
    flex-wrap: wrap;
  }

  .chart-container {
    height: 280px;
  }

  .x-label {
    font-size: 10px;
  }

  .zone-name-bottom {
    font-size: 10px;
  }

  .chart-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .chart-container {
    height: 240px;
  }

  .y-axis {
    width: 40px;
  }

  .x-label {
    transform: rotate(-90deg);
    top: 4px;
  }

  .stacked-bar {
    width: 80%;
  }

  .grouped-bars {
    width: 90%;
  }
}

/* 基础样式 - 白色背景 */
.parking-lot-management-container {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f8fafc 0%, #ffffff 100%);
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
}

/* 导航栏样式 */
.management-navbar {
  background: white;
  border-radius: 16px;
  padding: 20px 30px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-brand h1 {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

.navbar-subtitle {
  margin: 8px 0 0 0;
  color: #6b7280;
  font-size: 14px;
  font-weight: 400;
}

.navbar-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.nav-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.nav-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.user-avatar {
  font-size: 20px;
}

.user-name {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

/* 停车场概览卡片 */
.parking-lots-overview {
  margin-bottom: 24px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.overview-card.total .overview-icon {
  background: #dbeafe;
  color: #1d4ed8;
}

.overview-card.available .overview-icon {
  background: #d1fae5;
  color: #059669;
}

.overview-card.occupied .overview-icon {
  background: #fee2e2;
  color: #dc2626;
}

.overview-card.utilization .overview-icon {
  background: #fef3c7;
  color: #d97706;
}

.overview-count {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #6b7280;
}

/* 主要操作区 */
.main-operations {
  margin-bottom: 24px;
}

.operation-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.op-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  position: relative;
  overflow: hidden;
}

.op-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #3b82f6;
}

.op-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.add-card .op-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.stats-card .op-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.filter-card .op-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.export-card .op-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.op-content h3 {
  margin: 0 0 6px 0;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.op-content p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  font-weight: 400;
}

.op-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #f3f4f6;
  color: #374151;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

/* 区域统计面板 */
.zone-stats-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h4 {
  margin: 0;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.panel-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.panel-close:hover {
  color: #ef4444;
}

.zone-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.zone-stat-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.zone-name {
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
  font-size: 14px;
}

.zone-counts {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.count-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
}

.count-item.available {
  color: #059669;
}

.count-item.occupied {
  color: #dc2626;
}

.count-item.total {
  color: #4b5563;
}

.zone-utilization {
  display: flex;
  align-items: center;
  gap: 12px;
}

.utilization-bar {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.utilization-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981 0%, #f59e0b 50%, #ef4444 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.utilization-text {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
  min-width: 40px;
}

/* 筛选工具栏 */
.filter-toolbar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.zone-filters,
.status-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.zone-filter-btn,
.status-filter-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-weight: 400;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
  color: #4b5563;
}

.zone-filter-btn:hover,
.status-filter-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.zone-filter-btn.active,
.status-filter-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.status-filter-btn.available.active {
  background: #10b981;
  border-color: #10b981;
}

.status-filter-btn.occupied.active {
  background: #ef4444;
  border-color: #ef4444;
}

.status-filter-btn.maintenance.active {
  background: #f59e0b;
  border-color: #f59e0b;
}

.filter-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
}

.status-icon {
  font-size: 14px;
}

.search-box {
  position: relative;
  max-width: 300px;
}

.search-box input {
  width: 100%;
  padding: 10px 16px 10px 40px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: white;
  color: #374151;
}

.search-box input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 16px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.clear-filters-btn,
.apply-filters-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
}

.clear-filters-btn {
  background: #f3f4f6;
  color: #374151;
}

.clear-filters-btn:hover {
  background: #e5e7eb;
}

.apply-filters-btn {
  background: #3b82f6;
  color: white;
}

.apply-filters-btn:hover {
  background: #2563eb;
}

/* 视图控制 */
.view-controls {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-tabs {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
}

.view-tab {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  transition: all 0.2s ease;
}

.view-tab:hover {
  color: #374151;
  background: rgba(255, 255, 255, 0.8);
}

.view-tab.active {
  background: white;
  color: #111827;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tab-icon {
  font-size: 16px;
}

.view-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.records-per-page {
  display: flex;
  align-items: center;
  gap: 8px;
}

.records-per-page label {
  color: #6b7280;
  font-size: 14px;
}

.records-per-page select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  font-size: 14px;
}

/* 卡片视图 */
.parking-lot-cards-view {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.parking-spot-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
  cursor: pointer;
}

.parking-spot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.parking-spot-card.available {
  border-left: 4px solid #10b981;
}

.parking-spot-card.occupied {
  border-left: 4px solid #ef4444;
}

.parking-spot-card.maintenance {
  border-left: 4px solid #f59e0b;
}

.spot-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.spot-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
  margin-top: 4px;
}

.spot-title h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.spot-id {
  font-size: 11px;
  color: #9ca3af;
  display: block;
  margin-top: 2px;
}

.spot-actions {
  display: flex;
  gap: 4px;
}

.spot-action-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
  color: #6b7280;
}

.spot-action-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.spot-action-btn.delete:hover {
  background: #fee2e2;
  border-color: #fecaca;
  color: #dc2626;
}

.spot-card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.spot-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  color: #6b7280;
  font-size: 12px;
  font-weight: 400;
}

.info-value {
  font-weight: 500;
  color: #374151;
  font-size: 13px;
}

.zone-badge {
  background: #dbeafe;
  color: #1d4ed8;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-badge,
.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-badge.regular {
  background: #f3f4f6;
  color: #4b5563;
}

.type-badge.disabled {
  background: #fef3c7;
  color: #d97706;
}

.type-badge.reserved {
  background: #f3e8ff;
  color: #7c3aed;
}

.status-badge.available {
  background: #d1fae5;
  color: #059669;
}

.status-badge.occupied {
  background: #fee2e2;
  color: #dc2626;
}

.status-badge.maintenance {
  background: #fef3c7;
  color: #d97706;
}

.rate-value {
  color: #ea580c;
  font-weight: 600;
  font-size: 14px;
}

.spot-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.spot-footer-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.footer-label {
  font-size: 11px;
  color: #9ca3af;
}

.footer-value {
  font-size: 12px;
  color: #6b7280;
  font-weight: 400;
}

/* 区域视图 */
.parking-lot-map-view {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.map-view-header {
  margin-bottom: 24px;
}

.map-view-header h4 {
  margin: 0 0 8px 0;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.map-subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.zones-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.zone-section {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.zone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.zone-header h5 {
  margin: 0;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.zone-stats {
  display: flex;
  gap: 12px;
}

.zone-stat {
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.zone-stat.available {
  color: #059669;
}

.zone-stat.occupied {
  color: #dc2626;
}

.zone-stat.total {
  color: #4b5563;
}

.zone-spots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  gap: 8px;
}

.zone-spot-item {
  background: white;
  border-radius: 6px;
  padding: 8px;
  text-align: center;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.zone-spot-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.zone-spot-item.available {
  background: #d1fae5;
  border-color: #a7f3d0;
}

.zone-spot-item.occupied {
  background: #fee2e2;
  border-color: #fecaca;
}

.zone-spot-item.maintenance {
  background: #fef3c7;
  border-color: #fde68a;
}

.spot-number {
  font-size: 12px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.spot-status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 0 auto;
}

.zone-spot-item.available .spot-status-indicator {
  background: #059669;
}

.zone-spot-item.occupied .spot-status-indicator {
  background: #dc2626;
}

.zone-spot-item.maintenance .spot-status-indicator {
  background: #d97706;
}

/* 表格视图 - 原有样式微调 */
.parking-lot-table-view {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.table-responsive {
  overflow-x: auto;
}

.parking-spots-table {
  width: 100%;
  border-collapse: collapse;
}

.parking-spots-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 2px solid #e5e7eb;
  white-space: nowrap;
  font-size: 14px;
}

.parking-spots-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  vertical-align: middle;
  font-size: 14px;
}

.parking-spots-table tbody tr {
  transition: background-color 0.2s ease;
}

.parking-spots-table tbody tr:hover {
  background: #f9fafb;
}

.parking-spots-table tbody tr.selected {
  background: #eff6ff;
}

.parking-spots-table tbody tr.available {
  background: rgba(209, 250, 229, 0.2);
}

.parking-spots-table tbody tr.occupied {
  background: rgba(254, 226, 226, 0.2);
}

.parking-spots-table tbody tr.maintenance {
  background: rgba(254, 243, 199, 0.2);
}

/* 保持原有的表格相关样式 */
.sortable {
  cursor: pointer;
  position: relative;
  user-select: none;
}

.sortable:hover {
  background: #f3f4f6;
}

.sort-indicator {
  margin-left: 6px;
  font-weight: bold;
  font-size: 12px;
}

.checkbox-column {
  width: 40px;
  text-align: center;
}

.checkbox-column input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.spot-number-cell strong {
  font-size: 14px;
  color: #111827;
}

.zone-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #dbeafe;
  color: #1d4ed8;
}

.zone-tag.zone-a { background: #dbeafe; color: #1d4ed8; }
.zone-tag.zone-b { background: #d1fae5; color: #059669; }
.zone-tag.zone-c { background: #f3e8ff; color: #7c3aed; }

.action-cell {
  min-width: 180px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.edit-btn {
  background: #dbeafe;
  color: #1d4ed8;
}

.edit-btn:hover {
  background: #bfdbfe;
}

.status-btn {
  background: #fef3c7;
  color: #d97706;
}

.status-btn:hover {
  background: #fde68a;
}

.delete-btn {
  background: #fee2e2;
  color: #dc2626;
}

.delete-btn:hover {
  background: #fecaca;
}

/* 分页控件 */
.pagination-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  color: #6b7280;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
  color: #374151;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #3b82f6;
  color: #3b82f6;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
  align-items: center;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
  color: #374151;
}

.page-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.page-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.page-ellipsis {
  color: #9ca3af;
  padding: 0 8px;
}

/* 批量操作栏 */
.bulk-actions-bar {
  background: #1f2937;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.selected-count {
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.clear-selection-btn {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s ease;
}

.clear-selection-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.bulk-actions {
  display: flex;
  gap: 8px;
}

.bulk-action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  color: white;
  transition: all 0.2s ease;
}

.bulk-action-btn.available {
  background: #10b981;
}

.bulk-action-btn.available:hover {
  background: #059669;
}

.bulk-action-btn.occupied {
  background: #ef4444;
}

.bulk-action-btn.occupied:hover {
  background: #dc2626;
}

.bulk-action-btn.maintenance {
  background: #f59e0b;
}

.bulk-action-btn.maintenance:hover {
  background: #d97706;
}

.bulk-action-btn.delete {
  background: #dc2626;
}

.bulk-action-btn.delete:hover {
  background: #b91c1c;
}

/* 模态对话框样式 - 保持原有样式 */
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
  backdrop-filter: blur(2px);
}

.modal-dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid #e5e7eb;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.modal-close:hover {
  color: #ef4444;
}

.modal-body {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width {
  grid-column: span 2;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: all 0.2s ease;
  background: white;
  color: #374151;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-hint {
  color: #9ca3af;
  font-size: 12px;
  font-weight: 400;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn-cancel,
.btn-primary,
.btn-danger,
.btn-confirm {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover {
  background: #dc2626;
}

/* 状态菜单样式 */
.status-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.status-menu {
  background: white;
  border-radius: 12px;
  padding: 20px;
  min-width: 200px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border: 1px solid #e5e7eb;
}

.status-menu h4 {
  margin: 0 0 16px 0;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.status-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.status-option-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: #f9fafb;
  cursor: pointer;
  font-weight: 400;
  font-size: 14px;
  transition: all 0.2s ease;
  text-align: left;
  color: #374151;
}

.status-option-btn:hover {
  background: #f3f4f6;
  transform: translateX(2px);
}

.status-option-btn.available {
  color: #059669;
}

.status-option-btn.occupied {
  color: #dc2626;
}

.status-option-btn.maintenance {
  color: #d97706;
}

.status-option-icon {
  font-size: 16px;
}

.status-menu-close {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
  color: #374151;
}

.status-menu-close:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

/* 确认对话框样式 */
.confirm-dialog {
  background: white;
  border-radius: 12px;
  padding: 24px;
  max-width: 400px;
  width: 90%;
  border: 1px solid #e5e7eb;
}

.confirm-header {
  margin-bottom: 20px;
}

.confirm-header h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.confirm-body {
  margin-bottom: 24px;
}

.confirm-body p {
  margin: 0 0 12px 0;
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
}

.confirm-warning {
  color: #ef4444 !important;
  font-weight: 500;
  font-size: 13px;
}

.confirm-action {
  background: #f9fafb;
  padding: 12px;
  border-radius: 8px;
  margin: 16px 0;
  text-align: center;
  font-size: 15px;
  color: #111827;
}

.selected-spots-preview {
  max-height: 150px;
  overflow-y: auto;
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-top: 12px;
}

.selected-spot-item {
  padding: 6px 10px;
  background: white;
  border-radius: 6px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.selected-spot-item:last-child {
  margin-bottom: 0;
}

.more-spots {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 8px;
  font-weight: 400;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 底部状态栏 */
.status-bar {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #6b7280;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
}

.status-indicator.active {
  background: #10b981;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}

.status-update {
  font-size: 13px;
  color: #9ca3af;
}

.status-tips {
  font-size: 13px;
  color: #3b82f6;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .parking-lot-management-container {
    padding: 16px;
  }

  .management-navbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .navbar-actions {
    justify-content: space-between;
  }

  .overview-cards,
  .operation-cards {
    grid-template-columns: 1fr;
  }

  .view-controls {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .view-tabs {
    overflow-x: auto;
    padding: 4px;
  }

  .cards-grid {
    grid-template-columns: 1fr;
  }

  .zone-stats-grid {
    grid-template-columns: 1fr;
  }

  .filter-group {
    flex-direction: column;
  }

  .zone-filters,
  .status-filters {
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-group.full-width {
    grid-column: span 1;
  }

  .bulk-actions-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .bulk-actions {
    flex-wrap: wrap;
    justify-content: center;
  }

  .status-bar {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }
}

/* 适配小屏幕 */
@media (max-width: 480px) {
  .parking-lot-management-container {
    padding: 12px;
  }

  .modal-dialog {
    width: 95%;
    margin: 12px;
  }

  .zone-spots-grid {
    grid-template-columns: repeat(auto-fill, minmax(50px, 1fr));
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }

  .action-btn {
    padding: 6px;
    font-size: 12px;
  }
}
</style>
