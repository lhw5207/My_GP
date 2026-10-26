# Docker 容器化执行手册（新会话项目书）

> **目标**：用 `docker compose` 一键拉起 **MySQL + Redis + 后端 + 前端(Nginx)** 全栈，用于部署演示 / 答辩录屏 / 换机迁移；**不影响现有 Windows 开发环境**（三件套照旧可跑）。
> **使用方式**：新开对话 → 复制文末「第五节提示词」发给 Agent → Agent 按本手册 P1→P6 分阶段执行。
> 生成：2026-10-24 ｜ 状态：✅ **已执行**（2026-10-26，Docker 29.9.2 / Compose v5.6.1，P5 验证 8/8 通过；唯一偏差：宿主 MySQL 端口 3307 被本机另一 mysqld 实例占用，容器映射改用 **3308**）｜ 前置：用户已装 Docker Desktop（见 `docs/用户待办清单.md` §四）

---

## 一、目标架构（4 容器 + 2 卷）

| 容器 | 镜像 | 对外端口 | 职责 |
|---|---|---|---|
| `mysql` | `mysql:8.0`（utf8mb4 / TZ=Asia/Shanghai） | `3307:3306`（**3307** 避让本机 MySQL80 的 3306；也可不对外） | 库 `ry_jxc`（56 表），首次启动自动执行 init dump |
| `redis` | `redis:5.0.14-alpine`（不可用则 `5-alpine`） | 不对外 | 登录态/缓存；**无持久化**（与现状一致，重启清空无碍） |
| `backend` | `eclipse-temurin:8-jre`（+TZ） | `8080:8080` | `ruoyi-admin.jar`；**jar 不重打包，配置全用环境变量覆盖**；8080 对外供 cpolar 隧道 |
| `frontend` | `nginx:stable-alpine` | `8081:80`（**8081** 避让本机前端 dev 的 80） | 托管宿主机构建的 `dist/`；`/prod-api` 反代 `backend:8080` |

卷：`mysql-data`（数据库持久化）、`upload-path`（`/ruoyi/uploadPath` 上传目录，对应 `ruoyi.profile`）。

**与现状关系**：compose 全部用 3307/8081 等错峰端口，Windows 开发环境（3306/8080/80）完全不受影响，两套可并存。

## 二、关键设计决策（执行 Agent 必读，勿偏航）

1. **后端 jar 不重打包**：镜像内只用环境变量覆盖配置（Spring Boot 2.5 relaxed binding）：
   - `SPRING_DATASOURCE_DRUID_MASTER_URL=jdbc:mysql://mysql:3306/ry_jxc?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8`（⚠️ 以 `ruoyi-admin/.../application-druid.yml` 实际键名为准，属性路径 `spring.datasource.druid.master.*` → 环境变量 `SPRING_DATASOURCE_DRUID_MASTER_*`；用户名密码同法覆盖）
   - `SPRING_REDIS_HOST=redis`、`SPRING_REDIS_PORT=6379`
   - `RUOYI_PROFILE=/ruoyi/uploadPath`（对应自定义键 `ruoyi.profile`）
   - `TZ=Asia/Shanghai`
   - 若启动日志显示仍连 localhost → 说明键名对不上，核对 yml 后再调整 env 名，**禁止改 jar 内配置重打包**
2. **数据库种子用 mysqldump 全量，不用 `sql/` 目录拼装**：`sql/` 只有 jxc 脚本，缺 sys_/qrtz_/gen_ 基座表。执行：
   `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -p123456 --databases ry_jxc --single-transaction --routines --triggers --default-character-set=utf8mb4 > docker/mysql/init/01_ry_jxc_full.sql`
   （dump 含最新菜单 2082-2084/隐藏菜单/AI 与支付参数/演示数据；`--databases` 含 CREATE DATABASE/USE，配合 `MYSQL_DATABASE=ry_jxc` 均可正常初始化）
