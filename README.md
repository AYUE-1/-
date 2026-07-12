# 🐾 宠物领养信息平台

基于 Vue 3 + Spring Boot 的宠物领养信息平台，为流浪动物与爱心人士搭建桥梁。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 (Composition API) + Vite |
| UI 组件库 | Element Plus |
| 状态管理 | Pinia |
| 后端框架 | Spring Boot 3.2 + Spring Security |
| ORM | MyBatis-Plus 3.5 |
| 数据库 | PostgreSQL |
| 认证 | JWT (无状态) |
| API 文档 | Knife4j |

## 核心功能

- 🔐 用户注册/登录 (JWT认证, 角色管理)
- 🐱 宠物信息管理 (CRUD, 分类筛选, 搜索, 分页)
- 📋 领养申请与审核 (状态机: PENDING → APPROVED/REJECTED → COMPLETED)
- ❤️ 收藏与评论 (收藏切换, 评论+回复)
- 👤 个人中心 (资料编辑, 我的收藏, 我的申请)
- 📊 管理后台 (仪表盘, 宠物/用户/审核管理)

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- Maven 3.8+
- PostgreSQL 15+

### 数据库初始化

```bash
# 创建数据库
psql -U postgres -c "CREATE DATABASE pet_adoption;"

# 执行建表脚本
psql -U postgres -d pet_adoption -f pet-adoption-backend/src/main/resources/init.sql
```

### 后端启动

```bash
cd pet-adoption-backend

# 修改 application-dev.yml 中的数据库连接信息

# 启动
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，API 文档: `http://localhost:8080/doc.html`

### 前端启动

```bash
cd pet-adoption-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端运行在 `http://localhost:5173`

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 项目结构

```
宠物领养平台/
├── pet-adoption-frontend/    # Vue 3 前端
│   ├── src/
│   │   ├── api/              # API 请求封装
│   │   ├── components/       # 公共组件
│   │   ├── composables/      # 组合式函数
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── utils/            # 工具函数
│   │   └── views/            # 页面视图
│   └── vite.config.js
│
├── pet-adoption-backend/     # Spring Boot 后端
│   ├── src/main/java/com/petadoption/
│   │   ├── config/           # 配置类
│   │   ├── controller/       # 控制器
│   │   ├── service/          # 服务层
│   │   ├── mapper/           # MyBatis Mapper
│   │   ├── entity/           # 实体类
│   │   ├── dto/              # 数据传输对象
│   │   ├── vo/               # 视图对象
│   │   └── enums/            # 枚举
│   └── pom.xml
│
└── README.md
```

## API 概览

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录/注册 |
| 宠物 | `/api/pets` | 宠物CRUD, 公开查询 |
| 领养 | `/api/adoptions` | 申请/审核 |
| 用户 | `/api/users` | 个人资料 |
| 收藏 | `/api/favorites` | 收藏管理 |
| 评论 | `/api/pets/{id}/comments` | 评论+回复 |
| 文件 | `/api/files` | 图片上传 |
| 管理 | `/api/admin` | 管理员接口 |
