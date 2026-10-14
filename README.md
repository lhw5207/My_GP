# 中小型商超进销存管理系统（毕业设计）

> 本科毕业设计项目：《基于 Spring Boot + Vue 的面向中小型商超的进销存管理系统的设计与实现》
> 技术基座：**RuoYi-Vue v3.9.1**（后端，JDK8）+ **RuoYi-Vue3**（前端，Vue3 + Vite + Element Plus）

## 目录结构

```
├── backend/          RuoYi-Vue v3.9.1 后端源码（JDK8 / Spring Boot 2.5.15 + 原生 MyBatis）
│                     └── 数据源已配置指向 ry_jxc（application-druid.yml）
├── frontend/         RuoYi-Vue3 前端源码（Vue 3.5 + Vite 6 + Element Plus 2.13）
├── sql/
│   ├── jxc_schema.sql       业务库建库建表脚本（ry_jxc，24 张 jxc_ 表，幂等可重放）
│   └── jxc_data_init.sql    演示基础资料 + RuoYi 数据字典（14 类型 / 52 数据）
└── docs/
    ├── 毕设调研与规划设计方案.md     GitHub 调研（jshERP/芋道/ydao-erp2 等 5 项目）+ 完整设计方案
    ├── database_design.md          逐表设计说明 + 状态机 + 索引对照 + ER 图
    ├── self_review_report.md       数据库自审报告（10 项清单）
    ├── execution_report.md         数据库执行与回读核验报告
    ├── deploy_report.md            项目部署报告（多代理分模块部署）
    ├── 数据库设计执行提示词.md      数据库设计→审查→执行的标准化提示词
    └── 部署执行提示词.md            分模块并行部署的标准化提示词
```

## 业务模块（规划）

基础资料（商品/供应商/会员）· 采购（订单/入库/退货）· 销售（POS/退货/收款核销）· 库存（流水/盘点/预警）· 往来账款（应收应付对称 + 一对多核销）· 统计报表 · 系统管理（RuoYi 原生）

**核心设计**：库存双轨（现存量 + 全量流水）、批次效期 FEFO、个别计价成本核算、审核即执行 + 红字纠错、单仓库边界。

## 快速启动

```bash
# 1. Redis（先启动）
redis-server.exe --port 6379

# 2. 数据库初始化
mysql -u root -p < sql/jxc_schema.sql
mysql -u root -p ry_jxc < sql/jxc_data_init.sql      # 第一节（演示数据）
# 第二节字典在 RuoYi 基座库中执行

# 3. 后端（JDK8）
cd backend && mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=8080

# 4. 前端（Node 18+）
cd frontend && npm install && npm run dev     # http://localhost
```

| 访问 | 地址 | 账号 |
|---|---|---|
| 前端登录页 | http://localhost | admin / admin123 |
| 后端 API | http://localhost:8080 | — |
| 数据库 | MySQL `ry_jxc` | root / 123456 |

## 说明

- `backend/` 为 RuoYi-Vue v3.9.1 源码快照（上游 MIT 协议），数据源配置已改为 `ry_jxc`；`frontend/` 为 RuoYi-Vue3 源码快照
- 参考项目：jishenghua/jshERP（业务模型蓝本，GPL 仅参考设计不抄代码）、YunaiV/ruoyi-vue-pro 的 yudao-module-erp（表结构模板，MIT）
- 部署验证记录见 `docs/deploy_report.md`
