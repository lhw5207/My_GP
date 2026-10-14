-- =============================================================================
-- 中小型商超进销存管理系统 · 基础数据初始化脚本
-- 数据库：MySQL 8.0.42 ｜ 字符集：utf8mb4 / utf8mb4_general_ci
--
-- ⚠️ 执行前请备份目标库（本脚本为新建库场景，无数据风险）
--
-- ⚠️ 幂等性说明：本脚本含显式 id 的 INSERT，仅可执行一次；重放前请先清空对应表
--   （TRUNCATE jxc_product_category, jxc_product, jxc_supplier, jxc_member），
--   字典节重放前需先删除对应 dict_type 的旧数据，否则会主键冲突/产生重复字典。
--
-- 本脚本分两部分，执行位置不同，请分节执行：
--   第一节：演示基础资料 —— 在 ry_jxc 库中执行（jxc_schema.sql 执行完之后）
--   第二节：RuoYi 字典数据 —— 需在部署 RuoYi 基座后、在含 sys_* 表的库中执行
-- =============================================================================


-- #############################################################################
-- 第一节：演示基础资料（在 ry_jxc 库执行）
--   内容：2 个商品分类、5 个商品（含批次管理与非批次）、2 个供应商、2 个会员
-- #############################################################################
USE ry_jxc;

-- -----------------------------------------------------------------------------
-- 1.1 商品分类（2 个根分类）
-- -----------------------------------------------------------------------------
INSERT INTO jxc_product_category (id, parent_id, category_name, sort, status, create_by, create_time, remark) VALUES
(1, 0, '食品饮料', 1, '0', 'admin', NOW(), '演示数据：食品类根分类'),
(2, 0, '日用百货', 2, '0', 'admin', NOW(), '演示数据：百货类根分类');

-- -----------------------------------------------------------------------------
-- 1.2 商品档案（5 个：3 个批次效期管理 + 2 个非批次管理）
-- -----------------------------------------------------------------------------
INSERT INTO jxc_product (id, category_id, product_code, product_name, barcode, unit, spec, purchase_price, sale_price, stock_min, stock_max, shelf_life_days, enable_batch, status, create_by, create_time, remark) VALUES
(1, 1, 'SP0001', '可口可乐', '6901234100012', '瓶', '330ml/瓶', 1.8000, 2.5000, 24.0000, 240.0000, 270, '1', '0', 'admin', NOW(), '演示数据：批次效期管理商品'),
(2, 1, 'SP0002', '伊利纯牛奶', '6901234100029', '盒', '250ml/盒', 1.5000, 2.0000, 36.0000, 360.0000, 180, '1', '0', 'admin', NOW(), '演示数据：批次效期管理商品'),
(3, 1, 'SP0003', '康师傅红烧牛肉面', '6901234100036', '桶', '105g/桶', 3.2000, 4.5000, 24.0000, 240.0000, 180, '1', '0', 'admin', NOW(), '演示数据：批次效期管理商品'),
(4, 2, 'SP0004', '蓝月亮洗衣液', '6901234100043', '瓶', '1kg/瓶', 12.0000, 19.9000, 12.0000, 120.0000, NULL, '0', '0', 'admin', NOW(), '演示数据：非批次管理商品'),
(5, 2, 'SP0005', '清风抽纸', '6901234100050', '提', '3包/提', 8.5000, 13.9000, 12.0000, 120.0000, NULL, '0', '0', 'admin', NOW(), '演示数据：非批次管理商品');

-- -----------------------------------------------------------------------------
-- 1.3 供应商（2 个）
-- -----------------------------------------------------------------------------
INSERT INTO jxc_supplier (id, supplier_code, supplier_name, contact, phone, payment_days, address, status, create_by, create_time, remark) VALUES
(1, 'GYS001', '成都红旗食品批发部', '张伟', '13800000001', 30, '成都市武侯区食品城A区12号', '0', 'admin', NOW(), '演示数据：食品类供应商'),
(2, 'GYS002', '成都日用百货配送中心', '李芳', '13800000002', 15, '成都市金牛区百货市场B区8号', '0', 'admin', NOW(), '演示数据：百货类供应商');

-- -----------------------------------------------------------------------------
-- 1.4 会员（2 个）
-- -----------------------------------------------------------------------------
INSERT INTO jxc_member (id, member_no, member_name, phone, level, discount, points, balance, status, create_by, create_time, remark) VALUES
(1, 'M0001', '王强', '13900000001', '1', 98.00, 0.0000, 0.00, '0', 'admin', NOW(), '演示数据：银卡会员'),
(2, 'M0002', '刘敏', '13900000002', '2', 95.00, 120.0000, 0.00, '0', 'admin', NOW(), '演示数据：金卡会员');


