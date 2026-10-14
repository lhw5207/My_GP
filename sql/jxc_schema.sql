-- =============================================================================
-- 中小型商超进销存管理系统 · 数据库建库建表脚本
-- 选题：基于 Spring Boot + Vue 的面向中小型商超的进销存管理系统的设计与实现
-- 数据库：MySQL 8.0.42 ｜ 引擎：InnoDB ｜ 字符集：utf8mb4 / utf8mb4_general_ci
-- 库名：ry_jxc（新建库，不复用 ry-vue；RuoYi 原生 sys_* 表保持原样不动）
--
-- ⚠️ 执行前请备份目标库（本脚本为新建库场景，无数据风险）
-- ⚠️ 本脚本包含 DROP TABLE IF EXISTS，可整体重复执行（幂等重放）
--
-- 全局规范：
--   1. 主键 id BIGINT NOT NULL AUTO_INCREMENT
--   2. 全表审计字段 create_by/create_time/update_by/update_time/remark（对齐 RuoYi）
--   3. 金额 DECIMAL(18,2)、数量 DECIMAL(18,4)、单价 DECIMAL(18,4)，禁止 FLOAT/DOUBLE
--   4. 不使用物理外键，一律逻辑关联，由应用层保证引用完整性
--   5. 业务单据 order_no VARCHAR(32) NOT NULL UNIQUE，规则：前缀+yyyyMMdd+3位流水
--   6. jxc_stock.batch_no NOT NULL DEFAULT ''（规避 MySQL 唯一索引 NULL 陷阱）
-- =============================================================================

CREATE DATABASE IF NOT EXISTS ry_jxc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE ry_jxc;

