<template>
  <div class="guest-guide">
    <!-- 头部导航 -->
    <header class="guide-header">
      <div class="header-content">
        <div class="logo">
          <i class="fas fa-parking"></i>
          <span>校园停车</span>
        </div>
        <nav class="nav-links">
          <button @click="goToParking" class="nav-btn">
            <i class="fas fa-map-marker-alt"></i>
            停车场
          </button>
          <button @click="goToTempParking" class="nav-btn primary">
            <i class="fas fa-play"></i>
            立即停车
          </button>
        </nav>
      </div>
    </header>

    <!-- 主要内容 -->
    <main class="guide-main">
      <!-- 英雄区域 -->
      <section class="hero">
        <div class="hero-content">
          <div class="hero-text">
            <div class="badges">
              <span class="badge new">全新体验</span>
              <span class="badge free">免费使用</span>
            </div>
            <h1 class="hero-title">
              智慧停车
              <span class="highlight">轻松出行</span>
            </h1>
            <p class="hero-desc">
              无需注册，输入车牌立即停车<br>
              实时车位查询，智能费用计算
            </p>
            <div class="hero-actions">
              <button @click="goToTempParking" class="btn primary large">
                <i class="fas fa-bolt"></i>
                立即体验
              </button>
              <button @click="scrollToFeatures" class="btn secondary">
                了解功能
              </button>
            </div>
          </div>
          <div class="hero-visual">
            <div class="demo-card">
              <div class="card-header">
                <i class="fas fa-car"></i>
                <h3>临时停车</h3>
              </div>
              <div class="license-plate">京A·88888</div>
              <div class="parking-info">
                <div class="info-item">
                  <i class="fas fa-clock"></i>
                  <span>14:30 开始</span>
                </div>
                <div class="info-item">
                  <i class="fas fa-money-bill-wave"></i>
                  <span>5元/小时</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 核心功能 -->
      <section id="features" class="features">
        <div class="section-header">
          <h2>核心功能</h2>
          <p>专为访客设计的便捷停车体验</p>
        </div>
        <div class="features-grid">
          <div v-for="feature in features" :key="feature.id" class="feature-card">
            <div class="feature-icon" :style="{ background: feature.color }">
              <i :class="feature.icon"></i>
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
            <ul class="feature-list">
              <li v-for="item in feature.items" :key="item">
                <i class="fas fa-check"></i>
                {{ item }}
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 使用流程 -->
      <section class="process">
        <div class="section-header">
          <h2>使用流程</h2>
          <p>简单四步，快速完成停车</p>
        </div>
        <div class="process-steps">
          <div v-for="(step, index) in steps" :key="step.id" class="step">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <h3>{{ step.title }}</h3>
              <p>{{ step.desc }}</p>
              <div class="step-tip">
                <i class="fas fa-lightbulb"></i>
                {{ step.tip }}
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 价格对比 -->
      <section class="pricing">
        <div class="section-header">
          <h2>收费标准</h2>
          <p>透明计费，无隐藏费用</p>
        </div>
        <div class="pricing-cards">
          <div class="price-card">
            <div class="card-header">
              <i class="fas fa-user-clock"></i>
              <h3>访客停车</h3>
            </div>
            <div class="price">5元/小时</div>
            <ul class="price-features">
              <li><i class="fas fa-check"></i>首小时5元起</li>
              <li><i class="fas fa-check"></i>每日封顶30元</li>
              <li><i class="fas fa-check"></i>实时费用计算</li>
              <li><i class="fas fa-times"></i>无预约功能</li>
            </ul>
            <button @click="goToTempParking" class="btn primary">
              立即体验
            </button>
          </div>

          <div class="price-card featured">
            <div class="card-badge">推荐</div>
            <div class="card-header">
              <i class="fas fa-crown"></i>
              <h3>校内用户</h3>
            </div>
            <div class="price">免费</div>
            <ul class="price-features">
              <li><i class="fas fa-check"></i>完全免费停车</li>
              <li><i class="fas fa-check"></i>车位预约特权</li>
              <li><i class="fas fa-check"></i>多车辆管理</li>
              <li><i class="fas fa-check"></i>停车记录统计</li>
            </ul>
            <button @click="goToLogin" class="btn secondary">
              升级账号
            </button>
          </div>
        </div>
      </section>

      <!-- 常见问题 -->
      <section class="faq">
        <div class="section-header">
          <h2>常见问题</h2>
          <p>快速解答您的疑问</p>
        </div>
        <div class="faq-list">
          <div v-for="(item, index) in faqs" :key="index" class="faq-item">
            <div class="faq-question" @click="toggleFaq(index)">
              <h4>{{ item.question }}</h4>
              <i class="fas fa-chevron-down" :class="{ rotated: activeFaq === index }"></i>
            </div>
            <div v-show="activeFaq === index" class="faq-answer">
              <p>{{ item.answer }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 底部行动 -->
      <section class="cta">
        <div class="cta-content">
          <h2>准备好开始停车了吗？</h2>
          <p>选择适合您的使用方式</p>
          <div class="cta-buttons">
            <button @click="goToTempParking" class="btn primary large">
              <i class="fas fa-play-circle"></i>
              立即停车体验
            </button>
            <button @click="goToLogin" class="btn secondary">
              <i class="fas fa-user-plus"></i>
              注册完整账号
            </button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeFaq = ref(0)

// 功能数据
const features = reactive([
  {
    id: 1,
    title: '实时车位查询',
    desc: '查看所有停车场的实时空位信息',
    icon: 'fas fa-map-marker-alt',
    color: '#6366f1',
    items: ['空位数量实时更新', '停车场位置信息', '收费标准透明']
  },
  {
    id: 2,
    title: '快速临时停车',
    desc: '无需注册账号，输入车牌即可使用',
    icon: 'fas fa-car',
    color: '#10b981',
    items: ['免注册立即使用', '车牌识别记录', '自动费用计算']
  },
  {
    id: 3,
    title: '智能费用计算',
    desc: '实时显示停车费用，离场自动结算',
    icon: 'fas fa-calculator',
    color: '#f59e0b',
    items: ['实时费用显示', '多种支付方式', '电子发票开具']
  }
])

// 步骤数据
const steps = reactive([
  {
    id: 1,
    title: '查找停车场',
    desc: '查看附近停车场的实时空位信息',
    tip: '选择距离目的地最近的停车场'
  },
  {
    id: 2,
    title: '输入车牌',
    desc: '准确输入您的车牌号码开始计时',
    tip: '确保车牌号准确无误'
  },
  {
    id: 3,
    title: '确认停车',
    desc: '选择车位并确认开始停车',
    tip: '确认停车场和车位信息'
  },
  {
    id: 4,
    title: '离场结算',
    desc: '停车结束后自动计算费用并支付',
    tip: '支持多种支付方式'
  }
])

// 常见问题
const faqs = reactive([
  {
    question: '访客停车需要注册账号吗？',
    answer: '不需要。访客可以直接使用临时停车功能，只需输入车牌号码即可开始停车，无需注册账号。'
  },
  {
    question: '停车费用如何计算？',
    answer: '访客停车按小时计费，首小时5元，后续每小时3元，每日封顶30元。不足1小时按1小时计算。'
  },
  {
    question: '可以提前预约车位吗？',
    answer: '访客模式不支持车位预约功能。如需预约车位，请注册为校内用户或使用完整账号登录。'
  },
  {
    question: '支持哪些支付方式？',
    answer: '支持微信支付、支付宝、银联云闪付等多种电子支付方式，支付完成后可开具电子发票。'
  }
])

// 方法
const toggleFaq = (index) => {
  activeFaq.value = activeFaq.value === index ? -1 : index
}

const scrollToFeatures = () => {
  const element = document.getElementById('features')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

const goToParking = () => {
  alert('即将跳转到停车场页面')
}

const goToTempParking = () => {
  alert('即将跳转到临时停车页面')
}

const goToLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.guest-guide {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

/* 头部样式 */
.guide-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e5e7eb;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: #6366f1;
}

.logo i {
  font-size: 1.75rem;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.nav-btn.primary {
  background: #6366f1;
  color: white;
  border-color: #6366f1;
}

.nav-btn.primary:hover {
  background: #5b5cd9;
}

/* 英雄区域 */
.hero {
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
}

.badges {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge.new {
  background: #6366f1;
  color: white;
}

.badge.free {
  background: #10b981;
  color: white;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1rem;
  color: #1f2937;
}

.highlight {
  color: #6366f1;
  font-weight: 700;
  position: relative;
}

.highlight::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 2px;
}

.hero-desc {
  font-size: 1.25rem;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.hero-actions {
  display: flex;
  gap: 1rem;
}

.btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn.primary {
  background: #6366f1;
  color: white;
}

.btn.primary:hover {
  background: #5b5cd9;
  transform: translateY(-2px);
}

.btn.secondary {
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.btn.secondary:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.btn.large {
  padding: 1.25rem 2.5rem;
  font-size: 1.1rem;
}

/* 演示卡片 */
.hero-visual {
  display: flex;
  justify-content: center;
}

.demo-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  width: 300px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.card-header i {
  color: #6366f1;
  font-size: 1.5rem;
}

.card-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.license-plate {
  background: #1f2937;
  color: white;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-family: monospace;
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.parking-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #6b7280;
}

.info-item i {
  color: #6366f1;
  width: 16px;
}

/* 通用区域样式 */
section {
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.section-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.section-header p {
  font-size: 1.2rem;
  color: #6b7280;
}

/* 功能区域 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
  margin-bottom: 1.5rem;
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}

.feature-card p {
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  color: #374151;
}

.feature-list i {
  color: #10b981;
  font-size: 0.8rem;
}

/* 流程区域 */
.process-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.step {
  text-align: center;
  padding: 2rem;
}

.step-number {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 auto 1.5rem;
}

.step h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}

.step p {
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.step-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: #f59e0b;
  font-size: 0.9rem;
}

/* 价格区域 */
.pricing-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.price-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid #e5e7eb;
  text-align: center;
  position: relative;
  transition: all 0.3s ease;
}

.price-card.featured {
  border-color: #6366f1;
  transform: scale(1.05);
}

.price-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.price-card.featured:hover {
  transform: scale(1.05) translateY(-5px);
}

.card-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: #6366f1;
  color: white;
  padding: 0.5rem 1.5rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.price-card .card-header {
  justify-content: center;
  margin-bottom: 1.5rem;
}

.price-card .card-header i {
  font-size: 2rem;
}

.price {
  font-size: 2.5rem;
  font-weight: 700;
  color: #6366f1;
  margin-bottom: 1.5rem;
}

.price-features {
  list-style: none;
  padding: 0;
  margin-bottom: 2rem;
}

.price-features li {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  justify-content: center;
}

.price-features i {
  font-size: 0.8rem;
}

.price-features .fa-check {
  color: #10b981;
}

.price-features .fa-times {
  color: #ef4444;
}

/* 常见问题 */
.faq-list {
  max-width: 800px;
  margin: 0 auto;
}

.faq-item {
  background: white;
  border-radius: 12px;
  margin-bottom: 1rem;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.faq-question {
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background 0.3s ease;
}

.faq-question:hover {
  background: #f8fafc;
}

.faq-question h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.faq-question i {
  color: #6366f1;
  transition: transform 0.3s ease;
}

.faq-question i.rotated {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 0 1.5rem 1.5rem;
  color: #6b7280;
  line-height: 1.6;
}

/* 底部行动 */
.cta {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  text-align: center;
  border-radius: 20px;
  margin: 2rem;
}

.cta-content {
  padding: 4rem 2rem;
}

.cta h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.cta p {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.cta .btn.primary {
  background: white;
  color: #6366f1;
}

.cta .btn.secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .pricing-cards {
    grid-template-columns: 1fr;
  }

  .price-card.featured {
    transform: none;
  }

  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }

  .cta .btn {
    width: 100%;
    max-width: 300px;
  }
}
</style>
