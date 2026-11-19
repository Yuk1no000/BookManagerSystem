# 日志记录与排查

## 保存位置

- 本机运行后端：控制台和 `logs/book-manage.log` 同时输出。路径相对于程序工作目录，建议从项目根目录启动。
- Docker 运行后端：容器 `/app/logs` 绑定到宿主机 `logs/backend`，当前日志为 `logs/backend/book-manage.log`。容器重建不会删除这个绑定目录中的文件。
- 归档文件放在各自日志目录的 `archive` 下，按天及 20MB 分卷压缩。最多保留 14 个日周期，归档总量上限 500MB；达到容量上限可能提前清理旧归档，并不保证保留满 14 天。
- Docker 同时用 `json-file` 收集控制台输出，单文件 10MB，最多 3 个文件；它与应用日志文件分别占用存储空间。
- `operation_log` 仍然只保存事务成功提交的图书新增、更新、删除记录，运行日志和异常堆栈不写入 MySQL。

`logs/` 已被 Git 和后端 Docker 构建上下文忽略；不要把运行日志提交到仓库。

## 记录内容

- 每次同步 `/api/` 请求都会生成独立的 `requestId`，写入响应头 `X-Request-ID` 和 MDC。
- 请求摘要记录方法、路径（不含查询字符串）、状态码、耗时。日志 Filter 在 Spring Security 之前执行，因此登录、CSRF 拒绝、未登录访问及权限拒绝也有请求编号和摘要。
- 登录成功、退出登录记录 INFO；登录失败、未登录访问、权限拒绝及已知的 4xx 请求错误记录 WARN。
- 数据库异常和意外 MVC 异常在统一异常处理层记录 ERROR 与完整堆栈。安全过滤器等向外抛出的未处理异常由请求 Filter 记录。请求摘要仍是单独的一条 INFO，不重复打印堆栈。
- MVC 错误响应包含安全的 `message` 和 `requestId`，参数校验保留 `errors`；不向前端返回堆栈或数据库异常详情。401/403 等安全层响应沿用原行为，通过响应头取得请求编号。
- 不主动记录密码、Cookie、Session ID、CSRF 令牌、Authorization 请求头或完整请求体。异常堆栈可能包含底层系统信息，应限制日志目录的访问权限。

当前使用同步请求链上的 MDC；以后引入异步请求或线程池任务时，需要单独传递上下文和处理异步完成日志。Filter 的异常摘要对向外抛出的异常按 500 记录，最终网络错误或容器错误分派的结果仍应结合容器日志分析。

## 排查命令（PowerShell）

```powershell
# 本机实时查看；Ctrl+C 退出查看
Get-Content .\logs\book-manage.log -Tail 100 -Wait

# 用浏览器 Network 中响应头 X-Request-ID 的值搜索，并查看前后文
Select-String -Path .\logs\book-manage.log -SimpleMatch 'requestId=替换为实际编号' -Context 3,20

# Docker 最近半小时的控制台日志
docker compose logs --since 30m --tail 200 backend

# Docker 文件日志，保留完整异常堆栈
Get-Content .\logs\backend\book-manage.log -Tail 100 -Wait
```

归档为 `.gz`，需要先解压再搜索。时间较长的堆栈应继续查看完整文件，不要仅依赖搜索结果的 20 行上下文。

## 验收

执行 `mvn.cmd test`。日志相关测试使用模拟业务服务和测试专用账号，不要求 MySQL 已启动，不创建真实业务数据。

重点检查正常请求、401、登录失败、403、CSRF 拒绝、参数错误、数据库异常、意外异常、404/405 的状态语义，以及请求编号关联、MDC 清理和文件中的异常堆栈。测试日志位于 `target/logging-tests/`。
