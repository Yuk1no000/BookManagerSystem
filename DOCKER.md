# Docker Compose 部署与操作

本项目用 Docker Compose 运行三个服务：MySQL 8.0 数据库、Spring Boot 后端、Nginx 承载的 Vue 前端。前端通过 Nginx 将 `/api/` 请求转发给后端；后端通过 Compose 网络中的服务名 `db` 连接数据库。

以下命令均在项目根目录的 PowerShell 中执行，例如 `D:\projects\BookManageSystem`。需要 Docker Desktop 已启动并使用 Linux 容器，可用 `docker compose version` 检查 Compose 是否可用。

## Windows 构建前的代理设置

如果构建在 `load metadata` 阶段出现 `failed to fetch anonymous token`，且 `auth.docker.io:443` 连接超时，这是基础镜像认证请求的网络问题，尚未执行项目代码构建。Docker Desktop 的系统代理和命令行进程的代理是不同的配置来源。

本机已经验证可用的 HTTP 代理地址为 `http://127.0.0.1:7897`。保持代理客户端运行，在执行 Docker 命令的同一个窗口中设置代理。其他机器需要替换成自己的实际地址；不要把这里的 `127.0.0.1` 当成通用公共代理。

PowerShell：

```powershell
$env:HTTP_PROXY = 'http://127.0.0.1:7897'
$env:HTTPS_PROXY = 'http://127.0.0.1:7897'
$env:NO_PROXY = 'localhost,127.0.0.1,::1'
```

如果当前窗口是 CMD（提示符类似 `D:\projects\BookManageSystem>`），使用：

```bat
set "HTTP_PROXY=http://127.0.0.1:7897"
set "HTTPS_PROXY=http://127.0.0.1:7897"
set "NO_PROXY=localhost,127.0.0.1,::1"
```

上述设置只影响当前窗口及其子进程，不修改系统 DNS 或全局环境变量。只把代理写入项目 `.env` 不会自动配置运行 Docker CLI 的进程。确认 Docker Desktop 自身的代理也能访问镜像仓库后，再执行下方构建步骤。

## 首次启动

1. 仅在 `.env` 不存在时复制配置模板，并填写数据库密码及三个演示登录账号密码。已有 `.env` 时不要覆盖它：

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

3. 构建前后端镜像，再用本地镜像启动全部服务。若数据库容器已经健康运行，继续执行即可，Compose 会复用它；不需要先执行 `down`：

   ```powershell
   docker compose --progress plain build
   docker compose up -d --no-build --pull never
   docker compose ps
   ```

   `build` 会下载 Maven、Java、Node、Nginx 基础镜像及项目依赖；首次构建可能较慢。`up` 使用已构建的镜像，不会再构建或拉取。数据库第一次使用空数据卷启动时，会执行 `src/main/java/com/example/bookmanagesystem/db/db.sql` 建表；后续启动保留数据，不会重新执行该 SQL。

## Maven 基础镜像下载停滞时的备用构建

如果代理已能访问认证服务，但体积较大的 Maven 基础镜像层长时间不再下载，本机已安装 JDK 17 和 Maven 时，可以先在本机完成测试、打包，再用 `Dockerfile.runtime` 制作 Java 运行镜像。应用运行时仍完全位于容器中；这条备用路径只改变打包所在的位置，不修改业务代码、数据库连接或数据卷。

在已设置代理的 PowerShell 窗口、项目根目录执行：

```powershell
mvn.cmd -B package
if ($LASTEXITCODE -ne 0) { throw '后端测试或打包失败，停止部署' }

New-Item -ItemType Directory -Force target/docker-runtime | Out-Null
$war = @(Get-ChildItem -LiteralPath target -Filter '*.war')
if ($war.Count -ne 1) { throw 'target 中应当恰好有一个待部署的 WAR 文件' }
Copy-Item -LiteralPath $war[0].FullName -Destination target/docker-runtime/app.war -Force

docker build --progress plain -f Dockerfile.runtime -t bookmanagesystem-backend:latest target/docker-runtime
if ($LASTEXITCODE -ne 0) { throw '后端运行镜像构建失败' }

# 前端镜像若已成功构建，可以跳过这一条
docker compose --progress plain build frontend
if ($LASTEXITCODE -ne 0) { throw '前端镜像构建失败' }

docker compose up -d --no-build --pull never
docker compose ps
```

上面的后端镜像名与当前默认 Compose 项目名 `bookmanagesystem` 对应。若通过 `-p` 或 `COMPOSE_PROJECT_NAME` 改过项目名，需要同步调整镜像标签。原 `Dockerfile` 的多阶段构建保留，网络恢复稳定后仍可使用通常的 `docker compose build`。

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

最后一条命令会提示输入 `.env` 中的 `MYSQL_PASSWORD`。`db` 应显示 `healthy`。先在前端登录页登录，再使用图书接口；未登录直接请求 `/api/books` 返回 401 是当前安全配置的预期行为。账号为 `reader`、`librarian`、`admin`，密码分别来自 `.env` 中对应的 `DEMO_*_PASSWORD`；不要把这些登录密码和 MySQL 密码混淆。

前后端成功启动后，日常使用只需保持 Docker Desktop 和这三个容器运行，不需要再启动 IDEA 后端或 Vite 开发服务器。应用文件日志保存在宿主机 `logs/backend/book-manage.log`，排查方法见 `LOGGING.md`。

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
- 构建阶段访问 `auth.docker.io` 超时：按上方章节给当前命令行窗口设置代理；不要把超时误判成密码错误，也不要通过清空数据卷、禁用 TLS 或固定未经核验的 IP 来处理。如果本次构建中部分镜像已经完成，修复网络后重试会复用已有缓存。
- `npm ci` 报 `package.json` 和 `package-lock.json` 不同步：修改 `frontend/package.json` 后必须同步锁文件；镜像构建会严格执行 `npm ci`。
- `descriptor is neither a manifest or index`：本地镜像记录可能不完整。运行 `docker image inspect mysql:8.0 --format '{{.Os}}/{{.Architecture}} {{.Size}}'`；若系统或架构为空，或大小为 `0`，在网络可用时运行 `docker compose pull db` 重新获取该镜像，再重试 `up`。不要仅凭 `docker image ls` 中有标签就认为镜像可运行。
- 服务启动后访问失败：先看 `docker compose ps` 和 `docker compose logs --tail=80 db backend frontend`，确认数据库健康状态、后端日志及端口占用情况。

不使用 Compose 运行后端时，当前程序会从工作目录读取 `.env`。数据库 URL 优先使用 `DB_URL`，否则连接 `127.0.0.1` 的 `book_manage_system` 数据库，端口取 `MYSQL_PORT`，未设置时为 `3307`；应用用户默认为 `book_app`，密码优先使用 `DB_PASSWORD`，否则使用 `MYSQL_PASSWORD`。
