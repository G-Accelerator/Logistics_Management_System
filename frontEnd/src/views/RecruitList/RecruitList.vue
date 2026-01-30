<template>
  <div class="recruit-page">
    <!-- 顶部导航区 -->
    <div class="header-section">
      <!-- 状态栏 -->
      <div class="status-bar">
        <span class="time">9:41</span>
        <div class="status-icons">
          <span class="signal">📶</span>
          <span class="wifi">📡</span>
          <span class="battery">🔋</span>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="work-record-icon">
          <span>记工</span>
        </div>
        <div class="search-input">
          <span class="search-icon">🔍</span>
          <span class="placeholder">搜索占位文字</span>
          <span class="search-btn">搜索</span>
        </div>
      </div>

      <!-- 职位Tab -->
      <div class="job-tabs">
        <span class="tab active">推荐职位</span>
        <span class="tab">Java</span>
        <span class="tab">产品经理</span>
        <span class="tab">软件架构师</span>
        <span class="tab">前端工程师</span>
      </div>

      <!-- 金刚区 -->
      <div class="category-icons">
        <div class="category-item">
          <div class="icon blue">💻</div>
          <span>互联网AI</span>
        </div>
        <div class="category-item">
          <div class="icon orange">📚</div>
          <span>教育培训</span>
        </div>
        <div class="category-item">
          <div class="icon green">👔</div>
          <span>人事行政</span>
        </div>
        <div class="category-item">
          <div class="icon purple">🏠</div>
          <span>生活服务</span>
        </div>
        <div class="category-item">
          <div class="icon red">🎬</div>
          <span>影视传媒</span>
        </div>
        <div class="category-item">
          <div class="icon teal">💼</div>
          <span>白领专区</span>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-tabs">
          <span class="filter-tab active">综合</span>
          <span class="filter-tab">最新</span>
          <span class="filter-tab">附近</span>
        </div>
        <div class="filter-dropdowns">
          <span class="dropdown">职位 <i>▼</i></span>
          <span class="dropdown">城市 <i>▼</i></span>
          <span class="dropdown">筛选 <i>▼</i></span>
        </div>
      </div>
    </div>

    <!-- 提示横幅 -->
    <div class="notice-banner">
      <span class="notice-icon">📢</span>
      <span class="notice-text">职位信息审核中，审核通过后可联系本人</span>
      <span class="close-btn">×</span>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <!-- 通知卡片 -->
      <div class="notification-card">
        <div class="notification-icon">🔔</div>
        <div class="notification-content">
          <div class="title">人才消息通知</div>
          <div class="desc">
            开启通知，简历投递、职位访问、求职者消息及时提醒。
          </div>
        </div>
        <button class="enable-btn">去开启</button>
        <span class="card-close">×</span>
      </div>

      <!-- 简历完善引导 -->
      <div class="resume-guide-card">
        <div class="guide-content">
          <div class="guide-title">
            你首次参加工作的时间是? <span class="arrow">›</span>
          </div>
          <div class="guide-desc">老板想要了解你的工作经验时间</div>
        </div>
      </div>

      <!-- 招聘卡片列表 -->
      <div class="job-card" v-for="(job, index) in jobList" :key="index">
        <div class="job-header">
          <div class="job-title-row">
            <span class="company-tag" v-if="job.companyTag">{{
              job.companyTag
            }}</span>
            <span class="job-title">{{ job.title }}</span>
          </div>
          <div class="job-title-row second">
            <span class="job-subtitle">{{ job.subtitle }}</span>
            <span class="tag urgent" v-if="job.urgent">急聘</span>
            <span class="tag intern" v-if="job.intern">实习</span>
            <span class="tag remote" v-if="job.remote">外地</span>
          </div>
          <div class="salary">
            {{ job.salary }}<span v-if="job.bonus">·{{ job.bonus }}</span>
          </div>
        </div>
        <div class="company-info">
          <span class="company-name">{{ job.company }}</span>
          <span class="company-size">{{ job.size }}</span>
          <span class="company-funding">{{ job.funding }}</span>
        </div>
        <div class="job-tags">
          <span class="benefit-tag" v-for="tag in job.benefits" :key="tag">{{
            tag
          }}</span>
        </div>
        <div class="recruiter-info">
          <div class="avatar">{{ job.recruiter.avatar }}</div>
          <div class="recruiter-detail">
            <div class="recruiter-row">
              <span class="name">{{ job.recruiter.name }}</span>
              <span class="active-time">{{ job.recruiter.activeTime }}</span>
            </div>
            <div class="location-row">
              <span>{{ job.recruiter.distance }}</span>
              <span>{{ job.recruiter.district }}</span>
              <span>{{ job.recruiter.address }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 为你推荐分隔 -->
      <div class="recommend-divider">
        <span class="line"></span>
        <span class="star">✨</span>
        <span class="text">为你推荐</span>
        <span class="line"></span>
      </div>

      <!-- 满意度反馈 -->
      <div class="feedback-bar">
        <span class="feedback-icon">✏️</span>
        <span class="feedback-text">请问对本次的推荐结果还满意吗？</span>
        <span class="feedback-btn">我要反馈</span>
      </div>

      <!-- 广告卡片 -->
      <div class="ad-card">
        <div class="ad-image">
          <span class="ad-label">广告</span>
        </div>
        <div class="ad-content">
          <div class="ad-title">薄如蝉翼的金华火腿，每一口都是时间的味道</div>
          <div class="ad-footer">
            <span class="ad-close">×</span>
            <button class="ad-btn">立即体验</button>
          </div>
        </div>
        <div class="ad-source">
          脉脉 北京淘友天下科技发展优先公司 | 版本:6.6.2
        </div>
      </div>

      <!-- 去广告引导 -->
      <div class="remove-ad-card">
        <span>不想看广告？花小钱去广告</span>
        <button class="buy-btn">立即购买 ›</button>
      </div>

      <!-- 品牌占位 -->
      <div class="brand-placeholder">
        <span class="brand-name">鱼泡直聘</span>
      </div>

      <!-- 加载提示 -->
      <div class="loading-tip">
        <span class="loading-icon">⏳</span>
        <span>加载中...</span>
      </div>

      <!-- 底部提示 -->
      <div class="end-tip">- 没有更多内容了 -</div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item active">
        <div class="nav-icon">🔍</div>
        <span>找工作</span>
      </div>
      <div class="nav-item">
        <div class="nav-icon">
          💬
          <span class="badge">9</span>
        </div>
        <span>消息</span>
      </div>
      <div class="nav-item">
        <div class="nav-icon">👤</div>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

interface Recruiter {
  avatar: string;
  name: string;
  activeTime: string;
  distance: string;
  district: string;
  address: string;
}

interface Job {
  title: string;
  subtitle: string;
  companyTag?: string;
  urgent?: boolean;
  intern?: boolean;
  remote?: boolean;
  salary: string;
  bonus?: string;
  company: string;
  size: string;
  funding: string;
  benefits: string[];
  recruiter: Recruiter;
}

const jobList = ref<Job[]>([
  {
    title: "长期招收男",
    subtitle: "工数长期招收普身体健康能吃苦",
    companyTag: "安能",
    urgent: true,
    intern: true,
    remote: true,
    salary: "6000-9000元/月",
    bonus: "13薪",
    company: "成都心也科技有限公司",
    size: "1000-9999人",
    funding: "不需要融资",
    benefits: ["服务员", "兼职", "包吃住", "五险一金", "有提成", "带薪年假"],
    recruiter: {
      avatar: "👩",
      name: "成吉思汗",
      activeTime: "32分钟前回复",
      distance: "999km",
      district: "武侯区",
      address: "四川大学江安校区",
    },
  },
  {
    title: "长期招收男工数名",
    subtitle: "工数长期招收普身体健康能吃苦",
    companyTag: "猎头",
    urgent: true,
    intern: true,
    remote: true,
    salary: "6000-9000元/月",
    bonus: "13薪",
    company: "成都心也科技有限公司",
    size: "1000-9999人",
    funding: "不需要融资",
    benefits: ["服务员", "兼职", "包吃住", "五险一金", "有提成", "带薪年假"],
    recruiter: {
      avatar: "👨",
      name: "成吉思汗",
      activeTime: "32分钟前回复",
      distance: "999km",
      district: "武侯区",
      address: "四川大学江安校区",
    },
  },
  {
    title: "长期招收",
    subtitle: "工数长期招收普身体健康能吃苦",
    companyTag: "淘宝闪购合作商",
    urgent: true,
    intern: true,
    remote: true,
    salary: "6000-9000元/月",
    bonus: "13薪",
    company: "成都心也科技有限公司",
    size: "1000-9999人",
    funding: "不需要融资",
    benefits: ["服务员", "兼职", "包吃住", "五险一金", "有提成", "带薪年假"],
    recruiter: {
      avatar: "�",
      name: "成吉思汗",
      activeTime: "32分钟前回复",
      distance: "999km",
      district: "武侯区",
      address: "四川大学江安校区",
    },
  },
]);
</script>

<style scoped>
.recruit-page {
  width: 375px;
  min-height: 100vh;
  background: #f5f6fa;
  margin: 0 auto;
  position: relative;
  font-family:
    "PingFang SC",
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
  padding-bottom: 56px;
}
.header-section {
  background: linear-gradient(180deg, #e2f3ff 0%, #edf6ff 50%, #ffffff 100%);
}
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  height: 44px;
  box-sizing: border-box;
}
.status-bar .time {
  font-weight: 600;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
}
.status-icons {
  display: flex;
  gap: 4px;
  font-size: 12px;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  height: 48px;
}
.work-record-icon {
  width: 44px;
  height: 36px;
  background: linear-gradient(180deg, #249ffc 0%, #0aadfe 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
}
.search-input {
  flex: 1;
  height: 36px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid #fff;
  border-radius: 99px;
  display: flex;
  align-items: center;
  padding: 0 16px 0 12px;
  gap: 8px;
}
.search-input .placeholder {
  flex: 1;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}
.search-input .search-btn {
  color: #0092ff;
  font-size: 14px;
}
.job-tabs {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 12px;
  height: 44px;
  overflow-x: auto;
}
.job-tabs::-webkit-scrollbar {
  display: none;
}
.job-tabs .tab {
  font-size: 17px;
  color: rgba(0, 0, 0, 0.45);
  white-space: nowrap;
}
.job-tabs .tab.active {
  font-size: 23px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}
.category-icons {
  display: flex;
  gap: 20px;
  padding: 12px;
  overflow-x: auto;
}
.category-icons::-webkit-scrollbar {
  display: none;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.category-item .icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.category-item .icon.blue {
  background: linear-gradient(180deg, #6fd1ff 0%, #0b9dfe 100%);
}
.category-item .icon.orange {
  background: linear-gradient(180deg, #ffb86a 0%, #ff6239 100%);
}
.category-item .icon.green {
  background: linear-gradient(180deg, #4fe7cd 0%, #14c1bd 100%);
}
.category-item .icon.purple {
  background: linear-gradient(180deg, #c4a0ff 0%, #9b6dff 100%);
}
.category-item .icon.red {
  background: linear-gradient(180deg, #ff9b9b 0%, #ff6b6b 100%);
}
.category-item .icon.teal {
  background: linear-gradient(180deg, #4fe7cd 0%, #14c1bd 100%);
}
.category-item span {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
}
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  background: #fff;
}
.filter-tabs {
  display: flex;
}
.filter-tab {
  padding: 0 12px;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}
.filter-tab.active {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}
.filter-dropdowns {
  display: flex;
}
.dropdown {
  padding: 0 10px;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
}
.dropdown i {
  font-size: 10px;
  font-style: normal;
}
.notice-banner {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #e0f3ff;
}
.notice-icon {
  font-size: 14px;
}
.notice-text {
  flex: 1;
  font-size: 13px;
  color: #0092ff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.close-btn {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.45);
  cursor: pointer;
}
.content-section {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.notification-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 20px 8px 12px;
  background: #fff;
  border-radius: 12px;
  position: relative;
}
.notification-icon {
  width: 32px;
  height: 32px;
  background: #dff2ec;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.notification-content {
  flex: 1;
}
.notification-content .title {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.85);
}
.notification-content .desc {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.enable-btn {
  padding: 4px 8px;
  background: #0092ff;
  color: #fff;
  border: none;
  border-radius: 13px;
  font-size: 12px;
  font-weight: 500;
}
.card-close {
  position: absolute;
  top: 2px;
  right: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.25);
}
.resume-guide-card {
  background: linear-gradient(180deg, #e0f3ff 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 18px 12px;
}
.guide-title {
  font-size: 17px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}
.guide-title .arrow {
  color: #0092ff;
  margin-left: 8px;
}
.guide-desc {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
  margin-top: 4px;
}
.job-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.job-header {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.job-title-row {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}
.company-tag {
  background: #ebf7ff;
  border: 0.5px solid #0092ff;
  border-radius: 4px;
  padding: 1px 4px;
  font-size: 11px;
  color: #0092ff;
}
.job-title {
  font-size: 17px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}
.job-subtitle {
  font-size: 17px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tag {
  padding: 0 4px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  height: 18px;
  line-height: 18px;
}
.tag.urgent {
  background: #ffebec;
  border: 0.5px solid #e8362e;
  color: #e8362e;
}
.tag.intern {
  background: #e0f3ff;
  border: 0.5px solid #0092ff;
  color: #0092ff;
}
.tag.remote {
  background: #f5f6fa;
  border: 0.5px solid rgba(0, 0, 0, 0.45);
  color: rgba(0, 0, 0, 0.45);
}
.salary {
  font-size: 15px;
  font-weight: 500;
  color: #0092ff;
}
.company-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.65);
}
.company-name {
  max-width: 154px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.benefit-tag {
  background: #f5f7fc;
  border-radius: 4px;
  padding: 0 6px;
  height: 22px;
  line-height: 22px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
}
.recruiter-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.recruiter-detail {
  flex: 1;
}
.recruiter-row {
  display: flex;
  gap: 8px;
}
.recruiter-row .name {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.85);
  max-width: 59px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recruiter-row .active-time {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.location-row {
  display: flex;
  gap: 4px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.recommend-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 12px;
  height: 32px;
}
.recommend-divider .line {
  width: 33px;
  height: 1px;
  background: linear-gradient(90deg, #0092ff 0%, #f5f6fa 100%);
}
.recommend-divider .line:last-child {
  background: linear-gradient(270deg, #0092ff 0%, #f5f6fa 100%);
}
.recommend-divider .star {
  font-size: 16px;
}
.recommend-divider .text {
  font-size: 13px;
  color: #0092ff;
}
.feedback-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
}
.feedback-icon {
  font-size: 16px;
}
.feedback-text {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}
.feedback-btn {
  font-size: 14px;
  color: #0092ff;
}
.ad-card {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ad-image {
  width: 132px;
  height: 88px;
  background: #f0f0f0;
  border-radius: 4px;
  position: relative;
}
.ad-label {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: rgba(255, 255, 255, 0.65);
  font-size: 8px;
  padding: 2px 4px;
  border-radius: 2px;
}
.ad-content {
  flex: 1;
}
.ad-title {
  font-size: 15px;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.4;
}
.ad-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}
.ad-close {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.25);
}
.ad-btn {
  width: 60px;
  height: 24px;
  border: 1px solid #0092ff;
  border-radius: 32px;
  background: transparent;
  color: #0092ff;
  font-size: 11px;
}
.ad-source {
  font-size: 10px;
  color: rgba(0, 0, 0, 0.25);
}
.remove-ad-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
}
.remove-ad-card span {
  font-size: 15px;
  color: rgba(0, 0, 0, 0.85);
}
.buy-btn {
  background: transparent;
  border: none;
  color: #0092ff;
  font-size: 13px;
}
.brand-placeholder {
  background: #fff;
  border-radius: 12px;
  height: 97px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 30px;
  font-weight: 400;
  color: #f5f6fa;
  letter-spacing: 2px;
}
.loading-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 12px;
}
.loading-icon {
  font-size: 16px;
}
.loading-tip span {
  font-size: 15px;
  color: rgba(0, 0, 0, 0.45);
}
.end-tip {
  text-align: center;
  padding: 10px 0;
  font-size: 13px;
  color: rgba(0, 0, 0, 0.25);
}
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 375px;
  height: 56px;
  background: #fff;
  box-shadow: 0 -2px 4px rgba(50, 52, 60, 0.05);
  display: flex;
  justify-content: space-around;
  align-items: center;
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.nav-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  position: relative;
}
.badge {
  position: absolute;
  top: -5px;
  right: -7px;
  min-width: 14px;
  height: 14px;
  background: #e8362e;
  border: 0.5px solid #fff;
  border-radius: 7px;
  font-size: 10px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
}
.nav-item span {
  font-size: 10px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.65);
}
.nav-item.active span {
  color: #0092ff;
}
</style>
