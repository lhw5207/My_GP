-- =============================================
-- 销售管理：目录 + 销售单菜单（POS） + 按钮权限 + 授权
-- 说明：在 ry_jxc 库执行
-- =============================================

-- 1. 一级目录：销售管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销售管理', 0, 2, 'sale', NULL, 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', NULL, '销售管理目录');
SET @dirId := LAST_INSERT_ID();

-- 2. 销售单菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销售开单(POS)', @dirId, 1, 'sale_order', 'jxc/sale_order/index', 1, 0, 'C', '0', '0', 'jxc:sale_order:list', 'shopping', 'admin', sysdate(), '', NULL, '销售单菜单');
SET @m1 := LAST_INSERT_ID();

-- 3. 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销售单查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_order:query', '#', 'admin', sysdate(), '', NULL, ''),
('销售开单', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_order:add', '#', 'admin', sysdate(), '', NULL, ''),
('销售单修改', @m1, 3, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_order:edit', '#', 'admin', sysdate(), '', NULL, ''),
('销售单删除', @m1, 4, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_order:remove', '#', 'admin', sysdate(), '', NULL, ''),
('销售单导出', @m1, 5, '#', '', 1, 0, 'F', '0', '0', 'jxc:sale_order:export', '#', 'admin', sysdate(), '', NULL, '');

-- 4. 授权 admin
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @dirId), (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 5. 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @dirId ORDER BY menu_id;
