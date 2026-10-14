-- =============================================
-- 采购管理：采购入库单菜单（主子表） + 按钮权限 + 授权
-- 说明：在 ry_jxc 库执行；父目录"采购管理"ID=2025
-- =============================================

SET @dirId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 采购入库单菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('采购入库', @dirId, 2, 'stock_in', 'jxc/stock_in/index', 1, 0, 'C', '0', '0', 'jxc:stock_in:list', 'upload', 'admin', sysdate(), '', NULL, '采购入库单菜单');
SET @m1 := LAST_INSERT_ID();

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('入库单查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_in:query', '#', 'admin', sysdate(), '', NULL, ''),
('入库单新增', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_in:add', '#', 'admin', sysdate(), '', NULL, ''),
('入库单修改', @m1, 3, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_in:edit', '#', 'admin', sysdate(), '', NULL, ''),
('入库单删除', @m1, 4, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_in:remove', '#', 'admin', sysdate(), '', NULL, ''),
('入库单导出', @m1, 5, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_in:export', '#', 'admin', sysdate(), '', NULL, ''),
('入库单审核', @m1, 6, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_in:audit', '#', 'admin', sysdate(), '', NULL, '');

-- 授权 admin
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @m1 ORDER BY menu_id;