-- #############################################################################
-- 第二节：RuoYi 数据字典（sys_dict_type / sys_dict_data）
-- ⚠️ 本节需在部署 RuoYi 基座后、在含 sys_* 表的库中执行
--    （通常为 ry-vue 库；若 RuoYi 基座与业务表同库部署则在同库执行）
--    请勿在未初始化 RuoYi 基座的库中执行本节，否则将因 sys_* 表不存在而报错。
-- #############################################################################

-- USE ry_vue;  -- 按实际 RuoYi 基座所在库调整

-- -----------------------------------------------------------------------------
-- 2.1 字典类型
-- -----------------------------------------------------------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('商品计量单位',       'jxc_product_unit',          '0', 'admin', NOW(), '进销存：商品计量单位'),
('会员等级',           'jxc_member_level',          '0', 'admin', NOW(), '进销存：会员等级'),
('支付方式',           'jxc_pay_type',              '0', 'admin', NOW(), '进销存：销售/收付款支付方式'),
('采购订单状态',       'jxc_purchase_order_status', '0', 'admin', NOW(), '进销存：采购订单状态机'),
('销售单状态',         'jxc_sale_order_status',     '0', 'admin', NOW(), '进销存：销售单状态机'),
('通用单据状态',       'jxc_bill_status',           '0', 'admin', NOW(), '进销存：入出库/退货/盘点/收付款单状态机（审核即执行，仅草稿可作废）'),
('核销状态',           'jxc_writeoff_status',       '0', 'admin', NOW(), '进销存：应收应付核销状态/销售单收款状态'),
('库存流水方向',       'jxc_flow_type',             '0', 'admin', NOW(), '进销存：库存流水变动方向'),
('库存业务类型',       'jxc_biz_type',              '0', 'admin', NOW(), '进销存：库存流水业务类型'),
('入库单来源类型',     'jxc_stock_in_source_type',  '0', 'admin', NOW(), '进销存：入库单来源'),
('出库单来源类型',     'jxc_stock_out_source_type', '0', 'admin', NOW(), '进销存：出库单来源'),
('往来单来源类型',     'jxc_bill_source_type',      '0', 'admin', NOW(), '进销存：应收应付来源单据类型'),
('收付款对象类型',     'jxc_payment_target_type',   '0', 'admin', NOW(), '进销存：收付款单往来对象类型'),
('核销单据类型',       'jxc_payment_item_bill_type','0', 'admin', NOW(), '进销存：收付款核销明细单据类型');

