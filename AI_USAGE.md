# AI、脚手架及开源方案使用说明

## AI 辅助

项目开发过程中使用 OpenAI Codex 作为 AI 编程助手，主要用于代码建议与调整、现有代码解释、故障定位、测试补充、Docker 部署验证和文档整理。本说明记录能够从本次协作与仓库确认的使用情况，不包含完整 AI 对话记录。

AI 建议结合本项目代码、配置和运行结果进行了适配；验证方式包括 Maven 测试、前端构建、Docker Compose 配置检查，以及部署后的登录、角色和数据库查询检查。具体功能以交付版本源码与测试为准，项目作者应在提交前核对最终材料。

## 脚手架和开源组件

| 方案 | 在项目中的用途 |
| --- | --- |
| Vue 3 + Vite | 前端从 Vue/Vite 模板起步，使用单文件组件和 Composition API |
| Vue Router | 登录页、工作台、图书操作页面路由及角色访问守卫 |
| Spring Boot / Spring MVC | 后端工程结构、HTTP 接口、应用配置与可执行 WAR |
| Spring Security | 密码编码、Session 身份认证、角色授权、CSRF 与退出登录 |
| Spring JDBC / MySQL Connector | 参数化 SQL、图书数据访问与事务 |
| Jakarta Bean Validation | 请求字段的非空、长度和价格规则 |
| SLF4J / Logback | 日志接口、MDC 上下文、文件输出及滚动归档 |
| springdoc-openapi | OpenAPI 模型和 Swagger UI |
| JUnit / Mockito / MockMvc | 模拟业务服务、接口权限、异常和日志验证 |
| Docker Compose / Nginx | 三服务部署、容器网络、前端静态资源和 API 代理 |

后端采用 Spring Boot 标准项目结构及 Maven Starter 组合。依赖声明与锁定信息见 [pom.xml](pom.xml)、[frontend/package.json](frontend/package.json) 和 [frontend/package-lock.json](frontend/package-lock.json)。

## AI问答后的主要调整

1. 将前端示例页面扩展为登录、工作台和图书增删改查页面，添加可复用表单、错误提示与接口封装。
2. 接入真实 MySQL 数据，处理后端请求字段 `name` 与响应字段 `bookName` 的差异；使用 `BigDecimal` 和数据库定点数保存价格。
3. 加入普通读者、图书管理员、系统管理员角色；前端控制路由及顶部导航，后端独立校验接口权限。演示账号在内存中创建，密码由配置提供。
4. 接入 CSRF 令牌领取、登录会话、当前用户查询和退出流程，复用 `/api` 路径以适配 Vite 与 Nginx 两种代理。
5. 加入参数校验和统一异常响应，保留正常 HTTP 状态语义，对意外异常记录堆栈并返回安全提示。
6. 新增请求编号和日志 Filter，将运行日志、错误日志与数据库业务操作记录关联；添加归档、容量限制和日志目录挂载。
7. 增加 Dockerfile、Compose、数据库初始化和独立演示数据；保留多阶段构建，并提供本机打包 WAR 后构建运行镜像的备用方式。
8. 补充自动化测试、环境模板及运行、部署、日志、交付文档。

