# Docker Compose 部署与操作

本项目用 Docker Compose 运行三个服务：MySQL 8.0 数据库、Spring Boot 后端、Nginx 承载的 Vue 前端。前端通过 Nginx 将 `/api/` 请求转发给后端；后端通过 Compose 网络中的服务名 `db` 连接数据库。

以下命令均在项目根目录的 PowerShell 中执行，例如 `D:\Project\BookManagerSystem`。需要 Docker Desktop 已启动并使用 Linux 容器，可用 `docker compose version` 检查 Compose 是否可用。

## 首次启动

1. 仅在 `.env` 不存在时复制配置模板，并把两个示例密码换成自己的密码。已有 `.env` 时不要覆盖它：

   ```powershell
   if (-not (Test-Path .env)) { Copy-Item .env.example .env }
   notepad .env
   ```

   `.env` 已被 Git 忽略，不要把密码写入 `.env.example`。建议使用足够长的随机字母数字密码；若包含 `$`、`#` 等特殊字符，需按 Compose 的 `.env` 引号规则书写，避免变量插值或注释截断。可以在 `.env` 中调整 `FRONTEND_PORT`、`BACKEND_PORT`、`MYSQL_PORT`；默认分别为 `8081`、`8080`、`3307`。

2. 检查配置。若本机还没有 `mysql:8.0` 镜像，先拉取数据库镜像：

   ```powershell
   docker compose config --quiet
   docker compose pull db
   ```

   已有可用的 `mysql:8.0` 镜像时，可跳过 `pull db`。`config --quiet` 只检查配置，不打印 `.env` 中的密码。

3. 构建前后端镜像，再用本地镜像启动全部服务：

   ```powershell
   docker compose --progress plain build
   docker compose up -d --no-build --pull never
   docker compose ps
   ```

   `build` 会下载 Maven、Java、Node、Nginx 基础镜像及项目依赖；首次构建可能较慢。`up` 使用已构建的镜像，不会再构建或拉取。数据库第一次使用空数据卷启动时，会执行 `src/main/java/com/example/bookmanagesystem/db/db.sql` 建表；后续启动保留数据，不会重新执行该 SQL。

## 访问与检查

| 服务 | 默认地址 | 用途 |
| --- | --- | --- |
| 前端 | <http://localhost:8081> | 浏览器入口；前端的 `/api/` 请求由 Nginx 转发 |
| 后端 API | <http://localhost:8080/api/books> | 直接查看图书接口 |
| Swagger UI | <http://localhost:8080/docs> | 浏览器中的接口文档 |
| MySQL | `127.0.0.1:3307` | 数据库 `book_manage_system`；用户 `book_app`；密码是 `.env` 中的 `MYSQL_PASSWORD` |

三个端口默认仅绑定本机 `127.0.0.1`，其他设备不能直接访问。MySQL 容器内使用 `3306`，后端容器内使用 `8080`；不要把本机端口 `3307` 当作容器间连接端口。

检查服务和数据库表：

```powershell
docker compose ps
docker compose logs --tail=80 db backend frontend
docker compose exec db mysql -u book_app -p book_manage_system -e "SHOW TABLES;"
```

最后一条命令会提示输入 `.env` 中的 `MYSQL_PASSWORD`。`db` 应显示 `healthy`；前端页面和 `/api/books` 应能正常访问。若后续启用认证，API 和 Swagger UI 的访问要求应以当时的后端安全配置为准。

## 停止、重启与更新

| 操作 | 命令 | 数据影响 |
| --- | --- | --- |
| 查看实时日志 | `docker compose logs -f --tail=100` | 按 `Ctrl+C` 只退出日志查看 |
| 暂停三个服务 | `docker compose stop` | 保留容器和数据库数据 |
| 启动已暂停的服务 | `docker compose start` | 使用现有容器 |
| 停止并移除容器 | `docker compose down` | 保留 MySQL 数据卷 |
| 从本地镜像再次启动 | `docker compose up -d --no-build --pull never` | 保留 MySQL 数据卷 |

修改前端或后端代码后，重新构建并更新容器：

```powershell
docker compose --progress plain build
docker compose up -d --no-build --pull never
```

修改 `.env` 中的端口后，重新执行 `up` 使端口映射生效。**已有数据卷时，单纯修改 `.env` 中的 `MYSQL_PASSWORD` 或 `MYSQL_ROOT_PASSWORD` 不会更改 MySQL 内已创建的用户密码**。密码轮换需先在 MySQL 内修改对应账户，再同步 `.env` 并更新容器。只有明确要清空数据库、已备份并确认备份可恢复时才运行 `docker compose down -v`；该命令会删除数据卷，下次启动将重新建库建表。

## 常见问题

- `unknown shorthand flag: 'd'`：命令缺少 `up`；应使用 `docker compose up -d ...`。
- 构建阶段访问 `auth.docker.io` 超时：这是镜像仓库认证请求的网络问题。先确认本机网络或代理可访问该地址。若使用本机 HTTP 代理，可在当前 PowerShell 窗口设置 `$env:HTTP_PROXY`、`$env:HTTPS_PROXY` 为实际代理地址，再重新运行 `docker compose --progress plain build`。不要为这个错误清空 Docker 数据卷。
- `npm ci` 报 `package.json` 和 `package-lock.json` 不同步：修改 `frontend/package.json` 后必须同步锁文件；镜像构建会严格执行 `npm ci`。
- `descriptor is neither a manifest or index`：本地镜像记录可能不完整。运行 `docker image inspect mysql:8.0 --format '{{.Os}}/{{.Architecture}} {{.Size}}'`；若系统或架构为空，或大小为 `0`，在网络可用时运行 `docker compose pull db` 重新获取该镜像，再重试 `up`。不要仅凭 `docker image ls` 中有标签就认为镜像可运行。
- 服务启动后访问失败：先看 `docker compose ps` 和 `docker compose logs --tail=80 db backend frontend`，确认数据库健康状态、后端日志及端口占用情况。

不使用 Compose 运行后端时，通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 环境变量提供数据库连接；默认 URL 指向本机 `localhost:3306/book_manage_system`，默认用户为 `root`。
