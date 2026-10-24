-- =============================================
-- 前台收银：菜单 + 按钮权限 + 授权（挂在既有「销售管理」目录下）
-- 说明：在 ry_jxc 库执行；销售管理目录 menu_id 实查为 2039
-- 幂等性：重复执行会重复插入，仅首次部署执行
-- =============================================

-- 1. 收银台菜单（排在销售开单之前，零售场景的主入口）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('前台收银', 2039, 0, 'cashier', 'jxc/cashier/index', 1, 0, 'C', '0', '0', 'jxc:cashier:list', 'skill', 'admin', sysdate(), '', NULL, '收银台菜单（扫码加购+找零结算）');
SET @m1 := LAST_INSERT_ID();

-- 2. 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('收银台查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:cashier:query', '#', 'admin', sysdate(), '', NULL, ''),
('收银结算', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:cashier:add', '#', 'admin', sysdate(), '', NULL, '');

-- 3. 授权 admin
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 4. 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @m1 ORDER BY menu_id;
