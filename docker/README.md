# Docker 一键部署 · 速查

> 手册：`docs/Docker容器化执行手册.md` ｜ 首次执行：2026-10-26（Docker 29.9.2 / Compose v5.6.1）
> 架构：mysql:8.0（宿主 **3308**）+ redis:5.0.14-alpine（不对外）+ backend eclipse-temurin:8-jre（**8080**）+ frontend nginx:stable-alpine（**8081**）

## 一、启动 / 停止 / 重建

```bash
cd D:\Graduation_project\docker

# 一键启动（首次含拉镜像+56表初始化，1-3 分钟）
docker compose up -d --build

# 看状态 / 看后端日志
docker compose ps
docker compose logs -f backend

# 停止（保留数据卷，数据库/上传文件不丢）
docker compose down

# 彻底重置（⚠️ 清空数据库，重新执行 init dump）
docker compose down -v && docker compose up -d --build
```

- 访问：前端 `http://localhost:8081`（admin/admin123）；后端直连 `http://localhost:8080`（cpolar 隧道指向这里）
- MySQL 调试：宿主连 `127.0.0.1:3308`，root/123456，库 `ry_jxc`

## 二、改了代码后重建（⚠️ 顺序）

**后端镜像不重新构建就用旧 jar。** 必须先在宿主机重新打包，再 build 镜像：

```bash
# ① 停宿主 8080（jar 文件锁），再打包
cd D:\Graduation_project\RuoYi-Vue && mvn package -DskipTests

# ② 重建后端镜像并滚动重启
cd D:\Graduation_project\docker && docker compose up -d --build backend
```

前端同理：`cd RuoYi-Vue3 && npm run build:prod` → `docker compose up -d --build frontend`。

## 三、与 Windows 开发环境的共存关系

| 端口 | 宿主开发环境 | Docker 栈 | 冲突？ |
|---|---|---|---|
| 3306 | 本机 MySQL80 | — | 无 |
| 3308 | （原计划 3307 被本机另一 mysqld 占用，已错峰） | mysql 容器映射 | 无 |
| 6379 | 免安装 Redis | redis 容器**不对外** | 无 |
| 80 | 前端 dev server | — | 无 |
| 8080 | 后端 jar | backend 容器映射 | **二选一** |
| 8081 | — | nginx 容器映射 | 无 |

⚠️ **宿主后端与容器 backend 同抢 8080**：跑 compose 前先停宿主 8080（`taskkill /PID <pid> /F`）；反之要用宿主三件套调试时先 `docker compose stop backend`。

⚠️ 两套环境的**数据库是两份独立数据**：宿主连本机 3306，容器连自己的 mysql-data 卷；演示/答辩用哪套，数据就录在哪套。

## 四、目录说明

```
docker/
├── docker-compose.yml        # 四容器编排（healthcheck/卷/自定网络）
├── backend/Dockerfile        # 只 COPY jar（上下文外置见 additional_contexts）
├── frontend/Dockerfile       # 只 COPY dist
├── frontend/nginx.conf       # /prod-api 反代 + SSE 关缓冲（proxy_buffering off）
├── mysql/init/01_ry_jxc_full.sql   # 全量种子（56表，mysqldump 产物，不入 Git）
└── .gitignore                # 忽略 mysql/init/*.sql
```

重新生成种子 dump（改库结构/补演示数据后）：

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -p123456 \
  --databases ry_jxc --single-transaction --routines --triggers \
  --default-character-set=utf8mb4 > docker/mysql/init/01_ry_jxc_full.sql
# 然后必须 down -v 重置数据卷才会重新导入
```

## 五、常见排障

| 症状 | 原因与处理 |
|---|---|
| backend 起不来，日志连 localhost | env 键名核对：`SPRING_DATASOURCE_DRUID_MASTER_URL` 等，见 compose |
| 前端 502 | nginx 反代目标 `backend` 与 compose 服务名不一致 / 后端未就绪（等 mysql healthy） |
| 验证码 500 | redis 容器未起：`docker compose ps` 看 jxc-redis |
| mysql 反复 restart | `docker compose logs mysql`；初始化失败就 `down -v` 重来（清库，属预期） |
| 收不到到账广播 | nginx.conf 的 `proxy_buffering off` 被删（SSE 被缓冲） |
| curl 报 502/超时 | 宿主代理注入，本地一律加 `--noproxy '*'` |
