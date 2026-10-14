-- =============================================
-- 采购管理：目录 + 采购订单菜单（主子表） + 按钮权限 + 授权
-- 说明：在 ry_jxc 库执行
-- =============================================

-- 1. 一级目录：采购管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('采购管理', 0, 1, 'purchase', NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', NULL, '采购管理目录');
SET @dirId := LAST_INSERT_ID();

-- 2. 采购订单菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('采购订单', @dirId, 1, 'purchase_order', 'jxc/purchase_order/index', 1, 0, 'C', '0', '0', 'jxc:purchase_order:list', 'form', 'admin', sysdate(), '', NULL, '采购订单菜单');
SET @m1 := LAST_INSERT_ID();

-- 3. 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('采购订单查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_order:query', '#', 'admin', sysdate(), '', NULL, ''),
('采购订单新增', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_order:add', '#', 'admin', sysdate(), '', NULL, ''),
('采购订单修改', @m1, 3, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_order:edit', '#', 'admin', sysdate(), '', NULL, ''),
('采购订单删除', @m1, 4, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_order:remove', '#', 'admin', sysdate(), '', NULL, ''),
('采购订单导出', @m1, 5, '#', '', 1, 0, 'F', '0', '0', 'jxc:purchase_order:export', '#', 'admin', sysdate(), '', NULL, '');

-- 4. 授权 admin
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @dirId), (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 5. 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @dirId ORDER BY menu_id;
