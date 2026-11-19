# 项目提交与交付清单

## 1. Git 仓库与完整源码

仓库地址（根据当前本地 `origin` 配置）：

- HTTPS：<https://github.com/Yuk1no000/BookManagerSystem>
- SSH：`git@github.com:Yuk1no000/BookManagerSystem.git`


## 2. Docker、数据库和演示材料


| 文件 | 用途 |
| --- | --- |
| [compose.yaml](compose.yaml) | 数据库、后端、前端三个服务及持久化配置 |
| [Dockerfile](Dockerfile) | 后端多阶段构建 |
| [Dockerfile.runtime](Dockerfile.runtime) | 本机已打包 WAR 的备用运行镜像构建 |
| [frontend/Dockerfile](frontend/Dockerfile) | 前端构建及 Nginx 运行镜像 |
| [frontend/nginx.conf](frontend/nginx.conf) | 前端路由与 API 代理 |
| [.dockerignore](.dockerignore)、[frontend/.dockerignore](frontend/.dockerignore) | 镜像构建上下文排除规则 |
| [.env.example](.env.example) | 接收方创建本地配置的模板 |
| [db.sql](src/main/java/com/example/bookmanagesystem/db/db.sql) | 首次建库建表 |
| [demo-data.sql](src/main/java/com/example/bookmanagesystem/db/demo-data.sql) | 三条虚构演示图书，可重复导入，不覆盖已有记录 |
| [compose.demo.yaml](compose.demo.yaml) | 首次创建演示数据库时，追加挂载演示 SQL |

### 在新环境首次启动演示

以下为项目根目录的 Windows PowerShell 命令。先创建 `.env`，填写两个数据库密码与三个演示账号密码；已有配置不要覆盖：

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
notepad .env

docker compose -f compose.yaml -f compose.demo.yaml config --quiet
docker compose -f compose.yaml -f compose.demo.yaml pull db
docker compose -f compose.yaml -f compose.demo.yaml --progress plain build
docker compose -f compose.yaml -f compose.demo.yaml up -d --no-build --pull never
docker compose -f compose.yaml -f compose.demo.yaml ps
```

每一步成功后再执行下一步。镜像下载需要可用网络，Windows 代理和后端备用构建步骤见 [DOCKER.md](DOCKER.md)。演示入口为 `http://localhost:8081/login`；启动后无需运行本机 IDEA 后端或 Vite。

两个 Compose 文件合并后同时挂载 `01-schema.sql` 和 `02-demo-data.sql`。**只有空数据目录首次初始化时才自动执行它们**；已有数据库不会因为增加这个配置而自动导入演示数据，不要为此删除原数据卷。

### 向已有数据库导入演示图书

只导入独立的 `demo-data.sql`，不要重新执行整个 `db.sql`：

```powershell
docker compose cp src/main/java/com/example/bookmanagesystem/db/demo-data.sql db:/tmp/bookmanagesystem-demo-data.sql
docker compose exec db mysql --default-character-set=utf8mb4 -u book_app -p book_manage_system
```

按提示输入 `.env` 中的 `MYSQL_PASSWORD`，进入 MySQL 提示符后执行：

```sql
SOURCE /tmp/bookmanagesystem-demo-data.sql;
SELECT id, name, author, price FROM books;
exit
```

演示 SQL 不固定图书编号，前端查询、更新、删除时以列表实际显示的 ID 为准。它按书名和作者判断重复，不覆盖已有图书；用户修改了演示书名/作者后再次导入，原名称的演示条目可能重新出现。SQL 导入不会伪造 `operation_log`，业务操作日志应通过登录后的页面操作产生。

### 演示账号及获取方式

| 登录账号 | 角色 | 能演示的功能 | 密码获取方式 |
| --- | --- | --- | --- |
| `reader` | READER | 登录、列表和编号查询；验证无删除权限 | 接收方在 `.env` 设置 `DEMO_READER_PASSWORD` |
| `librarian` | LIBRARIAN | 查询、新增、更新；验证无删除权限 | 接收方在 `.env` 设置 `DEMO_LIBRARIAN_PASSWORD` |
| `admin` | ADMIN | 查询、新增、更新、删除 | 接收方在 `.env` 设置 `DEMO_ADMIN_PASSWORD` |

账号名和角色由 `AuthConfig` 内存配置创建，不需要到数据库注册。没有固定的公开登录密码；接收方按模板配置后即得到可用账号。真实开发 `.env` 不应随源码公开提交。如需向评审提供预设密码，可另行配置一套专用演示密码，通过指定渠道说明，不要使用现有开发或其他系统密码。

建议按“普通读者查询 → 图书管理员新增/更新 → 系统管理员删除测试图书 → 根据 X-Request-ID 查看日志”的顺序演示；删除对象选用演示过程中新建的图书。

## 3. 项目说明文档

| 文档 | 覆盖内容 |
| --- | --- |
| [README.md](README.md) | 功能、技术栈、目录、环境配置、本机运行、容器部署、使用方式、角色、接口、已知问题 |
| [DOCKER.md](DOCKER.md) | Windows 代理、多阶段构建、备用构建、端口、数据库初始化、启动停止和故障排查 |
| [LOGGING.md](LOGGING.md) | 运行/错误/业务日志、请求编号、保存位置、归档、检索及验收 |
| 本文件 | 提交范围、演示数据导入、账号获取与交付前检查 |

账号使用内存配置；图书查询为列表及 ID 查询；已有库不会自动执行结构迁移；`.env` 的 properties 与 dotenv 解析规则并非完全一致；当前端口绑定本机。具体配置和已知问题见 README 与 Docker 文档。

## 4. AI、脚手架和开源方案说明

详情请看[AI_USAGE.md](AI_USAGE.md)。其中说明 Codex 的辅助范围、Vue/Vite 模板、Spring Boot 及其他开源组件，以及针对特对原理语法问题的提问探讨。


