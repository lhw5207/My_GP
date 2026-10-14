-- =============================================
-- 报表管理：目录 + 销售统计 + 库存预警 + 授权
-- 说明：在 ry_jxc 库执行
-- =============================================

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('报表管理', 0, 5, 'report', NULL, 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', NULL, '报表管理目录');
SET @dirId := LAST_INSERT_ID();

-- 销售统计
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('销售统计', @dirId, 1, 'sale_report', 'jxc/report/sale/index', 1, 0, 'C', '0', '0', 'jxc:report:sale', 'chart', 'admin', sysdate(), '', NULL, '销售日报（含毛利）与畅销排行');
SET @m1 := LAST_INSERT_ID();
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m1);

-- 库存预警
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('库存预警', @dirId, 2, 'warning_report', 'jxc/report/warning/index', 1, 0, 'C', '0', '0', 'jxc:report:stock', 'monitor', 'admin', sysdate(), '', NULL, '低库存与临期预警');
SET @m2 := LAST_INSERT_ID();
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @m2);

-- 授权目录
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @dirId);

-- 核验
SELECT menu_id, menu_name, parent_id, menu_type, path, component, perms FROM sys_menu WHERE menu_id >= @dirId ORDER BY menu_id;