-- -----------------------------------------------------------------------------
-- 2.2 字典数据
-- -----------------------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- 商品计量单位
(1, '个',  '个',  'jxc_product_unit', '', 'default', 'Y', '0', 'admin', NOW(), ''),
(2, '瓶',  '瓶',  'jxc_product_unit', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '盒',  '盒',  'jxc_product_unit', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(4, '桶',  '桶',  'jxc_product_unit', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(5, '提',  '提',  'jxc_product_unit', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(6, '袋',  '袋',  'jxc_product_unit', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(7, '千克', '千克', 'jxc_product_unit', '', 'primary', 'N', '0', 'admin', NOW(), ''),
-- 会员等级
(1, '普通会员', '0', 'jxc_member_level', '', 'default', 'Y', '0', 'admin', NOW(), ''),
(2, '银卡会员', '1', 'jxc_member_level', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '金卡会员', '2', 'jxc_member_level', '', 'success', 'N', '0', 'admin', NOW(), ''),
-- 支付方式
(1, '现金',   'CASH',     'jxc_pay_type', '', 'success', 'Y', '0', 'admin', NOW(), ''),
(2, '微信',   'WECHAT',   'jxc_pay_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '支付宝', 'ALIPAY',   'jxc_pay_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(4, '银行卡', 'CARD',     'jxc_pay_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(5, '挂账',   'CREDIT',   'jxc_pay_type', '', 'warning', 'N', '0', 'admin', NOW(), '销售挂账（赊销）'),
(6, '转账',   'TRANSFER', 'jxc_pay_type', '', 'info',    'N', '0', 'admin', NOW(), '往来收付款转账'),
-- 采购订单状态（0草稿→1已审核→2部分执行/3已执行，仅草稿可作废4）
(1, '草稿',     '0', 'jxc_purchase_order_status', '', 'info',    'Y', '0', 'admin', NOW(), ''),
(2, '已审核',   '1', 'jxc_purchase_order_status', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '部分执行', '2', 'jxc_purchase_order_status', '', 'warning', 'N', '0', 'admin', NOW(), '分批到货中'),
(4, '已执行',   '3', 'jxc_purchase_order_status', '', 'success', 'N', '0', 'admin', NOW(), '全部入库完成，不可作废'),
(5, '已作废',   '4', 'jxc_purchase_order_status', '', 'danger',  'N', '0', 'admin', NOW(), '仅草稿可作废'),
-- 销售单状态（POS保存即已审核）
(1, '草稿/挂单', '0', 'jxc_sale_order_status', '', 'info',    'N', '0', 'admin', NOW(), ''),
(2, '已审核',    '1', 'jxc_sale_order_status', '', 'success', 'Y', '0', 'admin', NOW(), '保存即审核生效'),
(3, '已作废',    '2', 'jxc_sale_order_status', '', 'danger',  'N', '0', 'admin', NOW(), '已审核不可作废，纠错走销售退货'),
-- 通用单据状态（入库/出库/采购退货/销售退货/盘点/收付款：审核即执行，仅草稿可作废）
(1, '草稿',   '0', 'jxc_bill_status', '', 'info',    'Y', '0', 'admin', NOW(), ''),
(2, '已审核', '1', 'jxc_bill_status', '', 'success', 'N', '0', 'admin', NOW(), '审核即执行库存与往来联动'),
(3, '已作废', '2', 'jxc_bill_status', '', 'danger',  'N', '0', 'admin', NOW(), '仅草稿可作废，已审单纠错走红字/退货'),
-- 核销状态（应收应付核销状态；销售单收款状态同用）
(1, '未核销',   '0', 'jxc_writeoff_status', '', 'danger',  'Y', '0', 'admin', NOW(), ''),
(2, '部分核销', '1', 'jxc_writeoff_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(3, '已核销',   '2', 'jxc_writeoff_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
-- 库存流水方向
(1, '入库', '1', 'jxc_flow_type', '', 'success', 'Y', '0', 'admin', NOW(), ''),
(2, '出库', '2', 'jxc_flow_type', '', 'danger',  'N', '0', 'admin', NOW(), ''),
-- 库存业务类型
(1, '采购入库',     'PURCHASE_IN',     'jxc_biz_type', '', 'success', 'Y', '0', 'admin', NOW(), ''),
(2, '销售出库',     'SALE_OUT',        'jxc_biz_type', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(3, '采购退货',     'PURCHASE_RETURN', 'jxc_biz_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '销售退货',     'SALE_RETURN',     'jxc_biz_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(5, '盘盈入库',     'CHECK_IN',        'jxc_biz_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(6, '盘亏出库',     'CHECK_OUT',       'jxc_biz_type', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(7, '期初建账',     'INIT',            'jxc_biz_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
-- 入库单来源类型
(1, '采购入库', 'PURCHASE', 'jxc_stock_in_source_type', '', 'success', 'Y', '0', 'admin', NOW(), ''),
(2, '盘点盘盈', 'CHECK',    'jxc_stock_in_source_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
-- 出库单来源类型
(1, '采购退货', 'PURCHASE_RETURN', 'jxc_stock_out_source_type', '', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '盘点盘亏', 'CHECK',           'jxc_stock_out_source_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '其他出库', 'OTHER',           'jxc_stock_out_source_type', '', 'info',    'N', '0', 'admin', NOW(), '如损耗出库'),
-- 往来单来源类型
(1, '采购入库', 'STOCK_IN',        'jxc_bill_source_type', '', 'success', 'Y', '0', 'admin', NOW(), '生成应付'),
(2, '采购退货', 'PURCHASE_RETURN', 'jxc_bill_source_type', '', 'warning', 'N', '0', 'admin', NOW(), '应付红冲'),
(3, '销售赊销', 'SALE',            'jxc_bill_source_type', '', 'primary', 'N', '0', 'admin', NOW(), '生成应收'),
(4, '销售退货', 'SALE_RETURN',     'jxc_bill_source_type', '', 'info',    'N', '0', 'admin', NOW(), '应收红冲'),
-- 收付款对象类型
(1, '供应商付款', '1', 'jxc_payment_target_type', '', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '会员收款',   '2', 'jxc_payment_target_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
-- 核销单据类型
(1, '应付单', '1', 'jxc_payment_item_bill_type', '', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '应收单', '2', 'jxc_payment_item_bill_type', '', 'success', 'N', '0', 'admin', NOW(), '');

-- =============================================================================
-- 初始化完成
-- 第一节：分类2 + 商品5（批次3/非批次2）+ 供应商2 + 会员2
-- 第二节：字典类型14 + 字典数据52
-- =============================================================================
