# OnlineLearn 在线学习平台

![License](https://img.shields.io/badge/License-GPLv3-blue.svg)

一个面向在线学习场景的 Web 应用平台，支持**管理员、教师、学生**三种角色，提供课程管理、作业管理、视频学习、在线练习、问答互动等核心功能。

---

## 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| **前端** | Vue 2 + Element UI | 2.6.11 / 2.4.5 |
| **后端** | Spring Boot + MyBatis-Plus | 2.5.6 / 3.4.0 |
| **数据库** | MySQL | 5.7+ / 8.0 |
| **构建** | Maven / npm | 3.x / 6.x+ |

## 项目结构

```
OnlineLearn/
├── backend/              # 后端 Spring Boot 项目
│   ├── src/
│   ├── pom.xml
│   ├── online_learn.sql  # 数据库初始化脚本
│   └── file/             # 上传文件存储目录
├── frontend/             # 前端 Vue 2 项目
│   ├── src/
│   ├── vue.config.js
│   └── package.json
├── docs/                 # 项目文档
├── start.bat             # Windows 快速启动脚本
├── start.ps1             # PowerShell 启动脚本
└── LICENSE               # GPL-3.0 许可证
```

## 快速启动

### 环境要求

- JDK 1.8+
- Maven 3.x
- MySQL 5.7+
- Node.js 12.x+
- npm 6.x+

### 1. 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS online_learn DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p online_learn < backend/online_learn.sql
```

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`，修改数据库用户名和密码。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问: http://localhost:9251

### 4. 启动前端

```bash
cd frontend
npm install --legacy-peer-deps
npm run serve
```

前端启动后访问: http://localhost:9252

### 一键启动

也可直接运行 `start.bat`（Windows CMD）或 `start.ps1`（PowerShell）一键启动项目。

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 教师 | t1 | 123456 |
| 学生 | s1 | 123456 |

## 角色功能

| 功能模块 | 学生 | 教师 | 管理员 |
|----------|------|------|--------|
| 在线课程学习 | ✅ | — | — |
| 视频观看 | ✅ | ✅（上传管理） | ✅（管理） |
| 作业提交/批改 | ✅ | ✅ | ✅ |
| 在线练习 | ✅ | ✅（出题） | — |
| 问答交流 | ✅ | ✅ | — |
| 班级管理 | — | ✅ | ✅ |
| 用户管理 | — | — | ✅ |
| 成绩管理 | ✅(查看) | ✅ | — |
| 知识点管理 | — | — | ✅ |
| 科目管理 | — | — | ✅ |

## 许可证

本项目基于 **GNU General Public License v3.0** 开源协议发布。

详细条款请参见 [LICENSE](./LICENSE) 文件。