-- =============================================================================
-- 一、基础资料（4 张）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. jxc_product_category 商品分类表（支持树形）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_product_category;
CREATE TABLE jxc_product_category (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID（0表示根分类）',
  category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
  sort        INT          NOT NULL DEFAULT 0 COMMENT '显示排序（升序）',
  status      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_parent_id (parent_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- -----------------------------------------------------------------------------
-- 2. jxc_product 商品档案表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_product;
CREATE TABLE jxc_product (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  category_id     BIGINT        NOT NULL COMMENT '分类ID（逻辑关联jxc_product_category.id）',
  product_code    VARCHAR(32)   NOT NULL COMMENT '商品编码（唯一）',
  product_name    VARCHAR(128)  NOT NULL COMMENT '商品名称',
  barcode         VARCHAR(64)   DEFAULT NULL COMMENT '商品条码（扫码录入用）',
  unit            VARCHAR(16)   NOT NULL DEFAULT '个' COMMENT '计量单位（字典jxc_product_unit）',
  spec            VARCHAR(64)   DEFAULT NULL COMMENT '规格（如330ml/瓶）',
  purchase_price  DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '参考进价（最近/默认进价，实际成本以批次为准）',
  sale_price      DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '标准售价',
  stock_min       DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '库存预警下限（quantity<=stock_min 触发预警）',
  stock_max       DECIMAL(18,4) DEFAULT NULL COMMENT '库存预警上限（可空）',
  shelf_life_days INT           DEFAULT NULL COMMENT '保质期天数（NULL表示不做效期管理）',
  enable_batch    CHAR(1)       NOT NULL DEFAULT '0' COMMENT '是否批次管理（0否 1是，字典sys_yes_no）',
  status          CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by       VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_code (product_code),
  KEY idx_category_id (category_id),
  KEY idx_barcode (barcode),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品档案表';

-- -----------------------------------------------------------------------------
-- 3. jxc_supplier 供应商表（严禁冗余应付余额字段，余额由 jxc_payable 聚合查询）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_supplier;
CREATE TABLE jxc_supplier (
  id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  supplier_code VARCHAR(32) NOT NULL COMMENT '供应商编码（唯一）',
  supplier_name VARCHAR(128) NOT NULL COMMENT '供应商名称',
  contact       VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  phone         VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  payment_days  INT         DEFAULT NULL COMMENT '账期天数（可空）',
  address       VARCHAR(255) DEFAULT NULL COMMENT '地址',
  status        CHAR(1)     NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by     VARCHAR(64) DEFAULT NULL COMMENT '创建者',
  create_time   DATETIME    DEFAULT NULL COMMENT '创建时间',
  update_by     VARCHAR(64) DEFAULT NULL COMMENT '更新者',
  update_time   DATETIME    DEFAULT NULL COMMENT '更新时间',
  remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_supplier_code (supplier_code),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应商表';

-- -----------------------------------------------------------------------------
-- 4. jxc_member 会员表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_member;
CREATE TABLE jxc_member (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  member_no   VARCHAR(32)   NOT NULL COMMENT '会员号（唯一）',
  member_name VARCHAR(64)   NOT NULL COMMENT '会员姓名',
  phone       VARCHAR(32)   DEFAULT NULL COMMENT '手机号',
  level       VARCHAR(16)   NOT NULL DEFAULT '0' COMMENT '会员等级（字典jxc_member_level）',
  discount    DECIMAL(5,2)  NOT NULL DEFAULT 100.00 COMMENT '等级折扣（百分比，如95.00表示95折）',
  points      DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '积分（简化策略）',
  balance     DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '储值余额（若不涉储值保持为0）',
  status      CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_no (member_no),
  KEY idx_phone (phone),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员表';

-- =============================================================================
-- 二、采购域（采购订单、入库单 各主从 2 张，共 4 张）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 5. jxc_purchase_order 采购订单主表
--    状态机：0草稿 → 1已审核 → 2部分执行 / 3已执行；仅草稿可作废为4已作废
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_purchase_order;
CREATE TABLE jxc_purchase_order (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '采购订单ID',
  order_no     VARCHAR(32)   NOT NULL COMMENT '采购订单号（PO+yyyyMMdd+3位流水）',
  supplier_id  BIGINT        NOT NULL COMMENT '供应商ID（逻辑关联jxc_supplier.id）',
  order_date   DATE          NOT NULL COMMENT '单据日期',
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  status       CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2部分执行 3已执行 4已作废，字典jxc_purchase_order_status）',
  audit_by     VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time   DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by    VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time  DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by    VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time  DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_supplier_id (supplier_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购订单主表';

-- -----------------------------------------------------------------------------
-- 6. jxc_purchase_order_item 采购订单明细表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_purchase_order_item;
CREATE TABLE jxc_purchase_order_item (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  order_id     BIGINT        NOT NULL COMMENT '采购订单ID（逻辑关联jxc_purchase_order.id）',
  row_no       INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id   BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  quantity     DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '采购数量',
  price        DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '采购单价',
  amount       DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '金额（quantity*price）',
  received_qty DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '已入库数量（分批到货对账，驱动部分执行/已执行状态）',
  create_by    VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time  DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by    VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time  DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购订单明细表';

-- -----------------------------------------------------------------------------
-- 7. jxc_stock_in 入库单主表
--    状态机：0草稿 → 1已审核（审核即执行：库存+、流水+、应付+）→ 不可作废（2仅用于草稿作废）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_in;
CREATE TABLE jxc_stock_in (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  in_no        VARCHAR(32)   NOT NULL COMMENT '入库单号（SI+yyyyMMdd+3位流水）',
  source_type  VARCHAR(16)   NOT NULL DEFAULT 'PURCHASE' COMMENT '来源类型（PURCHASE采购入库 CHECK盘点盘盈，字典jxc_stock_in_source_type）',
  source_no    VARCHAR(32)   DEFAULT NULL COMMENT '来源单号（采购订单号/盘点单号，可空）',
  supplier_id  BIGINT        DEFAULT NULL COMMENT '供应商ID（采购入库必填，盘盈入库可空）',
  in_date      DATE          NOT NULL COMMENT '入库日期',
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '入库总金额',
  status       CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2已作废，仅草稿可作废，字典jxc_bill_status）',
  audit_by     VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time   DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by    VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time  DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by    VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time  DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_in_no (in_no),
  KEY idx_supplier_id (supplier_id),
  KEY idx_source (source_type, source_no),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入库单主表';

-- -----------------------------------------------------------------------------
-- 8. jxc_stock_in_item 入库单明细表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_in_item;
CREATE TABLE jxc_stock_in_item (
  id                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  in_id                BIGINT        NOT NULL COMMENT '入库单ID（逻辑关联jxc_stock_in.id）',
  row_no               INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id           BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no             VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（非批次商品统一空串）',
  production_date      DATE          DEFAULT NULL COMMENT '生产日期',
  expiry_date          DATE          DEFAULT NULL COMMENT '失效日期',
  quantity             DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '入库数量',
  price                DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '入库单价（批次成本，盘盈取商品档案进价快照）',
  amount               DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '金额（quantity*price）',
  source_order_id      BIGINT        DEFAULT NULL COMMENT '来源采购订单ID（可空）',
  source_order_item_id BIGINT        DEFAULT NULL COMMENT '来源采购订单明细ID（可空，审核时回写received_qty）',
  create_by            VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time          DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by            VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time          DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark               VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_in_id (in_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入库单明细表';

-- =============================================================================
-- 三、采购退货链路（主从 2 张）：审核 → 库存减 + 应付红冲
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 9. jxc_purchase_return 采购退货单主表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_purchase_return;
CREATE TABLE jxc_purchase_return (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '采购退货单ID',
  return_no    VARCHAR(32)   NOT NULL COMMENT '退货单号（PR+yyyyMMdd+3位流水）',
  supplier_id  BIGINT        NOT NULL COMMENT '供应商ID（逻辑关联jxc_supplier.id）',
  source_in_no VARCHAR(32)   DEFAULT NULL COMMENT '原入库单号（可空）',
  return_date  DATE          NOT NULL COMMENT '退货日期',
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '退货总金额',
  status       CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2已作废，仅草稿可作废，字典jxc_bill_status）',
  audit_by     VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time   DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by    VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time  DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by    VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time  DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_return_no (return_no),
  KEY idx_supplier_id (supplier_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购退货单主表';

-- -----------------------------------------------------------------------------
-- 10. jxc_purchase_return_item 采购退货单明细表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_purchase_return_item;
CREATE TABLE jxc_purchase_return_item (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  return_id   BIGINT        NOT NULL COMMENT '采购退货单ID（逻辑关联jxc_purchase_return.id）',
  row_no      INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id  BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（按原入库批次退货，非批次商品空串）',
  quantity    DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '退货数量',
  price       DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '退货单价（按原入库进价）',
  amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '金额（quantity*price）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_return_id (return_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购退货单明细表';

-- =============================================================================
-- 四、销售域（销售单主从 2 张）：POS 保存即审核生效（一手交钱一手交货）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 11. jxc_sale_order 销售单主表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_sale_order;
CREATE TABLE jxc_sale_order (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '销售单ID',
  sale_no         VARCHAR(32)   NOT NULL COMMENT '销售单号（XS+yyyyMMdd+3位流水）',
  member_id       BIGINT        DEFAULT NULL COMMENT '会员ID（可空，散客为空；逻辑关联jxc_member.id）',
  sale_date       DATE          NOT NULL COMMENT '销售日期',
  total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '商品总额（优惠前）',
  discount_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（会员折扣等）',
  actual_amount   DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应收金额（=total_amount-discount_amount）',
  received_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '实收金额（现款全额，赊销为0或部分）',
  pay_type        VARCHAR(16)   NOT NULL DEFAULT 'CASH' COMMENT '支付方式（CASH现金 WECHAT微信 ALIPAY支付宝 CARD银行卡 CREDIT挂账，字典jxc_pay_type）',
  pay_status      CHAR(1)       NOT NULL DEFAULT '0' COMMENT '收款状态（0未收 1部分 2已收，字典jxc_writeoff_status）',
  status          CHAR(1)       NOT NULL DEFAULT '1' COMMENT '状态（0草稿/挂单 1已审核 2已作废，POS保存即已审核，字典jxc_sale_order_status）',
  audit_by        VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time      DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by       VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sale_no (sale_no),
  KEY idx_member_id (member_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售单主表（POS即简化销售单）';

-- -----------------------------------------------------------------------------
-- 12. jxc_sale_order_item 销售单明细表（含成本快照，个别计价法）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_sale_order_item;
CREATE TABLE jxc_sale_order_item (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  sale_id     BIGINT        NOT NULL COMMENT '销售单ID（逻辑关联jxc_sale_order.id）',
  row_no      INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id  BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（FEFO拆批出库后的实际扣减批次，非批次商品空串）',
  quantity    DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '销售数量',
  price       DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '销售单价',
  amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '销售金额（quantity*price）',
  cost_price  DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '成本单价快照（出库时按所扣批次进价写入，此后不可变）',
  cost_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '成本金额快照（quantity*cost_price，毛利=amount-cost_amount）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_sale_id (sale_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售单明细表';

-- =============================================================================
-- 五、销售退货链路（主从 2 张）：审核 → 库存按原批次回增 + 退款/应收红冲
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 13. jxc_sale_return 销售退货单主表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_sale_return;
CREATE TABLE jxc_sale_return (
  id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '销售退货单ID',
  return_no      VARCHAR(32)   NOT NULL COMMENT '退货单号（SR+yyyyMMdd+3位流水）',
  source_sale_no VARCHAR(32)   DEFAULT NULL COMMENT '原销售单号（可空）',
  member_id      BIGINT        DEFAULT NULL COMMENT '会员ID（可空，逻辑关联jxc_member.id）',
  return_date    DATE          NOT NULL COMMENT '退货日期',
  total_amount   DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '退货总金额',
  refund_amount  DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额（现退部分；赊销则冲应收）',
  status         CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2已作废，仅草稿可作废，字典jxc_bill_status）',
  audit_by       VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time     DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by      VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time    DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by      VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark         VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_return_no (return_no),
  KEY idx_member_id (member_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售退货单主表';

-- -----------------------------------------------------------------------------
-- 14. jxc_sale_return_item 销售退货单明细表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_sale_return_item;
CREATE TABLE jxc_sale_return_item (
  id                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  return_id          BIGINT        NOT NULL COMMENT '销售退货单ID（逻辑关联jxc_sale_return.id）',
  row_no             INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id         BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no           VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（按原销售批次回增库存，非批次商品空串）',
  quantity           DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '退货数量',
  price              DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '退货单价（按原销售价）',
  amount             DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '退货金额（quantity*price）',
  cost_price         DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '成本单价快照（还原原销售明细成本，用于毛利红冲）',
  cost_amount        DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '成本金额快照（quantity*cost_price）',
  source_sale_item_id BIGINT       DEFAULT NULL COMMENT '原销售单明细ID（还原成本快照依据，可空）',
  create_by          VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time        DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by          VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time        DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark             VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_return_id (return_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='销售退货单明细表';

-- =============================================================================
-- 六、出库单（主从 2 张）：仅承载盘亏与采购退货等"无销售行为的出库"
--     （销售出库由销售单直接驱动，不重复建单，避免一业务两单）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 15. jxc_stock_out 出库单主表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_out;
CREATE TABLE jxc_stock_out (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  out_no       VARCHAR(32)   NOT NULL COMMENT '出库单号（CK+yyyyMMdd+3位流水）',
  source_type  VARCHAR(16)   NOT NULL COMMENT '来源类型（PURCHASE_RETURN采购退货 CHECK盘点盘亏 OTHER其他出库，字典jxc_stock_out_source_type）',
  source_no    VARCHAR(32)   DEFAULT NULL COMMENT '来源单号（采购退货单号/盘点单号，可空）',
  out_date     DATE          NOT NULL COMMENT '出库日期',
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '出库成本总金额',
  status       CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2已作废，仅草稿可作废，字典jxc_bill_status）',
  audit_by     VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time   DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by    VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time  DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by    VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time  DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_out_no (out_no),
  KEY idx_source (source_type, source_no),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库单主表';

-- -----------------------------------------------------------------------------
-- 16. jxc_stock_out_item 出库单明细表（携带批次与成本快照）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_out_item;
CREATE TABLE jxc_stock_out_item (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  out_id      BIGINT        NOT NULL COMMENT '出库单ID（逻辑关联jxc_stock_out.id）',
  row_no      INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id  BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（非批次商品空串）',
  quantity    DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '出库数量',
  price       DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '成本单价快照（按所扣批次成本）',
  amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '成本金额（quantity*price）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_out_id (out_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出库单明细表';

-- =============================================================================
-- 七、库存双轨（jxc_stock 现存量 + jxc_stock_flow 全量流水，共 2 张）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 17. jxc_stock 实时库存表（库存校验唯一依据）
--     batch_no NOT NULL DEFAULT ''：非批次商品统一空串，规避唯一索引NULL陷阱
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock;
CREATE TABLE jxc_stock (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  product_id      BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no        VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（非批次商品统一空串，参与唯一键）',
  production_date DATE          DEFAULT NULL COMMENT '生产日期',
  expiry_date     DATE          DEFAULT NULL COMMENT '失效日期（临期预警依据：expiry_date-当前日期<=阈值）',
  quantity        DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '现存数量（负库存校验唯一依据，原子UPDATE扣减）',
  cost_price      DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '批次成本单价（个别计价法）',
  create_by       VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_batch (product_id, batch_no),
  KEY idx_expiry_date (expiry_date),
  KEY idx_quantity (quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='实时库存表（现存量，批次维度）';

-- -----------------------------------------------------------------------------
-- 18. jxc_stock_flow 库存流水台账（只增不改，按批次记录变动后结存快照）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_flow;
CREATE TABLE jxc_stock_flow (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  product_id  BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（非批次商品空串）',
  flow_type   CHAR(1)       NOT NULL COMMENT '变动方向（1入库 2出库，字典jxc_flow_type）',
  biz_type    VARCHAR(16)   NOT NULL COMMENT '业务类型（PURCHASE_IN采购入库 SALE_OUT销售出库 PURCHASE_RETURN采购退货 SALE_RETURN销售退货 CHECK_IN盘盈 CHECK_OUT盘亏 INIT期初，字典jxc_biz_type）',
  biz_id      BIGINT        NOT NULL COMMENT '业务单据ID（逻辑关联各单据主表）',
  biz_item_id BIGINT        DEFAULT NULL COMMENT '业务单据明细ID',
  biz_no      VARCHAR(32)   DEFAULT NULL COMMENT '业务单据编号（冗余便于查询展示）',
  change_qty  DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '变动数量（入库为正，出库为负）',
  balance_qty DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '结存数量（该批次本次变动后的结存快照，与jxc_stock互相印证）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '操作人',
  create_time DATETIME      DEFAULT NULL COMMENT '变动时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者（流水只增不改，恒为空）',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间（流水只增不改，恒为空）',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_product_batch (product_id, batch_no),
  KEY idx_biz (biz_type, biz_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='库存流水台账（只增不改）';

-- =============================================================================
-- 八、盘点（主从 2 张）：审核后盘盈生成入库单 / 盘亏生成出库单
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 19. jxc_stock_check 盘点单主表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_check;
CREATE TABLE jxc_stock_check (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  check_no    VARCHAR(32)  NOT NULL COMMENT '盘点单号（PD+yyyyMMdd+3位流水）',
  check_date  DATE         NOT NULL COMMENT '盘点日期',
  status      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2已作废，仅草稿可作废；审核即生成调整单，字典jxc_bill_status）',
  audit_by    VARCHAR(64)  DEFAULT NULL COMMENT '审核人',
  audit_time  DATETIME     DEFAULT NULL COMMENT '审核时间',
  create_by   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_check_no (check_no),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='盘点单主表';

-- -----------------------------------------------------------------------------
-- 20. jxc_stock_check_item 盘点单明细表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_stock_check_item;
CREATE TABLE jxc_stock_check_item (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  check_id    BIGINT        NOT NULL COMMENT '盘点单ID（逻辑关联jxc_stock_check.id）',
  row_no      INT           NOT NULL DEFAULT 1 COMMENT '行号',
  product_id  BIGINT        NOT NULL COMMENT '商品ID（逻辑关联jxc_product.id）',
  batch_no    VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '批次号（非批次商品空串）',
  book_qty    DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '账面数量（盘点时jxc_stock快照）',
  actual_qty  DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '实盘数量',
  diff_qty    DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '盈亏数量（=actual_qty-book_qty，正为盈负为亏）',
  gen_in_no   VARCHAR(32)   DEFAULT NULL COMMENT '生成的盘盈入库单号（审核后回写）',
  gen_out_no  VARCHAR(32)   DEFAULT NULL COMMENT '生成的盘亏出库单号（审核后回写）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_check_id (check_id),
  KEY idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='盘点单明细表';

-- =============================================================================
-- 九、资金域（应收应付对称 + 收付款一对多核销，共 4 张）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 21. jxc_payable 应付台账
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_payable;
CREATE TABLE jxc_payable (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '应付ID',
  supplier_id BIGINT        NOT NULL COMMENT '供应商ID（逻辑关联jxc_supplier.id）',
  source_no   VARCHAR(32)   NOT NULL COMMENT '来源单据号（入库单号/采购退货单号）',
  source_type VARCHAR(16)   NOT NULL DEFAULT 'STOCK_IN' COMMENT '来源类型（STOCK_IN采购入库 PURCHASE_RETURN采购退货红冲，字典jxc_bill_source_type）',
  amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应付总额（红冲为负数）',
  paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '已核销金额（由jxc_payment_item累计）',
  balance     DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '未付余额（=amount-paid_amount，程序维护一致性）',
  status      CHAR(1)       NOT NULL DEFAULT '0' COMMENT '核销状态（0未核销 1部分核销 2已核销，字典jxc_writeoff_status）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_supplier_status (supplier_id, status),
  KEY idx_source_no (source_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应付台账';

-- -----------------------------------------------------------------------------
-- 22. jxc_receivable 应收台账（与 jxc_payable 字段结构完全对称）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_receivable;
CREATE TABLE jxc_receivable (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '应收ID',
  member_id   BIGINT        NOT NULL COMMENT '会员ID（逻辑关联jxc_member.id）',
  source_no   VARCHAR(32)   NOT NULL COMMENT '来源单据号（销售单号/销售退货单号）',
  source_type VARCHAR(16)   NOT NULL DEFAULT 'SALE' COMMENT '来源类型（SALE销售赊销 SALE_RETURN销售退货红冲，字典jxc_bill_source_type）',
  amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '应收总额（红冲为负数）',
  paid_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '已核销金额（由jxc_payment_item累计）',
  balance     DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '未收余额（=amount-paid_amount，程序维护一致性）',
  status      CHAR(1)       NOT NULL DEFAULT '0' COMMENT '核销状态（0未核销 1部分核销 2已核销，字典jxc_writeoff_status）',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_member_status (member_id, status),
  KEY idx_source_no (source_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应收台账（与应付对称）';

-- -----------------------------------------------------------------------------
-- 23. jxc_payment 收付款单主表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_payment;
CREATE TABLE jxc_payment (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '收付款单ID',
  pay_no      VARCHAR(32)   NOT NULL COMMENT '收付款单号（FK付款/SK收款+yyyyMMdd+3位流水）',
  target_type CHAR(1)       NOT NULL COMMENT '往来对象类型（1供应商付款 2会员收款，字典jxc_payment_target_type）',
  target_id   BIGINT        NOT NULL COMMENT '往来对象ID（target_type=1关联jxc_supplier.id，=2关联jxc_member.id）',
  pay_date    DATE          NOT NULL COMMENT '收付款日期',
  amount      DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '收付款总金额（=核销明细writeoff_amount合计）',
  pay_type    VARCHAR(16)   NOT NULL DEFAULT 'CASH' COMMENT '支付方式（CASH现金 WECHAT微信 ALIPAY支付宝 CARD银行卡 TRANSFER转账，字典jxc_pay_type）',
  status      CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0草稿 1已审核 2已作废，仅草稿可作废；审核即执行核销，字典jxc_bill_status）',
  audit_by    VARCHAR(64)   DEFAULT NULL COMMENT '审核人',
  audit_time  DATETIME      DEFAULT NULL COMMENT '审核时间',
  create_by   VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pay_no (pay_no),
  KEY idx_target (target_type, target_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收付款单主表';

-- -----------------------------------------------------------------------------
-- 24. jxc_payment_item 收付款核销明细表（一张收付款单核销多张往来单，一对多）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS jxc_payment_item;
CREATE TABLE jxc_payment_item (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '核销明细ID',
  payment_id      BIGINT        NOT NULL COMMENT '收付款单ID（逻辑关联jxc_payment.id）',
  row_no          INT           NOT NULL DEFAULT 1 COMMENT '行号',
  bill_type       CHAR(1)       NOT NULL COMMENT '核销单据类型（1应付 2应收，字典jxc_payment_item_bill_type）',
  bill_id         BIGINT        NOT NULL COMMENT '被核销往来单ID（bill_type=1关联jxc_payable.id，=2关联jxc_receivable.id）',
  writeoff_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本次核销金额（累计至往来单paid_amount，balance递减、status联动）',
  create_by       VARCHAR(64)   DEFAULT NULL COMMENT '创建者',
  create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT NULL COMMENT '更新者',
  update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
  remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_payment_id (payment_id),
  KEY idx_bill (bill_type, bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收付款核销明细表（一对多核销）';

-- =============================================================================
-- 建表完成，共 24 张物理表（19 个业务实体，主从单据按 主表+明细表 两张物理表计）
-- 基础资料4 + 采购订单2 + 入库单2 + 采购退货2 + 销售单2 + 销售退货2
-- + 出库单2 + 库存2 + 盘点2 + 资金4 = 24
-- =============================================================================
