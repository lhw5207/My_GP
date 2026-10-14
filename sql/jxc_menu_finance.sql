-- =============================================
-- 往来管理：目录 + 收付款登记 + 应付台账 + 应收台账 + 授权
-- 说明：在 ry_jxc 库执行
-- =============================================

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('往来管理', 0, 4, 'finance', NULL, 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', NULL, '往来管理目录');
SET @dirId := LAST_INSERT_ID();

-- 收付款登记
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('收付款登记', @dirId, 1, 'payment', 'jxc/finance/payment/index', 1, 0, 'C', '0', '0', 'jxc:fin:list', 'money', 'admin', sysdate(), '', NULL, '收付款登记（一对多核销）');
SET @m1 := LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('收付款查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:fin:query', '#', 'admin', sysdate(), '', NULL, ''),
('收付款登记', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:fin:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 应付台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('应付台账', @dirId, 2, 'payable', 'jxc/finance/payable/index', 1, 0, 'C', '0', '0', 'jxc:payable:list', 'list', 'admin', sysdate(), '', NULL, '应付账款台账');
SET @m2 := LAST_INSERT_ID();
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m2);

-- 应收台账
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('应收台账', @dirId, 3, 'receivable', 'jxc/finance/receivable/index', 1, 0, 'C', '0', '0', 'jxc:receivable:list', 'list', 'admin', sysdate(), '', NULL, '应收账款台账');
SET @m3 := LAST_INSERT_ID();
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m3);

-- 授权目录
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @dirId);

-- 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @dirId ORDER BY menu_id;
