# 项目部署报告（中小型商超进销存管理系统）

> 部署日期：2026-10-14 ｜ 执行方式：多子代理分模块并行部署
> 环境：Windows 10 / JDK 8 (Temurin 1.8.0_422) / Maven 3.9.16 / Node 22 / MySQL 8.0.42 / Redis 5.0.14

---

## 一、部署结果总览

| 模块 | 状态 | 验证证据 |
|---|---|---|
| **数据库** | ✅ 完成 | `ry_jxc` 库 **55 张表**（jxc_ 24 + sys_ 18 + QRTZ_ 11 + gen_ 2）；sys_user 2 条；jxc 字典 14 类型/52 数据；演示商品 5 条 |
| **后端** | ✅ 完成 | RuoYi-Vue **v3.9.1（JDK8 / Spring Boot 2.5.15）**；`GET /captchaImage` → 200（返回 uuid + base64 验证码图）；启动日志无 ERROR |
| **前端** | ✅ 完成 | **RuoYi-Vue3（Vue 3.5.26 + Vite 6.4.1 + Element Plus 2.13.1）**；`http://localhost/` → 200，含 `<div id="app">`；代理 `/dev-api` → 8080 验证 200 |
| **Redis** | ✅ 完成 | 5.0.14 Windows 版，127.0.0.1:6379，PING=PONG |
| **全链路** | ✅ 完成 | 前端 → 代理 → 后端 → MySQL/Redis 全通（三处 curl 200） |

## 二、访问信息

| 项 | 值 |
|---|---|
| 前端登录页 | **http://localhost/** （端口 80） |
| 后端 API | http://localhost:8080 |
| 登录账号 | `admin` / `admin123`（RuoYi 默认，含验证码） |
| 数据库 | MySQL `ry_jxc`（root/123456） |

## 三、关键决策与问题处置

### 3.1 JDK8 版本约束（重要）
克隆的 RuoYi-Vue master（3.9.2）已升级为 **Spring Boot 4.x / 要求 JDK17**（jar 字节码 class 61.0），与"JDK8"约束冲突。处置：**回退到 v3.9.1 tag**（`java.version=1.8`、Spring Boot 2.5.15），用 JDK8 重新构建（BUILD SUCCESS，3m20s）并启动验证通过。备份：旧 jar 存于 `/tmp/ruoyi-admin-3.9.2-jdk17.jar.bak`。

### 3.2 前端技术栈调整（用户决策）
前端由 Vue2/Element UI 调整为 **Vue3（官方 RuoYi-Vue3 + Vite + Element Plus）**。Gitee 源克隆需认证，改用 GitHub 镜像成功。npm 安装遇到三个问题均已解决：① npm 缓存 EPERM → 改用工作区缓存；② esbuild postinstall EBUSY → 显式补装 `@esbuild/win32-x64`；③ 相关版本均落在 node_modules（382 包）。

### 3.3 数据库兼容
v3.9.1 的系统表脚本（`ry_20250522.sql`）重新导入 `ry_jxc`（覆盖 sys_* 与 QRTZ_*，**jxc_ 业务表与字典不受影响**，字典已重新导入校验 14/52）。

### 3.4 环境陷阱记录
- 本机 shell 存在宿主注入的 `SERVER__PORT=9514`，会经 Spring 宽松绑定覆盖后端端口 → 启动命令必须带 `--server.port=8080`（或先 unset）。
- Redis 为免安装方式运行，**机器重启后需手动启动**：`D:\WorkBuddyPJ\2026-10-14-10-40-45\redis\redis-server.exe --port 6379`。

## 四、启动命令速查

```bash
# 1. Redis（先启动）
cd D:\WorkBuddyPJ\2026-10-14-10-40-45\redis && redis-server.exe --port 6379

# 2. 后端（JDK8）
cd D:\WorkBuddyPJ\2026-10-14-10-40-45\RuoYi-Vue
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=8080

# 3. 前端（Vue3）
cd D:\WorkBuddyPJ\2026-10-14-10-40-45\RuoYi-Vue3
npm install --registry=https://registry.npmmirror.com
npm run dev        # http://localhost
```

## 五、遗留事项

1. 登录页验证码已启用（sys_config `sys.account.captchaEnabled=true`），如需答辩演示免验证码可临时置 false。
2. 后端 Redis 依赖为免安装模式，建议后续封装启动脚本（redis + 后端 + 前端一键启动）。
3. 下一步：RuoYi 代码生成器导入 `jxc_` 表生成业务模块 CRUD（建议从 jxc_product 起步）。
4. 部署产物与文档已同步至 GitHub 仓库 `lhw5207/My_GP`（backend/ frontend/ docs/ sql/）。
