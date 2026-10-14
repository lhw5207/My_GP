# 数据库执行报告（ry_jxc）

> 执行日期：2026-10-14 ｜ 执行环境：MySQL 8.0.42 Community（127.0.0.1:3306，服务 MySQL80）
> 执行人：软件开发团队（主理人齐活林编排，工程师寇豆码产出脚本，QA 严过关审查放行）
> 脚本：`sql/jxc_schema.sql`（建库建表）+ `sql/jxc_data_init.sql` 第一节（演示基础资料）

---

## 1. 执行结果总览

| 项 | 结果 |
|---|---|
| 建库 | ✅ `CREATE DATABASE IF NOT EXISTS ry_jxc`（utf8mb4 / utf8mb4_general_ci） |
| 建表 | ✅ **24 / 24 张表全部成功**（19 个业务实体：16 主表 + 8 明细表口径） |
| 演示数据 | ✅ 分类 2、商品 5（批次效期 3 + 非批次 2）、供应商 2、会员 2 |
| 字典数据 | ⏸ 未执行（设计如此）——需在部署 RuoYi 基座后、在含 sys_* 表的库中执行第二节 |
| 失败项 | **0** |
| 回滚 | 未发生失败，无需回滚；脚本幂等（DROP IF EXISTS + CREATE），可整体重放 |

## 2. 回读核验结论（SHOW CREATE TABLE + information_schema）

| 核验项 | 方法 | 结论 |
|---|---|---|
| 表数量 | `information_schema.TABLES` COUNT | ✅ 24 张，与设计一致 |
| 引擎/字符集 | TABLES 表过滤非 InnoDB / 非 utf8mb4_general_ci | ✅ 全部 InnoDB + utf8mb4_general_ci，异常计数 0 |
| batch_no NULL 陷阱 | COLUMNS 表过滤 batch_no 列 | ✅ 8 张含批次字段的表全部 `NOT NULL DEFAULT ''` |
| 库存唯一键 | SHOW CREATE TABLE jxc_stock | ✅ `UNIQUE KEY uk_product_batch (product_id, batch_no)`；另有 idx_expiry_date（临期预警）、idx_quantity（库存预警） |
| 单号唯一键 | STATISTICS 表过滤 uk_* | ✅ 11 个唯一键：订单/入库/出库/盘点/退货/收付款单号 + 商品编码 + 供应商编码 + 会员号 |
| 精度规范 | COLUMNS 过滤 float/double | ✅ 全库 0 个 FLOAT/DOUBLE 列 |
| 审计字段 | 逐表校验 5 字段齐全 | ✅ 24 张表全部含 create_by/create_time/update_by/update_time/remark |
| 演示数据 | 业务表计数 + 商品抽查 | ✅ 数量正确；3 个批次商品 enable_batch='1' 且 shelf_life_days 有值（270/180/180），2 个非批次商品 '0'/NULL |

**抽检建表语句（jxc_stock）关键内容**：`quantity DECIMAL(18,4) NOT NULL DEFAULT '0.0000' COMMENT '现存数量（负库存校验唯一依据，原子UPDATE扣减）'`、`cost_price DECIMAL(18,4) ... COMMENT '批次成本单价（个别计价法）'` —— 与设计方案 v2 的库存双轨/个别计价要求一致。

## 3. QA 审查关卡（执行前置）

QA（严过关）对脚本 10 项自审清单独立复核（含 MySQL 8.0 保留字外部检索验证）：**PASS 放行**，无 P0/P1 问题。3 项 P2 改进建议已在本次执行后落实：

| P2 项 | 处置 |
|---|---|
| P2-1 初始化脚本非幂等（显式 id 重放冲突） | ✅ 已在脚本头补充幂等性说明与重放前清表指引 |
| P2-2 字典 `jxc_product_unit` 千克项 dict_value 'kg' 与其余中文值不一致 | ✅ 已改为 '千克'（与其他单位取值风格统一，避免前端回显不一致） |
| P2-3 jxc_stock_out 不承载销售出库的取舍需显式说明 | ✅ 已在 database_design.md §3.5 补充"设计决策（已定稿）"段落 |

## 4. 遗留事项

1. **字典数据待执行**：`jxc_data_init.sql` 第二节（14 个字典类型 + 52 条字典数据）需在部署 RuoYi-Vue 基座后，在其 `sys_dict_type`/`sys_dict_data` 所在库执行；执行前按脚本头说明调整 USE 语句。
2. **应用层职责**（表结构已提供全部支撑字段，编码阶段实现）：单号 Redis 自增、审核时刻的事务编排（单据状态 + stock + flow + 台账四处同事务）、FEFO 拆批出库算法、负库存原子扣减（`UPDATE ... WHERE quantity >= ?`）。
3. **方案文档同步**：方案 v2 §2.4.1 的表清单口径（17+2 组）早于最终 schema（24 张物理表），字段层面以 `docs/database_design.md` 与 `jxc_schema.sql` 为准；论文撰写时以本次落库结构为准。

## 5. 交付物清单

| 文件 | 说明 |
|---|---|
| `sql/jxc_schema.sql` | 建库 + 24 张表 DDL（幂等可重放，含备份提醒） |
| `sql/jxc_data_init.sql` | 第一节：ry_jxc 演示数据（已执行）；第二节：RuoYi 字典（待基座部署后执行） |
| `docs/database_design.md` | 逐表设计说明 + 状态机 + 索引对照 + mermaid ER 图 + 事务写入路径 |
| `docs/self_review_report.md` | 工程师 10 项自审报告（IS_PASS: YES） |
| `docs/execution_report.md` | 本报告 |

---

**结论：数据库设计与执行链路（设计 → 自审 → QA 审查 → 执行 → 回读验证）全部完成，`ry_jxc` 库 24 张业务表就绪，可直接进入 RuoYi 代码生成器阶段。**
