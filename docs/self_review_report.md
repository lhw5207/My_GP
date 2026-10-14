# 数据库设计自审报告

> 依据《数据库设计执行提示词》第 6 节自审清单逐项检查。
> 审查对象：`sql/jxc_schema.sql`（19 个业务实体 / 24 张物理表）、`sql/jxc_data_init.sql`、`docs/database_design.md`
> 结论：**10 项全部通过，IS_PASS: YES**（本轮为新建库场景，仅输出脚本不执行 DDL）

---

## 逐项自审结论

### 1. 唯一键 NULL 陷阱 —— ✅ 通过
- `jxc_stock.batch_no` 定义为 `VARCHAR(32) NOT NULL DEFAULT ''`，非批次商品统一写空串，联合唯一键 `uk_product_batch(product_id, batch_no)` 不含 NULL 语义，规避 MySQL 唯一索引对多行 NULL 不去重的陷阱。
- 其余参与唯一索引的字段（各单据 `xxx_no`、`product_code`、`supplier_code`、`member_no`）均为 `NOT NULL`，无 NULL 语义残留。

### 2. 双轨一致性 —— ✅ 通过
- `jxc_stock(product_id, batch_no, quantity, cost_price)` 与 `jxc_stock_flow(product_id, batch_no, change_qty, balance_qty)` 可互相印证：按 `(product_id, batch_no)` 对流水 `SUM(change_qty)` = 现存量 `quantity`。
- `jxc_stock_flow.balance_qty` 记录该批次**每次变动后的结存快照**，可直接对账。
- 流水只增不改：`update_by/update_time` 恒为空（注释已注明），应用层不提供流水修改入口；纠错通过红字/退货单产生反向流水。

### 3. 状态机完整性 —— ✅ 通过
- 采购订单：`0草稿→1已审核→2部分执行/3已执行，仅草稿可作废为4已作废`，由 `jxc_purchase_order_item.received_qty` 驱动部分/已执行判断；字典 `jxc_purchase_order_status`。
- 入库/出库/采购退货/销售退货/盘点/收付款单：`0草稿→1已审核（审核即执行）`，仅草稿可作废 `2已作废`；字典 `jxc_bill_status`。
- 销售单（POS）：保存即已审核（`status DEFAULT '1'`），另有挂单态 `0`；字典 `jxc_sale_order_status`。
- 收款/核销状态：`0未核销 1部分核销 2已核销`；字典 `jxc_writeoff_status`。
- 红字纠错链路完整：采购退货（库存−+应付红冲）、销售退货（库存按原批次回增+退款/应收红冲），已审单不可作废。
- 全部枚举在字段 COMMENT 与 sys_dict_data 中同步体现。

### 4. 应收应付对称 —— ✅ 通过
- `jxc_payable` 与 `jxc_receivable` 字段结构逐一对称：`amount / paid_amount / balance / status` 同名、同型（DECIMAL(18,2)/CHAR(1)）、同语义；`supplier_id↔member_id`、`source_no/source_type` 对应。
- 索引对称：`idx_supplier_status(supplier_id, status)` ↔ `idx_member_status(member_id, status)`。

### 5. 成本快照 —— ✅ 通过
- `jxc_sale_order_item.cost_price / cost_amount`：出库时按所扣批次进价快照写入，此后不可变；毛利 = amount − cost_amount。
- 个别计价法所需字段齐备：`jxc_stock.cost_price`（批次成本单价）+ `jxc_stock_in_item.price`（入库单价即批次成本）+ FEFO 拆批后的明细 `batch_no`。
- `jxc_sale_return_item` 含 `cost_price/cost_amount` 与 `source_sale_item_id`，可还原原销售成本快照用于毛利红冲；`jxc_stock_out_item.price/amount` 为出库成本快照。

### 6. 核销一对多 —— ✅ 通过
- `jxc_payment_item(payment_id, bill_type, bill_id, writeoff_amount)`：一张收付款单多行明细 → 核销多张应收/应付单；同一 `bill_id` 可出现于多张收付款单 → 往来单可被多次部分核销。
- 勾稽关系可由表结构支撑：`payable/receivable.paid_amount = Σ payment_item.writeoff_amount`（按 bill_type+bill_id 聚合），`balance = amount − paid_amount`，status 联动；`idx_bill(bill_type, bill_id)` 支撑反查。

### 7. 精度规范 —— ✅ 通过
- 全库无 FLOAT/DOUBLE（已逐一核对 24 张表）。
- 金额 DECIMAL(18,2)、数量 DECIMAL(18,4)、单价 DECIMAL(18,4) 全局一致。
- 例外说明：`jxc_member.discount DECIMAL(5,2)` 为折扣百分比（0–100），非金额字段，不违反规范。

