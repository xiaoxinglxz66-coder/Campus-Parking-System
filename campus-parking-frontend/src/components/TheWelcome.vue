<script setup>
// 确保这里只有一个 script setup 标签
import WelcomeItem from './WelcomeItem.vue'
import DocumentationIcon from './icons/IconDocumentation.vue'
import ToolingIcon from './icons/IconTooling.vue'
import EcosystemIcon from './icons/IconEcosystem.vue'
import CommunityIcon from './icons/IconCommunity.vue'
import SupportIcon from './icons/IconSupport.vue'
</script>

<template>
  <div class="welcome">
    <WelcomeItem>
      <template #icon>
        <DocumentationIcon />
      </template>
      <template #heading>校园停车</template>
      停车位管理功能开发中...
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <ToolingIcon />
      </template>
      <template #heading>用户管理</template>
      用户注册登录功能开发中...
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <EcosystemIcon />
      </template>
      <template #heading>车辆管理</template>
      车辆信息管理功能开发中...
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <CommunityIcon />
      </template>
      <template #heading>费用计算</template>
      停车费用计算功能开发中...
    </WelcomeItem>

    <WelcomeItem>
      <template #icon>
        <SupportIcon />
      </template>
      <template #heading>数据统计</template>
      数据统计分析功能开发中...
    </WelcomeItem>
  </div>
</template>

<style>
.welcome {
  margin: 20px 0;
}
</style>
