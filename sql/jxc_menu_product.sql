-- =============================================
-- 进销存管理：目录 + 商品信息菜单 + 按钮权限 + 角色授权
-- 说明：在 ry_jxc 库执行（RuoYi sys_menu / sys_role_menu）
-- =============================================

-- 1. 一级目录：进销存管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('进销存管理', 0, 0, 'jxc', NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', NULL, '进销存管理目录');
SET @jxcDirId := LAST_INSERT_ID();

-- 2. 商品信息菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品信息', @jxcDirId, 1, 'product', 'jxc/product/index', 1, 0, 'C', '0', '0', 'jxc:product:list', 'shopping', 'admin', sysdate(), '', NULL, '商品信息菜单');
SET @parentId := LAST_INSERT_ID();

-- 3. 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品信息查询', @parentId, '1', '#', '', 1, 0, 'F', '0', '0', 'jxc:product:query', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品信息新增', @parentId, '2', '#', '', 1, 0, 'F', '0', '0', 'jxc:product:add', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品信息修改', @parentId, '3', '#', '', 1, 0, 'F', '0', '0', 'jxc:product:edit', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品信息删除', @parentId, '4', '#', '', 1, 0, 'F', '0', '0', 'jxc:product:remove', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('商品信息导出', @parentId, '5', '#', '', 1, 0, 'F', '0', '0', 'jxc:product:export', '#', 'admin', sysdate(), '', NULL, '');

-- 4. 授权给超级管理员角色（role_id=1）：目录 + 菜单 + 全部按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @jxcDirId), (1, @parentId);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE parent_id = @parentId;

-- 5. 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @jxcDirId ORDER BY menu_id;