### 8. 索引覆盖 —— ✅ 通过
| 高频查询 | 索引 |
|---|---|
| 库存预警 | `jxc_stock.uk_product_batch(product_id)` + `idx_quantity`，联查 `jxc_product.stock_min` |
| 临期预警 | `jxc_stock.idx_expiry_date(expiry_date)` |
| 流水追溯 | `jxc_stock_flow.idx_product_batch(product_id, batch_no)`、`idx_biz(biz_type, biz_id)` |
| 单据列表 | 各主表 `uk_xxx_no` 唯一 + `idx_status` + `idx_create_time`；明细表 `idx_父表ID` |
| 往来核销 | `jxc_payable.idx_supplier_status`、`jxc_receivable.idx_member_status`、`jxc_payment_item.idx_bill(bill_type, bill_id)` |
- 附加：`jxc_product.idx_barcode`（扫码）、`idx_category_id`；入/出库单 `idx_source(source_type, source_no)`。

### 9. 冗余检查 —— ✅ 通过
- `jxc_supplier` **无** `payable_balance` 等应付余额冗余字段，供应商应付余额由 `SELECT SUM(balance) FROM jxc_payable WHERE supplier_id=?` 聚合获得；会员侧同理（`jxc_member.balance` 仅为储值余额，非应收往来）。
- 有意保留的冗余均已注明用途且单写多读：`jxc_stock_flow.biz_no`（查询展示冗余）、`jxc_stock_check_item.book_qty`（时点快照，业务必需）、各成本快照字段（快照性质，非可派生数据）。
- `payable/receivable.balance` 为规范允许的派生字段，由程序在核销事务内维护一致性（= amount − paid_amount）。

### 10. 规范一致性 —— ✅ 通过
- 19 个业务实体 / 24 张物理表（基础资料4 + 采购订单2 + 入库单2 + 采购退货2 + 销售单2 + 销售退货2 + 出库单2 + 库存2 + 盘点2 + 资金4）与方案 v2 §2.4.2 表清单完全一致。
- 全部表：统一主键 `id BIGINT AUTO_INCREMENT`、统一 5 个审计字段、`ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci`、表注释与字段注释齐全、无物理外键、单仓库无 warehouse_id 维度。

---

## MySQL 8.0 语法兼容性核查（补充）

| 检查点 | 结论 |
|---|---|
| DATETIME 默认值 | 审计字段均 `DEFAULT NULL`，未使用 MySQL 5.7 之前的非法零日期，兼容 ✅ |
| 保留字 | 未使用 `rank`/`system`/`groups` 等 8.0 新增保留字作标识符；`level/sort/status` 均非保留字 ✅ |
| 索引长度 | 最长索引列为 VARCHAR(32) utf8mb4（128 字节），联合索引总长远低于 3072 字节上限 ✅ |
| 字符集/排序规则 | 库级与表级均显式声明 utf8mb4 / utf8mb4_general_ci ✅ |
| 幂等重放 | 全部 `DROP TABLE IF EXISTS` + `CREATE TABLE`；建库 `CREATE DATABASE IF NOT EXISTS` ✅ |
| 执行位置隔离 | 字典 SQL 单独分节并注明"需在部署 RuoYi 基座后、在含 sys_* 表的库中执行"，与 ry_jxc 业务库隔离 ✅ |

## 修正记录

- 第 1 轮设计稿即全部通过，无需修正项。
- 设计取舍说明：
  1. **表数量口径**：提示词称"17 张核心业务表 + 2 组退货表"，方案 v2 称"19 张业务表"——按方案 v2 §2.4.2 清单实现为 19 个业务实体，主从单据拆为物理表后共 24 张，两个口径均已覆盖。
  2. `jxc_sale_order.status` 默认值取 `'1'`（已审核），契合"POS 保存即生效"的主流路径；挂单场景显式存 `'0'`。
  3. `jxc_member.balance` 保留为储值余额字段（提示词注明"若涉储值"），默认 0，不涉储值时不启用，不构成往来余额冗余。

## 遗留事项

1. 本阶段按任务要求**仅编写 SQL 脚本，未执行 DDL**；执行与 `SHOW CREATE TABLE` / `information_schema` 回读核验待部署环境就绪后进行（脚本幂等可整体重放，新建库无数据风险）。
2. 字典脚本（jxc_data_init.sql 第二节）需按实际 RuoYi 基座所在库调整 `USE` 语句（默认 ry-vue 已注释标明）。
3. 单据编号 Redis 自增、审核事务编排、FEFO 拆批等为应用层职责，表结构已提供全部支撑字段，待编码阶段实现。
