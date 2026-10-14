-- =============================================
-- 退货模块菜单：采购退货（采购管理下） + 销售退货（销售管理下）
-- 说明：在 ry_jxc 库执行
-- =============================================

-- 1. 采购退货（采购管理 ID=2025）
SET @dirPurchase = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('采购退货', @dirPurchase, 3, 'purchase_return', 'jxc/purchase_return/index', 1, 0, 'C', '0', '0', 'jxc:purchase_return:list', 'undo', 'admin', sysdate(), '', NULL, '采购退货菜单');
SET @m1 := LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('退货单查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_return:query', '#', 'admin', sysdate(), '', NULL, ''),
('退货单新增', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_return:add', '#', 'admin', sysdate(), '', NULL, ''),
('退货单修改', @m1, 3, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_return:edit', '#', 'admin', sysdate(), '', NULL, ''),
('退货单删除', @m1, 4, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_return:remove', '#', 'admin', sysdate(), '', NULL, ''),
('退货单导出', @m1, 5, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_return:export', '#', 'admin', sysdate(), '', NULL, ''),
('退货单审核', @m1, 6, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_return:audit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 2. 销售退货（销售管理 ID=2039）
SET @dirSale = (SELECT menu_id FROM sys_menu WHERE menu_name='销售管理' AND parent_id=0 LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销售退货', @dirSale, 2, 'sale_return', 'jxc/sale_return/index', 1, 0, 'C', '0', '0', 'jxc:sale_return:list', 'undo', 'admin', sysdate(), '', NULL, '销售退货菜单');
SET @m2 := LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('退货单查询', @m2, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_return:query', '#', 'admin', sysdate(), '', NULL, ''),
('退货单新增', @m2, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_return:add', '#', 'admin', sysdate(), '', NULL, ''),
('退货单修改', @m2, 3, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_return:edit', '#', 'admin', sysdate(), '', NULL, ''),
('退货单删除', @m2, 4, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_return:remove', '#', 'admin', sysdate(), '', NULL, ''),
('退货单导出', @m2, 5, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_return:export', '#', 'admin', sysdate(), '', NULL, ''),
('退货单审核', @m2, 6, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_return:audit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m2);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m2;

-- 3. 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @m1 ORDER BY menu_id;