3. **前后端镜像不含构建过程**：宿主机 `mvn package -DskipTests`（先停 8080，jar 锁）+ `npm run build:prod` 产 dist → Dockerfile 只 COPY 产物。理由：容器内 maven/npm 构建慢且前端无 lock 文件会依赖漂移。
4. **Nginx 必须为 SSE 关缓冲**（到账广播依赖）：`/prod-api/` 反代配置加 `proxy_buffering off;` + `proxy_read_timeout 300s;` + `proxy_http_version 1.1;`。前端 dist 已用 `VITE_APP_BASE_API=/prod-api`（`.env.production`）。
5. **cpolar 支付宝回调不受影响**：仍指向宿主机 8080（backend 已映射），换网关/隧道后只需更新参数设置里的 `jxc.pay.notify.baseUrl`。
6. **所有新增文件集中放 `docker/` 目录**：`docker/docker-compose.yml`、`docker/backend/Dockerfile`、`docker/frontend/{Dockerfile,nginx.conf}`、`docker/mysql/init/01_ry_jxc_full.sql`（该 dump 不入 Git，`.gitignore` 或说明即可）。基座代码、`vite.config.js`、`application*.yml` **零改动**。

## 三、执行阶段（P1→P6，一轮会话内完成）

### P1 前置检查（5 分钟）
- [ ] `docker -v`、`docker compose version` 可用；Docker Desktop 运行中
- [ ] 端口占用核对：3307/8081 空闲（8080 宿主后端可继续跑，互不冲突）
- [ ] Maven/JDK8/Node 可用（沿用 `AGENTS.md` 环境）

### P2 产物准备（约 4 分钟）
- [ ] 停宿主 8080 → `mvn package -DskipTests` → 确认 `ruoyi-admin/target/ruoyi-admin.jar` 新鲜
- [ ] `npm run build:prod` → 确认 `dist/` 新鲜（约 20s）
- [ ] mysqldump 全量 → `docker/mysql/init/01_ry_jxc_full.sql`（见决策 2）

### P3 编排文件（核心产出）
- [ ] `docker/docker-compose.yml`：services 四容器 + healthcheck（mysql: `mysqladmin ping -h localhost`；backend 依赖 mysql healthy + redis started）+ 卷 + 自定义 bridge 网络
- [ ] `docker/backend/Dockerfile`：`FROM eclipse-temurin:8-jre` + TZ + `COPY` jar + `ENTRYPOINT ["java","-jar","/app.jar"]`（env 在 compose 里给）
- [ ] `docker/frontend/nginx.conf` + Dockerfile：静态托管 `/usr/share/nginx/html`；`location /prod-api/ { proxy_pass http://backend:8080/; ...SSE 配置见决策4 }`；`try_files ... /index.html`（前端 history 路由）
- [ ] ⚠️ `proxy_pass` 结尾斜杠规则：`/prod-api/xxx → backend:8080/xxx`（RuoYi 生产部署标准写法），与后端接口路径核对一遍

### P4 启动与排障
- [ ] `docker compose up -d --build`（首次拉镜像+init 56 表需 1-3 分钟）
- [ ] 观察顺序：mysql healthy → backend 起来 → frontend 起来；`docker compose logs -f backend` 看数据源/Redis 连接是否指向容器服务名
- 预设排障表：连不上 mysql（healthcheck 未过/密码）→ 检查 env 与 `MYSQL_ROOT_PASSWORD`；后端仍连 localhost → 决策 1 的 env 键名核对；前端 502 → nginx 反代目标名 `backend` 与 compose 服务名一致；验证码图片 500 → Redis 容器未起

