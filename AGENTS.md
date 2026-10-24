# AGENTS.md · 商超进销存管理系统（毕设）

> **给任何 Agent / 模型的最短上手路径。** 详细版见 `docs/多Agent协同说明.md`；项目全貌见 `docs/项目交接文档.md`。
> 位置：`D:\Graduation_project` ｜ 状态：**开发 100% 完成**（19 模块 / 8 条事务链 / 55 张表），剩余为论文、测试文档、答辩脚本

---

## 1. 一句话
基于 **RuoYi-Vue 3.9.1 基座** + 自研进销存业务的前后端分离系统，覆盖采购 → 库存 → 销售 → 往来 → 报表全链路，面向 1–3 家门店的中小型商超。

## 2. 目录地图

| 路径 | 是什么 | 能改吗 |
|---|---|---|
| `RuoYi-Vue/` | 后端（Maven 多模块，JDK8）。业务代码在 `com.ruoyi.jxc` | ✅ 业务包 / ⚠️ 基座 |
| `RuoYi-Vue3/` | 前端（Vue3 + Vite + Element Plus），业务页在 `src/views/jxc/**` | ✅ 业务页 / ⚠️ 基座 |
| `sql/` | 13 个脚本：建表 45KB + 演示数据 + 10 个菜单授权 | ✅ 追加，勿改已执行 |
| `docs/` | 交接文档、进度台账、功能取舍、迁移清单、多 Agent 说明 | ✅ |
| `redis/` | Windows 免安装 Redis 5.0.14.1（47MB） | ❌ 只运行 |
| `codegen/` | 代码生成器输出留档（zip 冗余） | ✅ 仅参考 |
| `backup/` | `ry_jxc_20261024.sql`（55 表全量 dump，237KB） | ❌ |
| `repo/` | Git 仓库副本（与 `E:\Git` 同源同 remote） | ⚠️ 提交前先 pull |

## 3. 技术栈（版本不要动）

| 层 | 版本 | 硬约束 |
|---|---|---|
| 后端 | RuoYi-Vue **3.9.1** + Spring Boot 2.5.15 + 原生 MyBatis，**JDK8**（`D:\Dev\Java\jdk8`） | **禁止升级到 3.9.2+（需 JDK17）** |
| 前端 | RuoYi-Vue3，Vue 3.5 + Vite 6 + Element Plus 2.13 | 页面统一 `<script setup>` + `#default` 插槽 |
| 数据库 | MySQL 8.0.42，库 **`ry_jxc`**（55 表 = jxc_ 24 + sys_ 18 + qrtz_ 11 + gen_ 2） | utf8mb4 |
| 缓存 | Redis **5.0.14.1**（免安装，6379） | 无 rdb/aof，重启清空 |

## 4. 启动（顺序敏感）

```bash
# ① Redis（必须先起，否则后端启动失败）
cd D:\Graduation_project\redis && .\redis-server.exe --port 6379

# ② 后端（必须清掉宿主注入的 SERVER__PORT，并显式 8080）
cd D:\Graduation_project\RuoYi-Vue
env -u SERVER__PORT "D:\Dev\Java\jdk8\bin\java" -jar ruoyi-admin\target\ruoyi-admin.jar --server.port=8080

# ③ 前端
cd D:\Graduation_project\RuoYi-Vue3 && npm run dev     # 端口 80，代理到 8080
```

**停止**：前端 Ctrl+C；后端按 8080 的 PID `taskkill /PID <pid> /F`；Redis 关窗口。

## 5. 构建

```bash
# 后端（构建前必须先停 8080，否则 jar 文件锁）
cd D:\Graduation_project\RuoYi-Vue && mvn package -DskipTests
# 前端（产出 dist/，约 23s）
cd D:\Graduation_project\RuoYi-Vue3 && npm run build:prod
```

⚠️ `application*.yml` 会被打进 jar：改了配置**必须重新 package**，或用 `--spring.config.location` 外置。

## 6. 验证（改完必跑）

```bash
curl -s -o /dev/null -w '%{http_code}\n' --noproxy '*' http://localhost:8080/captchaImage     # 200
curl -s --noproxy '*' -X POST http://localhost:8080/login -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'                                          # code:200 + token
curl -s --noproxy '*' -H "Authorization: Bearer <token>" \
     "http://localhost:8080/jxc/stock/list?pageNum=1&pageSize=5"                              # total:4
curl -s --noproxy '*' http://localhost/ | grep -o '<title>.*</title>'                          # 商超管理系统
```

## 7. 修改边界

**✅ 可自由改**：`RuoYi-Vue/ruoyi-admin/.../jxc/controller/`、`ruoyi-system/.../jxc/{domain,mapper,service}`、`ruoyi-system/resources/mapper/jxc/*.xml`、`RuoYi-Vue3/src/views/jxc/**`、`docs/**`

**⚠️ 改动前先说明**：`ruoyi-framework`（基座安全/数据权限）、`sys_menu` 菜单数据、`JxcStockCoreMapper`（跨模块库存核心，被 8 条链复用）、`sql/` 已执行脚本

**❌ 禁止改**：`RuoYi-Vue3/src/utils/*`、`src/permission.js`、`src/store/**`、已审单"禁止改删"拦截逻辑（审计合规底线）、`RuoYi-Vue/**/application-druid.yml` 里的连接信息（除换库）

## 8. TOP 7 坑（踩过，别再踩）

1. **`SERVER__PORT` 每次不同**（10-21 实测 3827、历史 9514）→ 必须 `env -u SERVER__PORT` + `--server.port=8080`
2. **`http_proxy` 也是动态注入** → 所有 curl 加 `--noproxy '*'`，否则 502
3. **jar 文件锁** → `mvn package` 前必须停 8080
4. **无 `package-lock.json`** → `npm install` 版本可能漂移，改依赖前先记录版本
5. **`.vite` 缓存含绝对路径** → 换目录后删 `node_modules/.vite` 重建
6. **URL 中文参数被 Tomcat 拒绝**（400）→ 查询参数先 encode
7. **MySQL `UPDATE` 的 SET 从左至右求值** → status 的 CASE 必须写在 balance 赋值之前（核销逻辑已踩）

## 9. 文档索引

| 文档 | 用途 |
|---|---|
| `docs/项目交接文档.md` | ⭐ 背景 / 12 项决策 / 待办 / 启动手册 / 新会话提示词 |
| `docs/项目进度跟踪.md` | ⭐ 唯一进度台账（改完必须更新） |
| `docs/多Agent协同说明.md` | 架构 / 模块职责 / 事务链 / 中间件产物 / 协作约定 |
| `docs/系统功能取舍说明.md` | 为什么有用户/角色/字典等基座功能（答辩口径） |
| `docs/项目迁移清单.md` | 换盘/换机的文件迁移与配置调整 |
| `backup/ry_jxc_20261024.sql` | 数据库全量快照（55 表） |

## 10. 协作约定

1. **改完必须更新** `docs/项目进度跟踪.md` 的「更新记录」，文件是进度唯一来源
2. 代码改动同步到 `repo/`（或 `E:\Git`）并 `git push origin main`；**commit 消息内不要用英文双引号**
3. 事务链相关改动（库存/台账）必须跑第 6 节验证 + 对应拒单场景
4. 一次只让一个 Agent 改后端事务层，避免 `JxcStockCoreMapper` 冲突
