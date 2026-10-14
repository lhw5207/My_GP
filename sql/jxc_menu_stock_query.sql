-- =============================================
-- 库存管理：目录 + 库存查询 + 库存流水台账 + 按钮权限 + 授权
-- 说明：在 ry_jxc 库执行
-- =============================================

-- 1. 一级目录：库存管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('库存管理', 0, 3, 'stock', NULL, 1, 0, 'M', '0', '0', '', 'tree-table', 'admin', sysdate(), '', NULL, '库存管理目录');
SET @dirId := LAST_INSERT_ID();

-- 2. 库存查询菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('库存查询', @dirId, 1, 'stock_query', 'jxc/stock/index', 1, 0, 'C', '0', '0', 'jxc:stock:list', 'search', 'admin', sysdate(), '', NULL, '现货存量查询');
SET @m1 := LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('库存查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 3. 库存流水台账菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('库存流水', @dirId, 2, 'stock_flow', 'jxc/stock_flow/index', 1, 0, 'C', '0', '0', 'jxc:stock_flow:list', 'log', 'admin', sysdate(), '', NULL, '库存流水台账查询');
SET @m2 := LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('流水查询', @m2, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_flow:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m2);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m2;

-- 4. 授权目录
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @dirId);

-- 5. 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @dirId ORDER BY menu_id;
