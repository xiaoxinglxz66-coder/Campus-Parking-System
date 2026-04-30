/*
 Navicat Premium Dump SQL

 Source Server         : admin
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : campus_parking_db

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 06/01/2026 10:48:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for parking_lot_spots
-- ----------------------------
DROP TABLE IF EXISTS `parking_lot_spots`;
CREATE TABLE `parking_lot_spots`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parking_lot_id` bigint NOT NULL COMMENT '停车场ID',
  `parking_spot_id` bigint NOT NULL COMMENT '停车位ID',
  `assigned_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `assigned_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分配人',
  `notes` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spot_lot`(`parking_spot_id` ASC) USING BTREE COMMENT '一个车位只属于一个停车场',
  UNIQUE INDEX `uk_lot_spot_number`(`parking_lot_id` ASC, `parking_spot_id` ASC) USING BTREE COMMENT '组合唯一索引',
  INDEX `idx_lot_id`(`parking_lot_id` ASC) USING BTREE,
  INDEX `idx_spot_id`(`parking_spot_id` ASC) USING BTREE,
  INDEX `idx_assigned_time`(`assigned_time` ASC) USING BTREE,
  CONSTRAINT `fk_parking_lot_spots_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lots` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_parking_lot_spots_spot` FOREIGN KEY (`parking_spot_id`) REFERENCES `parking_spots_old` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 145 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车场与停车位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of parking_lot_spots
-- ----------------------------
INSERT INTO `parking_lot_spots` VALUES (1, 1, 163, '2026-01-03 13:52:38', 'SYSTEM', '北门主入口车位');
INSERT INTO `parking_lot_spots` VALUES (2, 1, 164, '2026-01-03 13:52:38', 'SYSTEM', '北门主入口车位');
INSERT INTO `parking_lot_spots` VALUES (3, 1, 165, '2026-01-03 13:52:38', 'SYSTEM', '北门主入口车位');
INSERT INTO `parking_lot_spots` VALUES (4, 1, 166, '2026-01-03 13:52:38', 'SYSTEM', '北门残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (5, 2, 133, '2026-01-03 13:52:38', 'SYSTEM', '南门主入口车位');
INSERT INTO `parking_lot_spots` VALUES (6, 2, 134, '2026-01-03 13:52:38', 'SYSTEM', '南门主入口车位');
INSERT INTO `parking_lot_spots` VALUES (7, 2, 135, '2026-01-03 13:52:38', 'SYSTEM', '南门残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (8, 3, 113, '2026-01-03 13:52:38', 'SYSTEM', '图书馆主车位');
INSERT INTO `parking_lot_spots` VALUES (9, 3, 114, '2026-01-03 13:52:38', 'SYSTEM', '图书馆车位');
INSERT INTO `parking_lot_spots` VALUES (10, 3, 115, '2026-01-03 13:52:38', 'SYSTEM', '图书馆残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (11, 3, 116, '2026-01-03 13:52:38', 'SYSTEM', '图书馆车位');
INSERT INTO `parking_lot_spots` VALUES (12, 3, 117, '2026-01-03 13:52:38', 'SYSTEM', '图书馆车位');
INSERT INTO `parking_lot_spots` VALUES (13, 4, 150, '2026-01-03 13:52:38', 'SYSTEM', '三食堂车位');
INSERT INTO `parking_lot_spots` VALUES (14, 4, 151, '2026-01-03 13:52:38', 'SYSTEM', '三食堂车位');
INSERT INTO `parking_lot_spots` VALUES (15, 4, 152, '2026-01-03 13:52:38', 'SYSTEM', '三食堂车位');
INSERT INTO `parking_lot_spots` VALUES (16, 4, 154, '2026-01-03 13:52:38', 'SYSTEM', '三食堂残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (17, 4, 155, '2026-01-03 13:52:38', 'SYSTEM', '三食堂车位');
INSERT INTO `parking_lot_spots` VALUES (18, 5, 136, '2026-01-03 13:52:38', 'SYSTEM', '一食堂车位');
INSERT INTO `parking_lot_spots` VALUES (19, 5, 137, '2026-01-03 13:52:38', 'SYSTEM', '一食堂车位');
INSERT INTO `parking_lot_spots` VALUES (20, 5, 138, '2026-01-03 13:52:38', 'SYSTEM', '一食堂车位');
INSERT INTO `parking_lot_spots` VALUES (21, 5, 139, '2026-01-03 13:52:38', 'SYSTEM', '一食堂车位');
INSERT INTO `parking_lot_spots` VALUES (22, 5, 140, '2026-01-03 13:52:38', 'SYSTEM', '一食堂残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (23, 5, 141, '2026-01-03 13:52:38', 'SYSTEM', '一食堂车位');
INSERT INTO `parking_lot_spots` VALUES (24, 5, 142, '2026-01-03 13:52:38', 'SYSTEM', '一食堂维修车位');
INSERT INTO `parking_lot_spots` VALUES (25, 5, 143, '2026-01-03 13:52:38', 'SYSTEM', '一食堂车位');
INSERT INTO `parking_lot_spots` VALUES (26, 6, 144, '2026-01-03 13:52:38', 'SYSTEM', '二食堂车位');
INSERT INTO `parking_lot_spots` VALUES (27, 6, 145, '2026-01-03 13:52:38', 'SYSTEM', '二食堂车位');
INSERT INTO `parking_lot_spots` VALUES (28, 6, 146, '2026-01-03 13:52:38', 'SYSTEM', '二食堂车位');
INSERT INTO `parking_lot_spots` VALUES (29, 6, 147, '2026-01-03 13:52:38', 'SYSTEM', '二食堂残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (30, 6, 148, '2026-01-03 13:52:38', 'SYSTEM', '二食堂车位');
INSERT INTO `parking_lot_spots` VALUES (31, 6, 149, '2026-01-03 13:52:38', 'SYSTEM', '二食堂车位');
INSERT INTO `parking_lot_spots` VALUES (32, 7, 156, '2026-01-03 13:52:38', 'SYSTEM', '美食街车位');
INSERT INTO `parking_lot_spots` VALUES (33, 7, 157, '2026-01-03 13:52:38', 'SYSTEM', '美食街车位');
INSERT INTO `parking_lot_spots` VALUES (34, 7, 158, '2026-01-03 13:52:38', 'SYSTEM', '美食街车位');
INSERT INTO `parking_lot_spots` VALUES (35, 7, 159, '2026-01-03 13:52:38', 'SYSTEM', '美食街车位');
INSERT INTO `parking_lot_spots` VALUES (36, 7, 160, '2026-01-03 13:52:38', 'SYSTEM', '美食街车位');
INSERT INTO `parking_lot_spots` VALUES (37, 7, 161, '2026-01-03 13:52:38', 'SYSTEM', '美食街残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (38, 7, 162, '2026-01-03 13:52:38', 'SYSTEM', '美食街车位');
INSERT INTO `parking_lot_spots` VALUES (39, 8, 167, '2026-01-03 13:52:38', 'SYSTEM', '活动中心车位');
INSERT INTO `parking_lot_spots` VALUES (40, 8, 168, '2026-01-03 13:52:38', 'SYSTEM', '活动中心车位');
INSERT INTO `parking_lot_spots` VALUES (41, 8, 170, '2026-01-03 13:52:38', 'SYSTEM', '活动中心残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (42, 8, 171, '2026-01-03 13:52:38', 'SYSTEM', '活动中心车位');
INSERT INTO `parking_lot_spots` VALUES (43, 8, 172, '2026-01-03 13:52:38', 'SYSTEM', '活动中心车位');
INSERT INTO `parking_lot_spots` VALUES (44, 8, 173, '2026-01-03 13:52:38', 'SYSTEM', '活动中心车位');
INSERT INTO `parking_lot_spots` VALUES (45, 8, 174, '2026-01-03 13:52:38', 'SYSTEM', '活动中心维修车位');
INSERT INTO `parking_lot_spots` VALUES (46, 9, 181, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心车位');
INSERT INTO `parking_lot_spots` VALUES (47, 9, 182, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心车位');
INSERT INTO `parking_lot_spots` VALUES (48, 9, 183, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心车位');
INSERT INTO `parking_lot_spots` VALUES (49, 9, 184, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心车位');
INSERT INTO `parking_lot_spots` VALUES (50, 9, 185, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心车位');
INSERT INTO `parking_lot_spots` VALUES (51, 9, 186, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (52, 9, 187, '2026-01-03 13:52:38', 'SYSTEM', '演艺中心车位');
INSERT INTO `parking_lot_spots` VALUES (53, 10, 188, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (54, 10, 189, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (55, 10, 190, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (56, 10, 191, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (57, 10, 192, '2026-01-03 13:52:38', 'SYSTEM', '体育馆残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (58, 10, 193, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (59, 10, 194, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (60, 10, 195, '2026-01-03 13:52:38', 'SYSTEM', '体育馆车位');
INSERT INTO `parking_lot_spots` VALUES (61, 11, 196, '2026-01-03 13:52:38', 'SYSTEM', '训练馆车位');
INSERT INTO `parking_lot_spots` VALUES (62, 11, 197, '2026-01-03 13:52:38', 'SYSTEM', '训练馆车位');
INSERT INTO `parking_lot_spots` VALUES (63, 11, 198, '2026-01-03 13:52:38', 'SYSTEM', '训练馆车位');
INSERT INTO `parking_lot_spots` VALUES (64, 11, 199, '2026-01-03 13:52:38', 'SYSTEM', '训练馆车位');
INSERT INTO `parking_lot_spots` VALUES (65, 12, 122, '2026-01-03 13:52:38', 'SYSTEM', '科大讯飞楼车位');
INSERT INTO `parking_lot_spots` VALUES (66, 12, 123, '2026-01-03 13:52:38', 'SYSTEM', '科大讯飞楼车位');
INSERT INTO `parking_lot_spots` VALUES (67, 12, 124, '2026-01-03 13:52:38', 'SYSTEM', '科大讯飞楼车位');
INSERT INTO `parking_lot_spots` VALUES (68, 12, 125, '2026-01-03 13:52:38', 'SYSTEM', '科大讯飞楼车位');
INSERT INTO `parking_lot_spots` VALUES (69, 12, 126, '2026-01-03 13:52:38', 'SYSTEM', '科大讯飞楼车位');
INSERT INTO `parking_lot_spots` VALUES (70, 12, 127, '2026-01-03 13:52:38', 'SYSTEM', '科大讯飞楼残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (71, 13, 175, '2026-01-03 13:52:38', 'SYSTEM', '创业园车位');
INSERT INTO `parking_lot_spots` VALUES (72, 13, 176, '2026-01-03 13:52:38', 'SYSTEM', '创业园车位');
INSERT INTO `parking_lot_spots` VALUES (73, 13, 177, '2026-01-03 13:52:38', 'SYSTEM', '创业园车位');
INSERT INTO `parking_lot_spots` VALUES (74, 13, 178, '2026-01-03 13:52:38', 'SYSTEM', '创业园车位');
INSERT INTO `parking_lot_spots` VALUES (75, 13, 179, '2026-01-03 13:52:38', 'SYSTEM', '创业园残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (76, 13, 180, '2026-01-03 13:52:38', 'SYSTEM', '创业园车位');
INSERT INTO `parking_lot_spots` VALUES (77, 14, 128, '2026-01-03 13:52:38', 'SYSTEM', '南硅谷车位');
INSERT INTO `parking_lot_spots` VALUES (78, 14, 129, '2026-01-03 13:52:38', 'SYSTEM', '南硅谷车位');
INSERT INTO `parking_lot_spots` VALUES (79, 14, 130, '2026-01-03 13:52:38', 'SYSTEM', '南硅谷车位');
INSERT INTO `parking_lot_spots` VALUES (80, 14, 131, '2026-01-03 13:52:38', 'SYSTEM', '南硅谷车位');
INSERT INTO `parking_lot_spots` VALUES (81, 14, 132, '2026-01-03 13:52:38', 'SYSTEM', '南硅谷车位');
INSERT INTO `parking_lot_spots` VALUES (82, 15, 118, '2026-01-03 13:52:38', 'SYSTEM', '新闻科大楼车位');
INSERT INTO `parking_lot_spots` VALUES (83, 15, 119, '2026-01-03 13:52:38', 'SYSTEM', '新闻科大楼车位');
INSERT INTO `parking_lot_spots` VALUES (84, 15, 120, '2026-01-03 13:52:38', 'SYSTEM', '新闻科大楼车位');
INSERT INTO `parking_lot_spots` VALUES (85, 15, 121, '2026-01-03 13:52:38', 'SYSTEM', '新闻科大楼维修车位');
INSERT INTO `parking_lot_spots` VALUES (86, 16, 201, '2026-01-03 13:52:38', 'SYSTEM', '万科楼车位');
INSERT INTO `parking_lot_spots` VALUES (87, 16, 202, '2026-01-03 13:52:38', 'SYSTEM', '万科楼车位');
INSERT INTO `parking_lot_spots` VALUES (88, 16, 203, '2026-01-03 13:52:38', 'SYSTEM', '万科楼车位');
INSERT INTO `parking_lot_spots` VALUES (89, 16, 204, '2026-01-03 13:52:38', 'SYSTEM', '万科楼车位');
INSERT INTO `parking_lot_spots` VALUES (90, 16, 205, '2026-01-03 13:52:38', 'SYSTEM', '万科楼残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (91, 16, 206, '2026-01-03 13:52:38', 'SYSTEM', '万科楼车位');
INSERT INTO `parking_lot_spots` VALUES (92, 17, 1, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (93, 17, 2, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (94, 17, 3, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (95, 17, 208, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (96, 17, 211, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (97, 17, 212, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (98, 17, 215, '2026-01-03 13:52:38', 'SYSTEM', 'A区车位');
INSERT INTO `parking_lot_spots` VALUES (99, 18, 4, '2026-01-03 13:52:38', 'SYSTEM', 'B区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (100, 18, 5, '2026-01-03 13:52:38', 'SYSTEM', 'B区预留车位');
INSERT INTO `parking_lot_spots` VALUES (101, 19, 73, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (102, 19, 74, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (103, 19, 75, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (104, 19, 76, '2026-01-03 13:52:38', 'SYSTEM', 'C区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (105, 19, 77, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (106, 19, 78, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (107, 19, 79, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (108, 19, 80, '2026-01-03 13:52:38', 'SYSTEM', 'C区维修车位');
INSERT INTO `parking_lot_spots` VALUES (109, 19, 81, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (110, 19, 82, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (111, 19, 83, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (112, 19, 84, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (113, 19, 85, '2026-01-03 13:52:38', 'SYSTEM', 'C区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (114, 19, 86, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (115, 19, 87, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (116, 19, 88, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (117, 19, 89, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (118, 19, 90, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (119, 19, 91, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (120, 19, 92, '2026-01-03 13:52:38', 'SYSTEM', 'C区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (121, 19, 93, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (122, 19, 94, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (123, 19, 95, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (124, 19, 96, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (125, 19, 97, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (126, 19, 98, '2026-01-03 13:52:38', 'SYSTEM', 'C区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (127, 19, 99, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (128, 19, 100, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (129, 19, 101, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (130, 19, 102, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (131, 19, 103, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (132, 19, 104, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (133, 19, 105, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (134, 19, 106, '2026-01-03 13:52:38', 'SYSTEM', 'C区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (135, 19, 107, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (136, 19, 108, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (137, 19, 109, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (138, 19, 110, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (139, 19, 111, '2026-01-03 13:52:38', 'SYSTEM', 'C区残疾人车位');
INSERT INTO `parking_lot_spots` VALUES (140, 19, 112, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (141, 19, 209, '2026-01-03 13:52:38', 'SYSTEM', 'C区车位');
INSERT INTO `parking_lot_spots` VALUES (142, 20, 7, '2026-01-03 13:52:38', 'SYSTEM', '学生活动中心车位');
INSERT INTO `parking_lot_spots` VALUES (143, 21, 32, '2026-01-03 13:52:38', 'SYSTEM', '测试车位');
INSERT INTO `parking_lot_spots` VALUES (144, 22, 207, '2026-01-03 13:52:38', 'SYSTEM', '测试停车场车位');

-- ----------------------------
-- Table structure for parking_lots
-- ----------------------------
DROP TABLE IF EXISTS `parking_lots`;
CREATE TABLE `parking_lots`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lot_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '停车场编号',
  `lot_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '停车场名称',
  `original_zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原始区域（从parking_spots.zone迁移）',
  `area_type` enum('GATE','LIBRARY','DINING','SPORTS','ACADEMIC','RESIDENTIAL','COMMERCIAL','GENERAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL' COMMENT '区域类型',
  `status` enum('ACTIVE','INACTIVE','MAINTENANCE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '停车场状态',
  `total_spots` int NOT NULL DEFAULT 0 COMMENT '总车位数',
  `available_spots` int NOT NULL DEFAULT 0 COMMENT '可用车位数',
  `occupied_spots` int NOT NULL DEFAULT 0 COMMENT '占用车位数',
  `maintenance_spots` int NOT NULL DEFAULT 0 COMMENT '维护中车位数',
  `hourly_rate_standard` decimal(38, 2) NULL DEFAULT 5.00 COMMENT '标准小时费率',
  `location_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置描述',
  `opening_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '06:00-22:00' COMMENT '开放时间',
  `is_24_hours` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否24小时开放',
  `allow_external_users` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许校外用户',
  `priority_level` int NOT NULL DEFAULT 3 COMMENT '优先级(1-高,2-中,3-低)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lot_number`(`lot_number` ASC) USING BTREE COMMENT '停车场编号唯一',
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_area_type`(`area_type` ASC) USING BTREE,
  INDEX `idx_available_spots`(`available_spots` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of parking_lots
-- ----------------------------
INSERT INTO `parking_lots` VALUES (1, 'GATE-NORTH', '北门停车场', '北门', 'GATE', 'ACTIVE', 4, 3, 1, 0, 8.00, '校园北门主入口，交通便利，靠近主干道', '全天开放', 1, 1, 1, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (2, 'GATE-SOUTH', '南门停车场', '南门', 'GATE', 'ACTIVE', 3, 3, 0, 0, 8.00, '校园南门主入口，外来车辆主要停放区', '全天开放', 1, 1, 1, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (3, 'LIB-MAIN', '图书馆主停车场', '图书馆', 'LIBRARY', 'ACTIVE', 5, 4, 1, 0, 5.00, '图书馆地下停车场，安静便利，适合长时间学习停车', '06:00-22:00', 0, 1, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (4, 'DINE-3', '三食堂停车场', '三食堂', 'DINING', 'ACTIVE', 5, 3, 1, 1, 4.00, '三食堂周边停车场，用餐高峰期繁忙', '06:30-21:30', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (5, 'DINE-1', '一食堂停车场', '一食堂', 'DINING', 'ACTIVE', 8, 6, 1, 1, 4.00, '一食堂南侧停车场，靠近学生宿舍区', '06:00-22:00', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (6, 'DINE-2', '二食堂停车场', '二食堂', 'DINING', 'ACTIVE', 6, 5, 1, 0, 4.00, '二食堂东侧露天停车场', '06:30-21:00', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (7, 'FOOD-STREET', '美食街停车场', '美食街', 'COMMERCIAL', 'ACTIVE', 7, 6, 1, 0, 6.00, '校园美食街旁停车场，晚间和周末繁忙', '08:00-23:00', 0, 1, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (8, 'ACT-CENTER', '学生活动中心停车场', '活动中心', 'GENERAL', 'ACTIVE', 8, 6, 0, 2, 5.00, '学生活动中心地下及周边停车场', '07:00-22:00', 0, 1, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (9, 'PERF-CENTER', '演艺中心停车场', '演艺中心', 'GENERAL', 'ACTIVE', 7, 6, 1, 0, 6.00, '演艺中心广场停车场，演出期间繁忙', '08:00-22:30', 0, 1, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (10, 'GYM-MAIN', '体育馆主停车场', '体育馆', 'SPORTS', 'ACTIVE', 8, 7, 1, 0, 5.00, '体育馆东侧大型停车场，可停放大巴车', '06:00-22:00', 0, 1, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (11, 'GYM-TRAIN', '训练馆停车场', '训练馆', 'SPORTS', 'ACTIVE', 4, 2, 2, 0, 4.00, '训练馆专用停车场，主要为教职工使用', '07:00-20:00', 0, 0, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (12, 'ACAD-IFLY', '科大讯飞楼停车场', '科大讯飞楼', 'ACADEMIC', 'ACTIVE', 7, 6, 1, 0, 5.00, '科大讯飞楼周边停车场，科研人员专用', '07:30-21:00', 0, 0, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (13, 'ACAD-START', '创业园停车场', '大学生创业园', 'ACADEMIC', 'ACTIVE', 6, 5, 1, 0, 4.00, '大学生创业园内停车场，创业团队专用', '07:00-21:00', 0, 0, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (14, 'ACAD-NG', '南硅谷停车场', '南硅谷', 'ACADEMIC', 'ACTIVE', 5, 4, 1, 0, 5.00, '南硅谷科研区停车场，高新技术企业专用', '07:00-20:00', 0, 0, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (15, 'ACAD-NEWS', '新闻科大楼停车场', '新闻科大楼', 'ACADEMIC', 'ACTIVE', 4, 3, 0, 1, 5.00, '新闻科大楼前广场停车场', '08:00-18:00', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (16, 'ACAD-WANKE', '万科楼停车场', '万科楼', 'ACADEMIC', 'ACTIVE', 6, 5, 1, 0, 5.00, '万科楼建筑学院专用停车场', '07:30-20:30', 0, 0, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (17, 'ZONE-A', 'A区综合停车场', 'A区', 'GENERAL', 'ACTIVE', 6, 5, 0, 1, 5.00, '校园A区综合停车场，覆盖多个教学楼', '06:00-22:00', 0, 1, 2, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (18, 'ZONE-B', 'B区停车场', 'B区', 'GENERAL', 'ACTIVE', 2, 2, 0, 0, 4.00, 'B区小型停车场，包含残疾人车位', '07:00-21:00', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (19, 'ZONE-C', 'C区大型停车场', 'C区', 'GENERAL', 'ACTIVE', 41, 34, 6, 1, 5.00, 'C区大型综合停车场，车位最多，分布广泛', '06:00-23:00', 0, 1, 1, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (20, 'ZONE-D', 'D区停车场', '学生活动中心', 'GENERAL', 'ACTIVE', 1, 1, 0, 0, 5.00, '学生活动中心D区停车位', '07:00-22:00', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (21, 'GEN-UNSPEC', '未指定区域停车场', '未指定', 'GENERAL', 'ACTIVE', 1, 1, 0, 0, 5.00, '位置未明确指定的停车位', '06:00-22:00', 0, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');
INSERT INTO `parking_lots` VALUES (22, 'TEST-AREA', '测试停车场', 'A', 'GENERAL', 'ACTIVE', 0, 0, 0, 0, 5.00, '系统测试用停车场', '全天开放', 1, 1, 3, '2026-01-03 13:48:22', '2026-01-03 14:26:06');

-- ----------------------------
-- Table structure for parking_records
-- ----------------------------
DROP TABLE IF EXISTS `parking_records`;
CREATE TABLE `parking_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NULL DEFAULT NULL,
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT NULL,
  `total_fee` decimal(38, 2) NULL DEFAULT NULL,
  `status` enum('PARKING','COMPLETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PARKING',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `need_payment` bit(1) NULL DEFAULT NULL,
  `user_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `fee` decimal(10, 2) NULL DEFAULT NULL,
  `updated_time` datetime(6) NULL DEFAULT NULL,
  `parking_spot_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `parking_lot_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `plate_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `out_trade_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `paid_time` datetime(6) NULL DEFAULT NULL,
  `payment_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `trade_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_vehicle_id`(`vehicle_id` ASC) USING BTREE,
  INDEX `idx_parking_spot_id`(`parking_spot_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_plate_number`(`plate_number` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE,
  INDEX `idx_payment_status`(`payment_status` ASC) USING BTREE,
  CONSTRAINT `FKciw6gqi88qcpaf5wejyqwt7g7` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKr8hrr6tpfsujgllyym82isxq1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parking_records
-- ----------------------------
INSERT INTO `parking_records` VALUES (1, 39, '2025-12-07 16:30:25', '2025-12-07 16:30:43', 0.00, 'COMPLETED', '2025-12-07 16:30:25', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12124', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (2, 40, '2025-12-07 16:32:43', '2025-12-07 16:32:54', 0.00, 'COMPLETED', '2025-12-07 16:32:43', b'0', 'STUDENT', 0.00, '2025-12-25 17:28:46.957149', 7, 11, '学生活动中心', '京A123456', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (3, 33, '2025-12-07 16:43:54', '2025-12-07 16:44:29', 0.00, 'COMPLETED', '2025-12-07 16:43:54', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (4, 33, '2025-12-07 16:59:17', '2025-12-07 16:59:37', 0.00, 'COMPLETED', '2025-12-07 16:59:17', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (5, 33, '2025-12-07 16:59:43', '2025-12-07 17:01:13', 0.00, 'COMPLETED', '2025-12-07 16:59:43', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (6, 33, '2025-12-07 17:01:23', '2025-12-07 17:01:29', 0.00, 'COMPLETED', '2025-12-07 17:01:23', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (7, 33, '2025-12-07 19:15:43', '2025-12-07 19:15:46', 0.00, 'COMPLETED', '2025-12-07 19:15:43', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (8, 33, '2025-12-09 15:21:03', '2025-12-09 15:21:05', 0.00, 'COMPLETED', '2025-12-09 15:21:03', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (9, 33, '2025-12-09 16:31:18', '2025-12-09 16:31:22', 0.00, 'COMPLETED', '2025-12-09 16:31:18', b'0', 'STUDENT', 0.00, '2026-01-01 17:10:05.304342', 7, 11, '学生活动中心', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (10, 75, '2025-12-24 13:31:53', '2025-12-24 13:32:08', 0.00, 'COMPLETED', '2025-12-24 13:31:53', b'0', 'STAFF', 0.00, '2026-01-01 17:10:02.196057', 7, 13, '学生活动中心', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (11, 75, '2025-12-24 13:32:24', '2025-12-24 13:33:07', 0.00, 'COMPLETED', '2025-12-24 13:32:24', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 113, 13, '图书馆', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (12, 75, '2025-12-24 13:33:22', '2025-12-24 13:41:40', 0.00, 'COMPLETED', '2025-12-24 13:33:22', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 3, 13, 'A区', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (13, 75, '2025-12-24 13:41:49', '2025-12-24 13:41:58', 0.00, 'COMPLETED', '2025-12-24 13:41:49', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 122, 13, 'A', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (14, 75, '2025-12-24 15:24:12', '2025-12-24 15:24:15', 0.00, 'COMPLETED', '2025-12-24 15:24:12', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 134, 13, '南门', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (15, 75, '2025-12-24 15:25:29', '2025-12-24 15:26:01', 0.00, 'COMPLETED', '2025-12-24 15:25:29', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 134, 13, '南门', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (16, 75, '2025-12-24 15:26:11', '2025-12-24 15:26:13', 0.00, 'COMPLETED', '2025-12-24 15:26:11', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 122, 13, 'A', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (17, 75, '2025-12-24 15:26:25', '2025-12-24 15:26:27', 0.00, 'COMPLETED', '2025-12-24 15:26:25', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 217, 13, '创业园', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (18, 75, '2025-12-25 15:25:37', '2025-12-25 15:28:34', 0.00, 'COMPLETED', '2025-12-25 15:25:37', b'0', 'STAFF', 0.00, '2026-01-01 17:16:31.517653', 217, 13, '创业园', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (19, NULL, '2025-12-25 23:51:11', '2025-12-25 23:51:16', 5.00, 'COMPLETED', '2025-12-25 23:51:11', b'1', 'EXTERNAL_USER', 5.00, '2026-01-01 17:16:33.638449', 7, 23, '学生活动中心', '琼A12121_T1233', 'guest_18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (20, NULL, '2026-01-01 17:15:05', '2026-01-01 17:15:12', 5.00, 'COMPLETED', '2026-01-01 17:15:05', b'1', 'EXTERNAL_USER', 5.00, '2026-01-03 17:40:09.958764', 78, 25, 'C', '琼A12121_T4883', '19825376288', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (21, NULL, '2026-01-01 17:25:42', '2026-01-01 17:25:45', 5.00, 'COMPLETED', '2026-01-01 17:25:42', b'1', 'EXTERNAL_USER', 5.00, '2026-01-04 18:04:56.917291', 7, 23, '学生活动中心', '京A66666', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (22, NULL, '2026-01-01 17:25:55', '2026-01-01 17:25:59', 5.00, 'COMPLETED', '2026-01-01 17:25:55', b'1', 'EXTERNAL_USER', 5.00, '2026-01-04 18:04:56.917291', 141, 23, '一食堂', '桂A12120', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (23, NULL, '2026-01-01 17:26:12', '2026-01-01 17:26:16', 5.00, 'COMPLETED', '2026-01-01 17:26:12', b'1', 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.093452', 175, 23, '大学生创业园', '贵A12120', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (24, NULL, '2026-01-01 23:30:00', '2026-01-01 23:30:04', 5.00, 'COMPLETED', '2026-01-01 23:30:00', b'1', 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.093452', 82, 23, 'C', '京A66666', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (25, NULL, '2026-01-01 23:31:06', '2026-01-01 23:31:19', 5.00, 'COMPLETED', '2026-01-01 23:31:06', b'1', 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.093452', 115, 23, '图书馆', '京A66666', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (26, NULL, '2025-01-05 09:30:00', '2025-01-05 14:45:00', 26.25, 'COMPLETED', '2025-01-05 09:30:00', b'1', 'EXTERNAL_USER', 26.25, '2025-01-05 14:45:00.000000', 7, 23, '学生活动中心', '粤A88888_T001', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (27, NULL, '2025-01-08 11:15:00', '2025-01-08 16:30:00', 26.25, 'COMPLETED', '2025-01-08 11:15:00', b'1', 'EXTERNAL_USER', 26.25, '2025-01-08 16:30:00.000000', 58, 23, '图书馆', '沪B66666_T002', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (28, NULL, '2025-01-12 08:45:00', '2025-01-12 12:20:00', 17.50, 'COMPLETED', '2025-01-12 08:45:00', b'1', 'EXTERNAL_USER', 17.50, '2025-01-12 12:20:00.000000', 51, 23, '北门', '京C77777_T003', 'guest_18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (29, NULL, '2025-01-15 14:20:00', '2025-01-15 18:40:00', 22.00, 'COMPLETED', '2025-01-15 14:20:00', b'1', 'EXTERNAL_USER', 22.00, '2025-01-15 18:40:00.000000', 82, 25, '美食街', '浙D88888_T004', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (30, NULL, '2025-02-03 10:45:00', '2025-02-03 15:30:00', 23.75, 'COMPLETED', '2025-02-03 10:45:00', b'1', 'EXTERNAL_USER', 23.75, '2025-02-03 15:30:00.000000', 141, 23, '一食堂', '粤A88888_T005', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (31, NULL, '2025-02-07 08:30:00', '2025-02-07 12:15:00', 18.75, 'COMPLETED', '2025-02-07 08:30:00', b'1', 'EXTERNAL_USER', 18.75, '2025-02-07 12:15:00.000000', 138, 23, '万科楼', '沪B66666_T006', 'guest_18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (32, NULL, '2025-02-11 14:00:00', '2025-02-11 16:45:00', 13.50, 'COMPLETED', '2025-02-11 14:00:00', b'1', 'EXTERNAL_USER', 13.50, '2025-02-11 16:45:00.000000', 129, 25, '南硅谷', '京C77777_T007', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (33, NULL, '2025-02-15 09:15:00', '2025-02-15 17:45:00', 42.50, 'COMPLETED', '2025-02-15 09:15:00', b'1', 'EXTERNAL_USER', 42.50, '2025-02-15 17:45:00.000000', 116, 25, '科大讯飞楼', '浙D88888_T008', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (34, NULL, '2025-03-02 08:45:00', '2025-03-02 14:20:00', 27.50, 'COMPLETED', '2025-03-02 08:45:00', b'1', 'EXTERNAL_USER', 27.50, '2025-03-02 14:20:00.000000', 76, 23, '二食堂', '粤A88888_T009', 'guest_18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (35, NULL, '2025-03-06 12:30:00', '2025-03-06 16:45:00', 21.00, 'COMPLETED', '2025-03-06 12:30:00', b'1', 'EXTERNAL_USER', 21.00, '2025-03-06 16:45:00.000000', 107, 23, '体育馆', '沪B66666_T010', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (36, NULL, '2025-03-10 09:00:00', '2025-03-10 18:30:00', 47.50, 'COMPLETED', '2025-03-10 09:00:00', b'1', 'EXTERNAL_USER', 47.50, '2025-03-10 18:30:00.000000', 122, 25, 'A', '京C77777_T011', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (37, NULL, '2025-03-14 14:15:00', '2025-03-14 17:40:00', 16.25, 'COMPLETED', '2025-03-14 14:15:00', b'1', 'EXTERNAL_USER', 16.25, '2025-03-14 17:40:00.000000', 134, 25, '南门', '浙D88888_T012', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (38, NULL, '2025-04-02 09:00:00', '2025-04-02 13:45:00', 23.75, 'COMPLETED', '2025-04-02 09:00:00', b'1', 'EXTERNAL_USER', 23.75, '2025-04-02 13:45:00.000000', 51, 23, '北门', '粤A88888_T013', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (39, NULL, '2025-04-06 11:15:00', '2025-04-06 15:30:00', 21.00, 'COMPLETED', '2025-04-06 11:15:00', b'1', 'EXTERNAL_USER', 21.00, '2025-04-06 15:30:00.000000', 78, 23, 'C', '沪B66666_T014', 'guest_18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (40, NULL, '2025-04-10 08:30:00', '2025-04-10 12:15:00', 18.75, 'COMPLETED', '2025-04-10 08:30:00', b'1', 'EXTERNAL_USER', 18.75, '2025-04-10 12:15:00.000000', 82, 25, '美食街', '京C77777_T015', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (41, NULL, '2025-04-14 14:00:00', '2025-04-14 18:30:00', 22.50, 'COMPLETED', '2025-04-14 14:00:00', b'1', 'EXTERNAL_USER', 22.50, '2025-04-14 18:30:00.000000', 97, 25, '演艺中心', '浙D88888_T016', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (42, NULL, '2025-05-03 09:30:00', '2025-05-03 14:45:00', 26.25, 'COMPLETED', '2025-05-03 09:30:00', b'1', 'EXTERNAL_USER', 26.25, '2025-05-03 14:45:00.000000', 138, 23, '万科楼', '粤A88888_T017', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (43, NULL, '2025-05-07 11:00:00', '2025-05-07 16:30:00', 27.50, 'COMPLETED', '2025-05-07 11:00:00', b'1', 'EXTERNAL_USER', 27.50, '2025-05-07 16:30:00.000000', 141, 23, '一食堂', '沪B66666_T018', 'guest_18260916208', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (44, NULL, '2025-05-11 08:45:00', '2025-05-11 18:15:00', 47.50, 'COMPLETED', '2025-05-11 08:45:00', b'1', 'EXTERNAL_USER', 47.50, '2025-05-11 18:15:00.000000', 175, 25, '大学生创业园', '京C77777_T019', 'guest_19825376288', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (45, NULL, '2025-05-15 13:30:00', '2025-05-15 17:45:00', 21.00, 'COMPLETED', '2025-05-15 13:30:00', b'1', 'EXTERNAL_USER', 21.00, '2025-05-15 17:45:00.000000', 217, 25, '创业园', '浙D88888_T020', 'guest_19825376288', NULL, NULL, 'PAID', NULL);
INSERT INTO `parking_records` VALUES (46, 33, '2025-02-10 09:30:00', '2025-02-10 12:30:00', 0.00, 'COMPLETED', '2025-02-10 09:30:00', b'0', 'STUDENT', 0.00, '2025-02-10 12:30:00.000000', 2, 11, 'A区', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (47, 33, '2025-03-15 14:00:00', '2025-03-15 17:30:00', 0.00, 'COMPLETED', '2025-03-15 14:00:00', b'0', 'STUDENT', 0.00, '2025-03-15 17:30:00.000000', 9, 11, 'C', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (48, 39, '2025-04-20 08:45:00', '2025-04-20 16:30:00', 0.00, 'COMPLETED', '2025-04-20 08:45:00', b'0', 'STUDENT', 0.00, '2025-04-20 16:30:00.000000', 17, 11, 'C', '贵A12124', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (49, 75, '2025-05-25 09:00:00', '2025-05-25 18:00:00', 0.00, 'COMPLETED', '2025-05-25 09:00:00', b'0', 'STAFF', 0.00, '2025-05-25 18:00:00.000000', 122, 13, 'A', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (50, 75, '2025-06-18 10:30:00', '2025-06-18 15:45:00', 0.00, 'COMPLETED', '2025-06-18 10:30:00', b'0', 'STAFF', 0.00, '2025-06-18 15:45:00.000000', 134, 13, '南门', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (51, NULL, '2026-01-03 16:54:45', '2026-01-03 17:40:07', NULL, 'COMPLETED', '2026-01-03 16:54:45', b'1', 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.092445', 58, 23, '图书馆', '粤A88888_T025', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (52, NULL, '2026-01-03 16:24:45', '2026-01-03 17:40:10', NULL, 'COMPLETED', '2026-01-03 16:24:45', b'1', 'EXTERNAL_USER', 10.00, '2026-01-05 22:57:36.093452', 68, 25, '一食堂', '沪B66666_T026', '19825376288', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (53, 33, '2026-01-03 17:09:45', '2026-01-04 18:04:56', NULL, 'COMPLETED', '2026-01-03 17:09:45', b'0', 'STUDENT', 0.00, '2026-01-05 22:57:36.092445', 76, 11, '二食堂', '贵A12346', '哎哎哎', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (54, 75, '2026-01-03 16:09:45', '2026-01-04 13:59:26', NULL, 'COMPLETED', '2026-01-03 16:09:45', b'0', 'STAFF', 0.00, '2026-01-05 22:57:36.093452', 122, 13, 'A', '琼A12121', '梁非凡', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (55, NULL, '2026-01-04 18:04:12', '2026-01-04 18:04:20', NULL, 'COMPLETED', '2026-01-04 18:04:12', NULL, 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.092445', 17, 23, 'C', '琼A12121', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (56, NULL, '2026-01-05 22:50:19', '2026-01-05 22:50:24', NULL, 'COMPLETED', '2026-01-05 22:50:19', NULL, 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.092445', 18, 23, 'C', '琼A12121', '18260916208', NULL, NULL, 'UNPAID', NULL);
INSERT INTO `parking_records` VALUES (57, NULL, '2026-01-05 22:50:45', '2026-01-05 22:50:48', NULL, 'COMPLETED', '2026-01-05 22:50:45', NULL, 'EXTERNAL_USER', 5.00, '2026-01-05 22:57:36.092445', 104, 23, '体育馆', '京A66666', '18260916208', NULL, NULL, 'UNPAID', NULL);

-- ----------------------------
-- Table structure for parking_spots
-- ----------------------------
DROP TABLE IF EXISTS `parking_spots`;
CREATE TABLE `parking_spots`  (
  `id` bigint NOT NULL DEFAULT 0,
  `spot_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号',
  `zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域',
  `spot_type` enum('REGULAR','DISABLED','RESERVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'REGULAR' COMMENT '车位类型',
  `status` enum('AVAILABLE','OCCUPIED','MAINTENANCE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'AVAILABLE' COMMENT '车位状态',
  `hourly_rate` decimal(38, 2) NULL DEFAULT NULL COMMENT '小时费率',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '具体位置',
  `parking_lot_id` bigint NULL DEFAULT NULL COMMENT '所属停车场ID',
  `spot_in_lot_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '停车场内编号',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parking_spots
-- ----------------------------
INSERT INTO `parking_spots` VALUES (1, 'A001', 'A区', 'REGULAR', 'MAINTENANCE', 5.00, '未指定', 17, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (2, 'A002', 'A区', 'REGULAR', 'AVAILABLE', 5.00, '未指定', 17, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (3, 'A003', 'A区', 'REGULAR', 'AVAILABLE', 5.00, '未指定', 17, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (4, 'A8888', 'A', 'REGULAR', 'AVAILABLE', 5.00, NULL, 17, '8888', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (5, 'A9999', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 17, '9999', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (6, 'A1000', '北门', 'REGULAR', 'AVAILABLE', 5.00, NULL, 17, '1000', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (7, 'B001', 'B区', 'DISABLED', 'AVAILABLE', 0.00, '未指定', 18, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (8, 'B002', 'B区', 'RESERVED', 'AVAILABLE', 8.00, '未指定', 18, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (9, 'C0101', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0101', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (10, 'C0102', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0102', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (11, 'C0103', 'C', 'REGULAR', 'OCCUPIED', 5.00, NULL, 19, '0103', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (12, 'C0104', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0104', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (13, 'C0105', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0105', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (14, 'C0106', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0106', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (15, 'C0107', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0107', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (16, 'C0108', 'C', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 19, '0108', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (17, 'C0201', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0201', '2026-01-03 14:25:57', '2026-01-04 18:04:19');
INSERT INTO `parking_spots` VALUES (18, 'C0202', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0202', '2026-01-03 14:25:57', '2026-01-05 22:50:24');
INSERT INTO `parking_spots` VALUES (19, 'C0203', 'C', 'REGULAR', 'OCCUPIED', 4.00, NULL, 19, '0203', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (20, 'C0204', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0204', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (21, 'C0205', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0205', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (22, 'C0206', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0206', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (23, 'C0301', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0301', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (24, 'C0302', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0302', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (25, 'C0303', 'C', 'REGULAR', 'OCCUPIED', 6.00, NULL, 19, '0303', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (26, 'C0304', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0304', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (27, 'C0305', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0305', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (28, 'C0306', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0306', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (29, 'C0307', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0307', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (30, 'C0401', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0401', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (31, 'C0402', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0402', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (32, 'C0403', 'C', 'REGULAR', 'OCCUPIED', 5.00, NULL, 19, '0403', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (33, 'C0404', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0404', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (34, 'C0405', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0405', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (35, 'C0406', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0406', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (36, 'C0407', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0407', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (37, 'C0408', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0408', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (38, 'C0501', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0501', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (39, 'C0502', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0502', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (40, 'C0503', 'C', 'REGULAR', 'OCCUPIED', 4.00, NULL, 19, '0503', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (41, 'C0504', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0504', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (42, 'C0505', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0505', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (43, 'C0601', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0601', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (44, 'C0602', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0602', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (45, 'C0603', 'C', 'REGULAR', 'OCCUPIED', 5.00, NULL, 19, '0603', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (46, 'C0604', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0604', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (47, 'C0605', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0605', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (48, 'C0606', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0606', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (49, 'C001', '创业园', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (50, 'D110', '学生活动中心', 'DISABLED', 'AVAILABLE', 5.00, '未指定', 20, '110', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (51, 'NORTH001', '北门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 1, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (52, 'NORTH002', '北门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 1, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (53, 'NORTH003', '北门', 'REGULAR', 'OCCUPIED', 8.00, NULL, 1, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (54, 'NORTH004', '北门', 'DISABLED', 'AVAILABLE', 5.00, NULL, 1, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (55, 'SOUTH001', '南门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 2, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (56, 'SOUTH002', '南门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 2, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (57, 'SOUTH003', '南门', 'DISABLED', 'AVAILABLE', 0.00, NULL, 2, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (58, 'LIB001', '图书馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 3, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (59, 'LIB002', '图书馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 3, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (60, 'LIB003', '图书馆', 'DISABLED', 'AVAILABLE', 0.00, NULL, 3, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (61, 'LIB004', '图书馆', 'REGULAR', 'OCCUPIED', 5.00, NULL, 3, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (62, 'LIB005', '图书馆', 'REGULAR', 'AVAILABLE', 6.00, NULL, 3, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (63, 'DINE301', '三食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 4, '301', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (64, 'DINE302', '三食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 4, '302', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (65, 'DINE303', '三食堂', 'REGULAR', 'OCCUPIED', 4.00, NULL, 4, '303', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (66, 'DINE305', '三食堂', 'DISABLED', 'MAINTENANCE', 4.50, NULL, 4, '305', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (67, 'DINE306', '三食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 4, '306', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (68, 'DINE101', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '101', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (69, 'DINE102', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '102', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (70, 'DINE103', '一食堂', 'REGULAR', 'OCCUPIED', 4.00, NULL, 5, '103', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (71, 'DINE104', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '104', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (72, 'DINE105', '一食堂', 'DISABLED', 'AVAILABLE', 0.00, NULL, 5, '105', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (73, 'DINE106', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '106', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (74, 'DINE107', '一食堂', 'REGULAR', 'MAINTENANCE', 4.00, NULL, 5, '107', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (75, 'DINE108', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '108', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (76, 'DINE201', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '201', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (77, 'DINE202', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '202', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (78, 'DINE203', '二食堂', 'REGULAR', 'OCCUPIED', 4.00, NULL, 6, '203', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (79, 'DINE204', '二食堂', 'DISABLED', 'AVAILABLE', 0.00, NULL, 6, '204', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (80, 'DINE205', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '205', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (81, 'DINE206', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '206', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (82, 'FOOD001', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (83, 'FOOD002', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (84, 'FOOD003', '美食街', 'REGULAR', 'OCCUPIED', 6.00, NULL, 7, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (85, 'FOOD004', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (86, 'FOOD005', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (87, 'FOOD006', '美食街', 'DISABLED', 'AVAILABLE', 0.00, NULL, 7, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (88, 'FOOD007', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (89, 'ACT001', '活动中心', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 8, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (90, 'ACT002', '新闻科大楼', 'REGULAR', 'AVAILABLE', 50.00, NULL, 8, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (91, 'ACT004', '活动中心', 'DISABLED', 'AVAILABLE', 0.00, NULL, 8, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (92, 'ACT005', '活动中心', 'REGULAR', 'AVAILABLE', 5.00, NULL, 8, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (93, 'ACT006', '活动中心', 'REGULAR', 'AVAILABLE', 5.00, NULL, 8, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (94, 'ACT007', '活动中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 8, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (95, 'ACT008', '活动中心', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 8, '008', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (96, 'B101', '活动中心', 'REGULAR', 'AVAILABLE', 5.00, NULL, 8, '101', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (97, 'PERF001', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (98, 'PERF002', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (99, 'PERF003', '演艺中心', 'REGULAR', 'OCCUPIED', 6.00, NULL, 9, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (100, 'PERF004', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (101, 'PERF005', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (102, 'PERF006', '演艺中心', 'DISABLED', 'AVAILABLE', 0.00, NULL, 9, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (103, 'PERF007', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (104, 'GYM001', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '001', '2026-01-03 14:25:57', '2026-01-05 22:50:47');
INSERT INTO `parking_spots` VALUES (105, 'GYM002', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (106, 'GYM003', '体育馆', 'REGULAR', 'OCCUPIED', 5.00, NULL, 10, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (107, 'GYM004', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (108, 'GYM005', '体育馆', 'DISABLED', 'AVAILABLE', 0.00, NULL, 10, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (109, 'GYM006', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (110, 'GYM007', '体育馆', 'REGULAR', 'AVAILABLE', 6.00, NULL, 10, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (111, 'GYM008', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '008', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (112, 'TRAIN001', '训练馆', 'REGULAR', 'OCCUPIED', 4.00, NULL, 11, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (113, 'TRAIN002', '训练馆', 'REGULAR', 'AVAILABLE', 4.00, NULL, 11, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (114, 'TRAIN003', '训练馆', 'REGULAR', 'OCCUPIED', 4.00, NULL, 11, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (115, 'TRAIN004', '训练馆', 'REGULAR', 'AVAILABLE', 4.00, NULL, 11, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (116, 'IFLY001', 'A', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (117, 'IFLY002', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (118, 'IFLY003', '科大讯飞楼', 'REGULAR', 'OCCUPIED', 5.00, NULL, 12, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (119, 'IFLY004', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 6.00, NULL, 12, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (120, 'IFLY005', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (121, 'IFLY006', '科大讯飞楼', 'DISABLED', 'AVAILABLE', 0.00, NULL, 12, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (122, 'A10', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '010', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (123, 'START001', '大学生创业园', 'REGULAR', 'AVAILABLE', 3.50, NULL, 13, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (124, 'START002', '大学生创业园', 'REGULAR', 'AVAILABLE', 4.00, NULL, 13, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (125, 'START003', '大学生创业园', 'REGULAR', 'OCCUPIED', 4.00, NULL, 13, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (126, 'START004', '大学生创业园', 'REGULAR', 'AVAILABLE', 4.00, NULL, 13, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (127, 'START005', '大学生创业园', 'DISABLED', 'AVAILABLE', 0.00, NULL, 13, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (128, 'START006', '大学生创业园', 'REGULAR', 'AVAILABLE', 4.00, NULL, 13, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (129, 'NG001', '南硅谷', 'REGULAR', 'AVAILABLE', 5.00, NULL, 14, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (130, 'NG002', '南硅谷', 'REGULAR', 'AVAILABLE', 5.00, NULL, 14, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (131, 'NG003', '南硅谷', 'REGULAR', 'AVAILABLE', 6.00, NULL, 14, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (132, 'NG004', '南硅谷', 'REGULAR', 'OCCUPIED', 5.00, NULL, 14, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (133, 'NG005', '南硅谷', 'REGULAR', 'AVAILABLE', 5.00, NULL, 14, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (134, 'NEWS001', '新闻科大楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 15, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (135, 'NEWS002', '新闻科大楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 15, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (136, 'NEWS003', '新闻科大楼', 'DISABLED', 'AVAILABLE', 5.00, NULL, 15, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (137, 'NEWS004', '新闻科大楼', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 15, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (138, 'WANKE001', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (139, 'WANKE002', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (140, 'WANKE003', '万科楼', 'REGULAR', 'OCCUPIED', 5.00, NULL, 16, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (141, 'WANKE004', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (142, 'WANKE005', '万科楼', 'DISABLED', 'AVAILABLE', 0.00, NULL, 16, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (143, 'WANKE006', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots` VALUES (144, 'TEST001', 'A', 'REGULAR', 'AVAILABLE', 5.00, NULL, 21, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');

-- ----------------------------
-- Table structure for parking_spots_old
-- ----------------------------
DROP TABLE IF EXISTS `parking_spots_old`;
CREATE TABLE `parking_spots_old`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `spot_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车位编号',
  `zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域',
  `spot_type` enum('REGULAR','DISABLED','RESERVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'REGULAR' COMMENT '车位类型',
  `status` enum('AVAILABLE','OCCUPIED','MAINTENANCE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'AVAILABLE' COMMENT '车位状态',
  `hourly_rate` decimal(38, 2) NULL DEFAULT NULL COMMENT '小时费率',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '具体位置',
  `parking_lot_id` bigint NULL DEFAULT NULL COMMENT '所属停车场ID',
  `spot_in_lot_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '停车场内编号',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spot_number`(`spot_number` ASC) USING BTREE COMMENT '车位编号唯一',
  UNIQUE INDEX `uk_lot_spot_number`(`parking_lot_id` ASC, `spot_in_lot_number` ASC) USING BTREE COMMENT '停车场内编号唯一',
  INDEX `idx_parking_lot_id`(`parking_lot_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_zone`(`zone` ASC) USING BTREE,
  INDEX `idx_spot_in_lot_number`(`spot_in_lot_number` ASC) USING BTREE,
  CONSTRAINT `fk_parking_spots_lot` FOREIGN KEY (`parking_lot_id`) REFERENCES `parking_lots` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 147 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '停车位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of parking_spots_old
-- ----------------------------
INSERT INTO `parking_spots_old` VALUES (1, 'A001', 'A区', 'REGULAR', 'MAINTENANCE', 5.00, '未指定', 17, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (2, 'A002', 'A区', 'REGULAR', 'AVAILABLE', 5.00, '未指定', 17, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (3, 'A003', 'A区', 'REGULAR', 'AVAILABLE', 5.00, '未指定', 17, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (4, 'A8888', 'A', 'REGULAR', 'AVAILABLE', 5.00, NULL, 17, '8888', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (5, 'A9999', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 17, '9999', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (6, 'A1000', '北门', 'REGULAR', 'AVAILABLE', 5.00, NULL, 17, '1000', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (7, 'B001', 'B区', 'DISABLED', 'AVAILABLE', 0.00, '未指定', 18, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (8, 'B002', 'B区', 'RESERVED', 'AVAILABLE', 8.00, '未指定', 18, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (9, 'C0101', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0101', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (10, 'C0102', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0102', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (11, 'C0103', 'C', 'REGULAR', 'OCCUPIED', 5.00, NULL, 19, '0103', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (12, 'C0104', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0104', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (13, 'C0105', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0105', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (14, 'C0106', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0106', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (15, 'C0107', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0107', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (16, 'C0108', 'C', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 19, '0108', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (17, 'C0201', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0201', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (18, 'C0202', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0202', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (19, 'C0203', 'C', 'REGULAR', 'OCCUPIED', 4.00, NULL, 19, '0203', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (20, 'C0204', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0204', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (21, 'C0205', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0205', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (22, 'C0206', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0206', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (23, 'C0301', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0301', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (24, 'C0302', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0302', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (25, 'C0303', 'C', 'REGULAR', 'OCCUPIED', 6.00, NULL, 19, '0303', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (26, 'C0304', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0304', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (27, 'C0305', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0305', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (28, 'C0306', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0306', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (29, 'C0307', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0307', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (30, 'C0401', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0401', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (31, 'C0402', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0402', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (32, 'C0403', 'C', 'REGULAR', 'OCCUPIED', 5.00, NULL, 19, '0403', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (33, 'C0404', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0404', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (34, 'C0405', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0405', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (35, 'C0406', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0406', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (36, 'C0407', 'C', 'REGULAR', 'AVAILABLE', 6.00, NULL, 19, '0407', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (37, 'C0408', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0408', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (38, 'C0501', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0501', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (39, 'C0502', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0502', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (40, 'C0503', 'C', 'REGULAR', 'OCCUPIED', 4.00, NULL, 19, '0503', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (41, 'C0504', 'C', 'REGULAR', 'AVAILABLE', 4.00, NULL, 19, '0504', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (42, 'C0505', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0505', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (43, 'C0601', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0601', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (44, 'C0602', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0602', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (45, 'C0603', 'C', 'REGULAR', 'OCCUPIED', 5.00, NULL, 19, '0603', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (46, 'C0604', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0604', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (47, 'C0605', 'C', 'DISABLED', 'AVAILABLE', 0.00, NULL, 19, '0605', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (48, 'C0606', 'C', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0606', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (49, 'C001', '创业园', 'REGULAR', 'AVAILABLE', 5.00, NULL, 19, '0001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (50, 'D110', '学生活动中心', 'DISABLED', 'AVAILABLE', 5.00, '未指定', 20, '110', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (51, 'NORTH001', '北门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 1, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (52, 'NORTH002', '北门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 1, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (53, 'NORTH003', '北门', 'REGULAR', 'OCCUPIED', 8.00, NULL, 1, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (54, 'NORTH004', '北门', 'DISABLED', 'AVAILABLE', 5.00, NULL, 1, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (55, 'SOUTH001', '南门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 2, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (56, 'SOUTH002', '南门', 'REGULAR', 'AVAILABLE', 8.00, NULL, 2, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (57, 'SOUTH003', '南门', 'DISABLED', 'AVAILABLE', 0.00, NULL, 2, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (58, 'LIB001', '图书馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 3, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (59, 'LIB002', '图书馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 3, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (60, 'LIB003', '图书馆', 'DISABLED', 'AVAILABLE', 0.00, NULL, 3, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (61, 'LIB004', '图书馆', 'REGULAR', 'OCCUPIED', 5.00, NULL, 3, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (62, 'LIB005', '图书馆', 'REGULAR', 'AVAILABLE', 6.00, NULL, 3, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (63, 'DINE301', '三食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 4, '301', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (64, 'DINE302', '三食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 4, '302', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (65, 'DINE303', '三食堂', 'REGULAR', 'OCCUPIED', 4.00, NULL, 4, '303', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (66, 'DINE305', '三食堂', 'DISABLED', 'MAINTENANCE', 4.50, NULL, 4, '305', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (67, 'DINE306', '三食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 4, '306', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (68, 'DINE101', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '101', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (69, 'DINE102', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '102', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (70, 'DINE103', '一食堂', 'REGULAR', 'OCCUPIED', 4.00, NULL, 5, '103', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (71, 'DINE104', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '104', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (72, 'DINE105', '一食堂', 'DISABLED', 'AVAILABLE', 0.00, NULL, 5, '105', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (73, 'DINE106', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '106', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (74, 'DINE107', '一食堂', 'REGULAR', 'MAINTENANCE', 4.00, NULL, 5, '107', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (75, 'DINE108', '一食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 5, '108', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (76, 'DINE201', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '201', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (77, 'DINE202', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '202', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (78, 'DINE203', '二食堂', 'REGULAR', 'OCCUPIED', 4.00, NULL, 6, '203', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (79, 'DINE204', '二食堂', 'DISABLED', 'AVAILABLE', 0.00, NULL, 6, '204', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (80, 'DINE205', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '205', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (81, 'DINE206', '二食堂', 'REGULAR', 'AVAILABLE', 4.00, NULL, 6, '206', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (82, 'FOOD001', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (83, 'FOOD002', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (84, 'FOOD003', '美食街', 'REGULAR', 'OCCUPIED', 6.00, NULL, 7, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (85, 'FOOD004', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (86, 'FOOD005', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (87, 'FOOD006', '美食街', 'DISABLED', 'AVAILABLE', 0.00, NULL, 7, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (88, 'FOOD007', '美食街', 'REGULAR', 'AVAILABLE', 6.00, NULL, 7, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (89, 'ACT001', '活动中心', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 8, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (90, 'ACT002', '新闻科大楼', 'REGULAR', 'AVAILABLE', 50.00, NULL, 8, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (91, 'ACT004', '活动中心', 'DISABLED', 'AVAILABLE', 0.00, NULL, 8, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (92, 'ACT005', '活动中心', 'REGULAR', 'AVAILABLE', 5.00, NULL, 8, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (93, 'ACT006', '活动中心', 'REGULAR', 'AVAILABLE', 5.00, NULL, 8, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (94, 'ACT007', '活动中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 8, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (95, 'ACT008', '活动中心', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 8, '008', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (96, 'B101', '活动中心', 'REGULAR', 'AVAILABLE', 5.00, NULL, 8, '101', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (97, 'PERF001', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (98, 'PERF002', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (99, 'PERF003', '演艺中心', 'REGULAR', 'OCCUPIED', 6.00, NULL, 9, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (100, 'PERF004', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (101, 'PERF005', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (102, 'PERF006', '演艺中心', 'DISABLED', 'AVAILABLE', 0.00, NULL, 9, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (103, 'PERF007', '演艺中心', 'REGULAR', 'AVAILABLE', 6.00, NULL, 9, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (104, 'GYM001', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (105, 'GYM002', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (106, 'GYM003', '体育馆', 'REGULAR', 'OCCUPIED', 5.00, NULL, 10, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (107, 'GYM004', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (108, 'GYM005', '体育馆', 'DISABLED', 'AVAILABLE', 0.00, NULL, 10, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (109, 'GYM006', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (110, 'GYM007', '体育馆', 'REGULAR', 'AVAILABLE', 6.00, NULL, 10, '007', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (111, 'GYM008', '体育馆', 'REGULAR', 'AVAILABLE', 5.00, NULL, 10, '008', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (112, 'TRAIN001', '训练馆', 'REGULAR', 'OCCUPIED', 4.00, NULL, 11, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (113, 'TRAIN002', '训练馆', 'REGULAR', 'AVAILABLE', 4.00, NULL, 11, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (114, 'TRAIN003', '训练馆', 'REGULAR', 'OCCUPIED', 4.00, NULL, 11, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (115, 'TRAIN004', '训练馆', 'REGULAR', 'AVAILABLE', 4.00, NULL, 11, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (116, 'IFLY001', 'A', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (117, 'IFLY002', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (118, 'IFLY003', '科大讯飞楼', 'REGULAR', 'OCCUPIED', 5.00, NULL, 12, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (119, 'IFLY004', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 6.00, NULL, 12, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (120, 'IFLY005', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (121, 'IFLY006', '科大讯飞楼', 'DISABLED', 'AVAILABLE', 0.00, NULL, 12, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (122, 'A10', '科大讯飞楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 12, '010', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (123, 'START001', '大学生创业园', 'REGULAR', 'AVAILABLE', 3.50, NULL, 13, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (124, 'START002', '大学生创业园', 'REGULAR', 'AVAILABLE', 4.00, NULL, 13, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (125, 'START003', '大学生创业园', 'REGULAR', 'OCCUPIED', 4.00, NULL, 13, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (126, 'START004', '大学生创业园', 'REGULAR', 'AVAILABLE', 4.00, NULL, 13, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (127, 'START005', '大学生创业园', 'DISABLED', 'AVAILABLE', 0.00, NULL, 13, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (128, 'START006', '大学生创业园', 'REGULAR', 'AVAILABLE', 4.00, NULL, 13, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (129, 'NG001', '南硅谷', 'REGULAR', 'AVAILABLE', 5.00, NULL, 14, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (130, 'NG002', '南硅谷', 'REGULAR', 'AVAILABLE', 5.00, NULL, 14, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (131, 'NG003', '南硅谷', 'REGULAR', 'AVAILABLE', 6.00, NULL, 14, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (132, 'NG004', '南硅谷', 'REGULAR', 'OCCUPIED', 5.00, NULL, 14, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (133, 'NG005', '南硅谷', 'REGULAR', 'AVAILABLE', 5.00, NULL, 14, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (134, 'NEWS001', '新闻科大楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 15, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (135, 'NEWS002', '新闻科大楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 15, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (136, 'NEWS003', '新闻科大楼', 'DISABLED', 'AVAILABLE', 5.00, NULL, 15, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (137, 'NEWS004', '新闻科大楼', 'REGULAR', 'MAINTENANCE', 5.00, NULL, 15, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (138, 'WANKE001', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (139, 'WANKE002', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '002', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (140, 'WANKE003', '万科楼', 'REGULAR', 'OCCUPIED', 5.00, NULL, 16, '003', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (141, 'WANKE004', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '004', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (142, 'WANKE005', '万科楼', 'DISABLED', 'AVAILABLE', 0.00, NULL, 16, '005', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (143, 'WANKE006', '万科楼', 'REGULAR', 'AVAILABLE', 5.00, NULL, 16, '006', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (144, 'TEST001', 'A', 'REGULAR', 'AVAILABLE', 5.00, NULL, 21, '001', '2026-01-03 14:25:57', '2026-01-03 14:25:57');
INSERT INTO `parking_spots_old` VALUES (145, 'A1009', '二食堂', 'REGULAR', 'AVAILABLE', 5.00, '未指定', NULL, NULL, '2026-01-05 23:16:58', '2026-01-05 23:16:58');
INSERT INTO `parking_spots_old` VALUES (146, 'A1008', '创业园', 'REGULAR', 'AVAILABLE', 5.00, '未指定', NULL, NULL, '2026-01-05 23:17:23', '2026-01-05 23:17:23');

-- ----------------------------
-- Table structure for reservations
-- ----------------------------
DROP TABLE IF EXISTS `reservations`;
CREATE TABLE `reservations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `end_time` datetime(6) NULL DEFAULT NULL,
  `start_time` datetime(6) NULL DEFAULT NULL,
  `status` enum('ACTIVE','CANCELLED','COMPLETED','EXPIRED','IN_USE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `parking_spot_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKb5g9io5h54iwl2inkno50ppln`(`user_id` ASC) USING BTREE,
  INDEX `FKiga37f34y7fl3x89wl2jasfdj`(`parking_spot_id` ASC) USING BTREE,
  CONSTRAINT `FKb5g9io5h54iwl2inkno50ppln` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKiga37f34y7fl3x89wl2jasfdj` FOREIGN KEY (`parking_spot_id`) REFERENCES `parking_spots_old` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reservations
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `need_payment` tinyint(1) NOT NULL DEFAULT 0,
  `balance` double NULL DEFAULT NULL,
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号/工号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$vLNNzlExyj/jXQc/jgMpOOVHou/2uXAhFFXALGDz3wBVMqpuvgxPW', '系统管理员', '13800138000', 'admin@campus.com', 'ADMIN', '2025-11-18 17:49:44', '2025-11-28 12:37:22', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (2, '水水水水', '$2a$10$oQ/jra98ZIlaZbO8uEqOcOcfAWi.fAybDnvuIyaG7QKmc7YsryAbK', '是是是', '12212345434', '2096274376@qq.com', 'STUDENT', '2025-11-18 17:50:48', '2025-11-23 13:59:00', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (5, '呃呃呃', '$2a$10$lj11QYwT.49GwPFS72TrxOU9uwkC2HIskBtydjBdYBi8fIxmh2uu.', '啊啊啊', '12121212121', '2096274376@qq.com', 'STUDENT', '2025-11-23 17:59:25', '2025-11-30 15:38:32', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (7, '嘻嘻嘻', '$2a$10$j4mSxHE.0Pzv6ZkFiD/uiuC3nx.m0MAmsUhUnIEq1cSdQP9aPqyva', '啊啊啊', '12121212121', '2096274376@qq.com', 'STUDENT', '2025-11-23 21:40:45', '2025-12-25 18:57:09', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (11, '擦擦擦', '$2a$10$yJ/uuwYDF7a3SEB1q3g9auMQAKI.2VfAg7kuwpOwBX.rFMSP1jLTS', '哎哎哎', '12121212333', '2096212126@qq.com', 'STUDENT', '2025-11-27 22:07:45', '2025-11-27 22:23:44', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (12, '热热热', '$2a$10$MucqMbuNBEUNN3ZElTxDxuKcyBRs609iFJEo.7cO1pnyOIzhZxZ4i', '唉唉唉', '12121212121', '2096212126@qq.com', 'STAFF', '2025-11-30 18:24:20', '2025-12-15 23:40:57', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (13, '梁非凡', '$2a$10$39/Zi0USugt2kpOoml2naOltY2fs2VzfqcudZldL1SYdk6fMCHwf2', '梁非凡', '19162368331', '2096274376@qq.com', 'STAFF', '2025-12-09 16:32:03', '2026-01-02 13:17:00', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (14, '葫芦娃', '$2a$10$Drr6gsbC6DWJj9UfD3ijpu0mpHZIMiiI5Yp2Ug3HvGiEy1AwVmKrG', '奥特曼', '12345678900', '2096274376@qq.com', 'TEACHER', '2025-12-11 13:22:55', '2025-12-11 13:23:20', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (17, '猪猪侠', '$2a$10$4FPFw96575fP/imh0hNgVehEUotvBWrYzOmlIz8m8Mmgo99bJO142', '新加坡', '18776936036', '2096274276@qq.com', 'TEACHER', '2025-12-16 15:10:24', '2025-12-16 15:19:07', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (22, '略略略', '$2a$10$EiOPVhRDaOZ3nM2i0ktQPuRi9BatdMeG74yYLecV8W.Q7ZGpKQEze', '嗷嗷嗷', '12121212129', '2096274076@qq.com', 'STUDENT', '2025-12-25 18:54:33', '2025-12-25 18:58:06', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (23, 'guest_18260916208', '$2a$10$cjEEsawTJucmJ.e/rDt7eeYVlTdsqAqXE1tW4Az.K/ItELkZ1SBkC', NULL, '18260916208', NULL, 'EXTERNAL_USER', '2025-12-25 23:50:59', '2025-12-25 23:50:59', 'APPROVED', 1, 0, NULL);
INSERT INTO `users` VALUES (24, '阿城市', '$2a$10$VrEQgduodyHceJ/zA7JjNeYJISvcXoZ.hMLD0clBFMWE2bqHFXHpa', '测算', '12124523247', '2096212376@qq.com', 'STUDENT', '2026-01-01 15:48:00', '2026-01-01 17:13:56', 'APPROVED', 0, 0, NULL);
INSERT INTO `users` VALUES (25, 'guest_19825376288', '$2a$10$54izidz9hDUzJU07yyiK7.zuWJg9G4.JHkjrNDIdfaFajAG9sEiui', NULL, '19825376288', NULL, 'EXTERNAL_USER', '2026-01-01 17:14:53', '2026-01-01 17:14:53', 'APPROVED', 1, 0, NULL);

-- ----------------------------
-- Table structure for vehicles
-- ----------------------------
DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `plate_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '车辆类型: CAR, MOTORCYCLE, ELECTRIC_CAR',
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING',
  `is_temporary` tinyint(1) NULL DEFAULT 0,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `plate_number`(`plate_number` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `vehicles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 90 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vehicles
-- ----------------------------
INSERT INTO `vehicles` VALUES (33, 11, '贵A12346', 'CAR', '奔驰', '白色', '2025-11-28 11:36:22', 'APPROVED', 0, '2025-12-15 16:55:30');
INSERT INTO `vehicles` VALUES (39, 11, '贵A12124', 'CAR', '本田', '绿色', '2025-11-30 18:28:06', 'APPROVED', 0, '2025-11-30 18:28:24');
INSERT INTO `vehicles` VALUES (40, 11, '京A123456', 'CAR', '迈巴赫', '黑色', '2025-11-30 18:36:46', 'APPROVED', 0, '2025-12-02 16:15:51');
INSERT INTO `vehicles` VALUES (71, 14, '桂A4443', 'CAR', '宝马', '白色', '2025-12-20 15:01:28', 'APPROVED', 0, '2025-12-20 15:03:01');
INSERT INTO `vehicles` VALUES (72, 14, '桂A4449', 'CAR', '宝马', '黑色', '2025-12-20 15:05:33', 'APPROVED', 0, '2025-12-20 15:11:01');
INSERT INTO `vehicles` VALUES (73, 14, '贵A12121', 'CAR', '宝马', '黑色', '2025-12-20 15:11:59', 'APPROVED', 0, '2025-12-20 15:12:07');
INSERT INTO `vehicles` VALUES (75, 13, '琼A12121', 'CAR', '宝马', '宝色', '2025-12-24 13:30:40', 'APPROVED', 0, '2025-12-24 13:31:01');
INSERT INTO `vehicles` VALUES (81, 23, '琼A12121_T1233', 'CAR', '临时车辆', '未知', '2025-12-25 23:51:11', 'APPROVED', 1, '2025-12-25 23:51:11');
INSERT INTO `vehicles` VALUES (82, 13, '京A12121', 'CAR', '宝马', '黑色', '2026-01-01 15:50:01', 'APPROVED', 0, '2026-01-03 15:40:01');
INSERT INTO `vehicles` VALUES (83, 25, '琼A12121_T4883', 'CAR', '临时车辆', '未知', '2026-01-01 17:15:05', 'APPROVED', 1, '2026-01-01 17:15:05');
INSERT INTO `vehicles` VALUES (84, 23, '京A66666', 'CAR', '临时车辆', '未知', '2026-01-01 17:25:42', 'APPROVED', 1, '2026-01-01 17:25:42');
INSERT INTO `vehicles` VALUES (85, 23, '桂A12120', 'CAR', '临时车辆', '未知', '2026-01-01 17:25:55', 'APPROVED', 1, '2026-01-01 17:25:55');
INSERT INTO `vehicles` VALUES (86, 23, '贵A12120', 'CAR', '临时车辆', '未知', '2026-01-01 17:26:12', 'APPROVED', 1, '2026-01-01 17:26:12');
INSERT INTO `vehicles` VALUES (87, 23, '琼A12121_T2120', 'CAR', '临时车辆', '未知', '2026-01-04 18:04:12', 'APPROVED', 1, '2026-01-04 18:04:12');
INSERT INTO `vehicles` VALUES (88, 23, '琼A12121_T8367', 'CAR', '临时车辆', '未知', '2026-01-05 22:50:18', 'APPROVED', 1, '2026-01-05 22:50:18');
INSERT INTO `vehicles` VALUES (89, 13, '京A12345', 'CAR', '奔驰', '黑色', '2026-01-05 22:53:01', 'APPROVED', 0, '2026-01-05 22:53:08');

SET FOREIGN_KEY_CHECKS = 1;
