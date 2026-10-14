-- =============================================
-- 库存管理：库存盘点菜单 + 按钮权限 + 授权
-- 说明：在 ry_jxc 库执行；父目录"库存管理"ID=2046
-- =============================================

SET @dirId = (SELECT menu_id FROM sys_menu WHERE menu_name='库存管理' AND parent_id=0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('库存盘点', @dirId, 3, 'stock_check', 'jxc/stock_check/index', 1, 0, 'C', '0', '0', 'jxc:stock_check:list', 'checkbox', 'admin', sysdate(), '', NULL, '库存盘点菜单');
SET @m1 := LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('盘点单查询', @m1, 1, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_check:query', '#', 'admin', sysdate(), '', NULL, ''),
('盘点单新增', @m1, 2, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_check:add', '#', 'admin', sysdate(), '', NULL, ''),
('盘点单修改', @m1, 3, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_check:edit', '#', 'admin', sysdate(), '', NULL, ''),
('盘点单删除', @m1, 4, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_check:remove', '#', 'admin', sysdate(), '', NULL, ''),
('盘点单导出', @m1, 5, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_check:export', '#', 'admin', sysdate(), '', NULL, ''),
('盘点单审核', @m1, 6, '#', '', 1, 0, 'F', '0', '0', 'jxc:stock_check:audit', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @m1;

-- 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @m1 ORDER BY menu_id;
