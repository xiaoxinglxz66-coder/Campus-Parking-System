<img width="2549" height="1473" alt="image" src="https://github.com/user-attachments/assets/65004f3f-ec48-4a76-a5cb-c23a406d8d24" /># 🎓 校园智慧停车管理系统 (Campus Smart Parking System)

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-Spring_Boot-green.svg)
![Vue/React](https://img.shields.io/badge/Frontend-Node.js-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-MySQL-orange.svg)

> 基于 Spring Boot + 前后端分离架构构建的校园智慧停车解决方案，集成了**腾讯云 OCR 车牌识别**与**支付宝沙箱支付**功能。

---

## 📖 项目简介

本项目旨在解决校园内车辆进出效率低、计费管理混乱等问题。系统通过调用腾讯云 OCR 接口实现车牌自动识别入库，并结合支付宝接口完成自动化计费与支付闭环。同时配备了完善的后台管理系统，支持细粒度的 RBAC 权限控制。

### ✨ 核心功能
* **🚗 智能出入库：** 模拟摄像头抓拍，调用 OCR 接口自动识别车牌并记录入场时间。
* **💰 自动化计费：** 根据停车时长自动计算费用，支持对接支付宝沙箱环境进行扫码模拟支付。
* **🔐 权限管理 (RBAC)：** 区分普通车主、安保人员和系统管理员，不同角色拥有不同操作权限。
* **📊 数据看板：** 实时查看当前车位剩余情况、历史停车记录及营收数据。

---

## 🛠️ 技术栈

### 后端 (Backend)
* **核心框架:** Spring Boot 
* **安全框架:** Spring Security + JWT
* **持久层:** MyBatis / Spring Data JPA
* **数据库:** MySQL 8.0
* **第三方接入:** 腾讯云 OCR API、支付宝开放平台 (沙箱环境)

### 前端 (Frontend)
* **基础框架:** (如果你用的是 Vue，就写 Vue 3 + Vite；如果是 React，就写 React)
* **UI 组件库:** (如 Element Plus / Ant Design)
* **网络请求:** Axios
![Uploading image.png…]()

---

## 🚀 快速开始 (本地运行指南)

想要在本地运行本项目，请确保你的电脑已安装以下环境：
* Java 8 或 17+
* Node.js (v14+ 推荐)
* MySQL (v5.7 或 v8.0)
* Maven

### 1. 克隆项目
```bash
git clone git@github.com:xiaoxinglxz66-coder/Campus-Parking-System.git

🤝 贡献与反馈
如果你在运行过程中遇到任何问题，欢迎提交 Issue。如果你觉得这个项目对你有帮助，请给我点一个 ⭐️ Star，非常感谢！
