# 物流管理系统

一个基于 Vue 3 + TypeScript + Element Plus 的现代化物流管理系统。

## 技术栈

- **Vue 3.5** - 渐进式 JavaScript 框架
- **TypeScript 5.9** - JavaScript 的超集
- **Element Plus 2.13** - Vue 3 组件库
- **Vue Router 4.6** - 官方路由管理器
- **Pinia 3.0** - Vue 状态管理
- **Vite 7.2** - 下一代前端构建工具

## 功能模块

### ✅ 已完成功能

- **用户认证**
  - 登录/登出
  - 路由守卫
  - 权限控制

- **首页 Dashboard**
  - 数据统计卡片
  - 最近订单列表
  - 快捷操作
  - 系统通知

- **订单管理**
  - 订单列表（搜索、分页）
  - 创建订单
  - 订单详情
  - 订单编辑/删除

- **仓库管理**
  - 仓库列表
  - 库存管理
  - 入库/出库
  - 库存盘点

- **运输管理**
  - 运输任务列表
  - 运输跟踪
  - 创建运输任务

- **系统设置**
  - 系统配置
  - 用户管理

## 快速开始

### 安装依赖

```bash
pnpm install
```

### 启动开发服务器

```bash
pnpm dev
```

访问 http://localhost:5174/

### 默认账号

- 用户名：`admin`
- 密码：`123456`

### 构建生产版本

```bash
pnpm build
```

### 预览生产构建

```bash
pnpm preview
```

## 项目结构

```
src/
├── api/              # API 接口
├── assets/           # 静态资源
├── components/       # 公共组件
│   ├── PageContainer.vue      # 页面容器
│   ├── SearchForm.vue         # 搜索表单
│   ├── SidebarItem.vue        # 侧边栏菜单项
│   └── TablePagination.vue    # 表格分页
├── composables/      # 组合式函数
├── constants/        # 常量定义
├── layouts/          # 布局组件
│   └── MainLayout.vue         # 主布局
├── router/           # 路由配置
│   └── index.ts               # 路由定义
├── store/            # 状态管理
│   └── user.ts                # 用户状态
├── styles/           # 全局样式
├── types/            # TypeScript 类型
│   ├── router.ts              # 路由类型
│   └── user.ts                # 用户类型
├── utils/            # 工具函数
├── views/            # 页面视图
│   ├── Dashboard.vue          # 首页
│   ├── Login.vue              # 登录页
│   ├── order/                 # 订单模块
│   ├── warehouse/             # 仓库模块
│   ├── transport/             # 运输模块
│   └── settings/              # 设置模块
├── App.vue           # 根组件
└── main.ts           # 入口文件
```

## 核心特性

### 1. 自动菜单生成

只需在 `router/index.ts` 中配置路由，左侧菜单会自动生成：

```typescript
{
  path: '/order',
  component: () => import('../layouts/MainLayout.vue'),
  meta: {
    title: '订单管理',
    icon: 'Document'
  },
  children: [...]
}
```

### 2. 可复用组件

- **PageContainer** - 统一的页面容器
- **SearchForm** - 搜索表单组件
- **TablePagination** - 分页组件
- **SidebarItem** - 递归菜单组件

### 3. 路由守卫

自动处理登录验证：

- 未登录访问受保护页面 → 跳转到登录页
- 已登录访问登录页 → 跳转到首页

### 4. 响应式布局

- 侧边栏折叠/展开
- 自动面包屑导航
- 页面切换动画
- Keep-alive 缓存

## 开发指南

### 添加新模块

1. 在 `router/index.ts` 中添加路由配置
2. 创建对应的页面组件
3. 左侧菜单会自动显示

详细说明请查看 [FRAMEWORK.md](./FRAMEWORK.md)

### 页面开发模板

```vue
<template>
  <page-container title="页面标题" description="页面描述">
    <template #extra>
      <el-button type="primary">操作</el-button>
    </template>

    <!-- 页面内容 -->
  </page-container>
</template>

<script setup lang="ts">
import PageContainer from "@/components/PageContainer.vue";
</script>
```

## 文档

- [框架使用说明](./FRAMEWORK.md)
- [页面功能说明](./PAGES.md)
- [测试指南](./TEST_GUIDE.md)

## 开发规范

1. 组件命名使用 PascalCase
2. 文件命名使用 PascalCase
3. 变量命名使用 camelCase
4. 常量命名使用 UPPER_SNAKE_CASE
5. 类型定义使用 PascalCase

## 浏览器支持

- Chrome >= 90
- Firefox >= 88
- Safari >= 14
- Edge >= 90

## License

MIT

## 作者

物流管理系统开发团队
