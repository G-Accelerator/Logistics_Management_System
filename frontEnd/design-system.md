# 物流轨迹追踪系统 - 设计系统

## 设计理念

本设计系统专为物流轨迹追踪系统打造，体现以下核心理念：

- **专业可靠**：深蓝色调传递信任感和专业性
- **高效清晰**：简洁的界面设计，信息层次分明
- **动态活力**：渐变和动画元素体现物流的流动性
- **现代科技**：融入地图、轨迹等物流元素

---

## 色彩系统

### 主色调 - 深海蓝

```css
--primary-color: #0ea5e9; /* 主色 - 天空蓝 */
--primary-light: #38bdf8; /* 浅色 */
--primary-dark: #0284c7; /* 深色 */
--primary-darker: #0369a1; /* 更深 */
```

### 辅助色 - 科技青

```css
--accent-color: #06b6d4; /* 强调色 - 青色 */
--accent-light: #22d3ee;
--accent-dark: #0891b2;
```

### 语义色

```css
/* 成功 - 物流送达 */
--success-color: #10b981;
--success-light: #34d399;
--success-bg: rgba(16, 185, 129, 0.1);

/* 警告 - 物流延迟 */
--warning-color: #f59e0b;
--warning-light: #fbbf24;
--warning-bg: rgba(245, 158, 11, 0.1);

/* 危险 - 物流异常 */
--danger-color: #ef4444;
--danger-light: #f87171;
--danger-bg: rgba(239, 68, 68, 0.1);

/* 信息 - 物流在途 */
--info-color: #3b82f6;
--info-light: #60a5fa;
--info-bg: rgba(59, 130, 246, 0.1);
```

### 中性色

```css
--text-primary: #0f172a; /* 主要文字 */
--text-secondary: #475569; /* 次要文字 */
--text-tertiary: #94a3b8; /* 辅助文字 */
--text-disabled: #cbd5e1; /* 禁用文字 */

--bg-primary: #ffffff; /* 主背景 */
--bg-secondary: #f8fafc; /* 次背景 */
--bg-tertiary: #f1f5f9; /* 内容区背景 */

--border-color: #e2e8f0; /* 边框色 */
--border-light: #f1f5f9; /* 浅边框 */
```

### 渐变色

```css
/* 主渐变 - 用于按钮、标题等 */
--gradient-primary: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);

/* 背景渐变 - 用于登录页等大面积背景 */
--gradient-bg: linear-gradient(135deg, #0c4a6e 0%, #164e63 50%, #134e4a 100%);

/* 卡片渐变 - 用于特殊卡片 */
--gradient-card: linear-gradient(
  180deg,
  rgba(14, 165, 233, 0.05) 0%,
  rgba(6, 182, 212, 0.02) 100%
);
```

---

## 字体系统

### 字体族

```css
--font-family:
  "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
  "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
--font-mono: "SF Mono", "Fira Code", "Consolas", monospace;
```

### 字号规范

| 用途     | 字号 | 行高 | 字重 |
| -------- | ---- | ---- | ---- |
| 大标题   | 32px | 1.25 | 700  |
| 页面标题 | 24px | 1.33 | 600  |
| 卡片标题 | 18px | 1.44 | 600  |
| 正文     | 14px | 1.57 | 400  |
| 辅助文字 | 12px | 1.67 | 400  |
| 小标签   | 10px | 1.6  | 500  |

---

## 间距系统

基础单位：4px

```css
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 12px;
--spacing-lg: 16px;
--spacing-xl: 24px;
--spacing-2xl: 32px;
--spacing-3xl: 48px;
```

---

## 圆角系统

```css
--radius-sm: 4px; /* 小元素：标签、徽章 */
--radius-md: 8px; /* 中等元素：按钮、输入框 */
--radius-lg: 12px; /* 大元素：卡片、弹窗 */
--radius-xl: 16px; /* 特大元素：登录框 */
--radius-full: 9999px; /* 圆形：头像、圆形按钮 */
```

---

## 阴影系统

```css
--shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1);

/* 主色阴影 - 用于主按钮悬停 */
--shadow-primary: 0 8px 20px rgba(14, 165, 233, 0.35);
```

---

## 组件规范

### 按钮

#### 主按钮

```css
background: var(--gradient-primary);
color: #ffffff;
border: none;
border-radius: var(--radius-md);
padding: 10px 20px;
font-weight: 500;
transition: all 0.3s ease;

/* 悬停 */
transform: translateY(-2px);
box-shadow: var(--shadow-primary);
```

#### 次按钮

```css
background: transparent;
color: var(--primary-color);
border: 1px solid var(--primary-color);
border-radius: var(--radius-md);

/* 悬停 */
background: rgba(14, 165, 233, 0.1);
```

### 输入框

```css
border: 1px solid var(--border-color);
border-radius: var(--radius-md);
padding: 10px 12px;
transition: all 0.3s ease;

/* 聚焦 */
border-color: var(--primary-color);
box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
```

### 卡片

```css
background: var(--bg-primary);
border-radius: var(--radius-lg);
box-shadow: var(--shadow-md);
padding: var(--spacing-xl);
transition: all 0.3s ease;

/* 悬停 */
box-shadow: var(--shadow-lg);
transform: translateY(-2px);
```

---

## 物流状态图标与颜色

| 状态   | 颜色              | 图标 | 说明           |
| ------ | ----------------- | ---- | -------------- |
| 待发货 | `--text-tertiary` | 📦   | 灰色，等待处理 |
| 已发货 | `--info-color`    | 🚚   | 蓝色，运输中   |
| 运输中 | `--primary-color` | 🛣️   | 主色，在途     |
| 派送中 | `--warning-color` | 🏃   | 橙色，即将送达 |
| 已签收 | `--success-color` | ✅   | 绿色，完成     |
| 异常   | `--danger-color`  | ⚠️   | 红色，需处理   |

---

## 动画规范

### 过渡时间

```css
--transition-fast: 0.15s ease;
--transition-normal: 0.3s ease;
--transition-slow: 0.5s ease;
```

### 常用动画

#### 轨迹流动动画

```css
@keyframes trackFlow {
  0% {
    stroke-dashoffset: 100;
  }
  100% {
    stroke-dashoffset: 0;
  }
}
```

#### 脉冲动画（用于当前位置标记）

```css
@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.5);
    opacity: 0.5;
  }
}
```

#### 背景流动（登录页）

```css
@keyframes bgFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
```

---

## 登录页设计规范

### 布局

- 左侧：品牌展示区（60%），深色渐变背景 + 物流动画
- 右侧：登录表单区（40%），白色背景

### 品牌区元素

- Logo + 系统名称
- 物流轨迹动画（SVG 路径动画）
- 核心功能亮点（3-4个图标+文字）

### 表单区元素

- 欢迎语
- 角色切换标签
- 表单输入
- 登录按钮
- 辅助信息

---

## 响应式断点

```css
--breakpoint-sm: 640px; /* 手机 */
--breakpoint-md: 768px; /* 平板竖屏 */
--breakpoint-lg: 1024px; /* 平板横屏 */
--breakpoint-xl: 1280px; /* 桌面 */
--breakpoint-2xl: 1536px; /* 大屏 */
```

---

## 使用指南

1. 所有新页面开发应参考本设计系统
2. 颜色使用 CSS 变量，便于主题切换
3. 间距使用规范值，保持视觉一致性
4. 物流状态展示统一使用规定的颜色和图标
5. 动画效果适度使用，避免影响性能
