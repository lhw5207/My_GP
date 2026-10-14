-- =============================================
-- 库存管理：期初数据导入菜单 + 授权
-- 说明：在 ry_jxc 库执行；父目录"库存管理"ID=2046
-- =============================================

SET @dirId = (SELECT menu_id FROM sys_menu WHERE menu_name='库存管理' AND parent_id=0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('期初导入', @dirId, 4, 'init', 'jxc/init/index', 1, 0, 'C', '0', '0', 'jxc:init:list', 'upload', 'admin', sysdate(), '', NULL, '期初数据导入（数据迁移）');
SET @m1 := LAST_INSERT_ID();

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);

-- 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @m1 ORDER BY menu_id;