### P5 验证清单（8 条，全过才算完成）
1. `docker compose ps` 四容器 running/healthy
2. `http://localhost:8081/` 标题 = 商超管理系统
3. `http://localhost:8081/prod-api/captchaImage` HTTP 200（经 Nginx 反代）
4. 登录 `admin/admin123` 得 token（curl 一律 `--noproxy '*'`）
5. `/prod-api/jxc/stock/list` total ≥ 4
6. **SSE 到账广播**：登录态生成微信模拟收款码 → 模拟到账 → 浏览器收银台页收到事件（验证 Nginx SSE 无缓冲配置；也可 `curl -N` 验）
7. AI 日报 job#101「执行一次」→ 铃铛出公告（模板模式即可）
8. 持久化：`docker compose down && up -d`（**不带 -v**）→ 库存/单据/公告仍在

### P6 收尾（协作约定，强制）
- [ ] `docker/README.md`：一键启动/停止/重建命令速查（含"先停宿主 8080 再 build 后端镜像"提醒）
- [ ] `AGENTS.md` §4 增加「Docker 一键启动」小节（4 行命令）；§2 目录地图补 `docker/`
- [ ] 更新 `docs/项目进度跟踪.md`（更新记录 + 运行状态表加 Docker 行）
- [ ] **`docs/会话变更记录.md` 末尾追加本轮 R 记录**（文件级清单）
- [ ] 同步 `repo/` → `git pull --rebase` → commit（消息不用英文双引号）→ push

## 四、风险与回退

| 风险 | 应对 |
|---|---|
| Docker Desktop/WSL2 未装好 | 先让用户完成 `用户待办清单.md` §四 |
| 镜像拉取慢/失败 | Docker Desktop 配 registry mirror（如阿里云加速器）或走本机代理 |
| mysql:8.0 init 慢/失败 | 看 `docker compose logs mysql`；确认 dump utf8mb4；失败则 `down -v` 重来（会清库重来，属预期） |
| eclipse-temurin:8-jre 拉不到 | 退路 `openjdk:8-jre` |
| 想彻底回退 | `docker compose down -v`；Windows 开发环境零改动，无残留影响 |

## 五、新会话提示词（复制即用）

```text
【项目】中小型商超进销存管理系统（毕设），工作区 D:\Graduation_project。
【先读】1) AGENTS.md  2) docs/Docker容器化执行手册.md（本次任务书，严格按它执行）  3) docs/会话变更记录.md（了解最近 R1-R4 改动）  4) docs/用户待办清单.md（项目状态快照）

【目标】按《Docker容器化执行手册》完成 docker compose 一键部署：mysql:8.0 + redis:5 + 后端(eclipse-temurin:8-jre) + 前端(nginx)，执行手册 P1→P6 全部阶段，P5 验证 8 条全过后收尾。

【硬约束】
- 不影响 Windows 开发环境：compose 端口用 3307/8081 错峰；基座代码/vite.config.js/application*.yml 零改动
- 后端 jar 不重打包：容器内全部用环境变量覆盖（SPRING_DATASOURCE_DRUID_MASTER_URL / SPRING_REDIS_HOST / RUOYI_PROFILE / TZ），键名以 application-druid.yml 实际结构为准
- 数据库种子：mysqldump 当前库全量（56 表）→ docker/mysql/init/，不用 sql/ 目录拼装（缺基座表）
- 宿主机构建产物后 Dockerfile 只 COPY（不在容器内 maven/npm build）
- nginx 反代 /prod-api 必须 proxy_buffering off（SSE 到账广播依赖）
- 环境坑：curl 一律 --noproxy '*'；mvn package 前先停宿主 8080（jar 锁）；后端启动 env -u SERVER__PORT
- 新增文件全部放 docker/ 目录；dump 文件不入 Git

【收尾（强制）】更新 docs/项目进度跟踪.md 更新记录 + docs/会话变更记录.md 末尾追加本轮记录（文件级清单）+ docker/README.md + AGENTS.md §4 补 Docker 启动 → 同步 repo/ → git pull --rebase → commit（消息内不用英文双引号）→ push origin main。

【本次任务】现在开始执行手册 P1（前置检查），逐阶段推进并在每阶段末汇报进度。
```

---

*执行完成后：本手册「状态」改为 ✅ 已执行（附日期与 compose 版本），并在会话变更记录登记*
