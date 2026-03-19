-- Flyway Baseline Migration
-- Source: database/tdesign_init.sql (structure only, data rows removed)
-- Governance: schema evolution must continue with incremental V{N} scripts.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_provider_settings
-- ----------------------------
DROP TABLE IF EXISTS `ai_provider_settings`;
CREATE TABLE `ai_provider_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `vendor` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `endpoint_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `temperature` double NULL DEFAULT NULL,
  `max_tokens` int NULL DEFAULT NULL,
  `is_default` tinyint NULL DEFAULT NULL,
  `enabled` tinyint NULL DEFAULT NULL,
  `extra_headers` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_test_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_test_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_tested_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
SET FOREIGN_KEY_CHECKS = 1;
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `publish_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `created_by_id` bigint NULL DEFAULT NULL,
  `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_broadcasted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for file_resources
-- ----------------------------
DROP TABLE IF EXISTS `file_resources`;
CREATE TABLE `file_resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_by_id` bigint NULL DEFAULT NULL,
  `created_by_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `to_user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL,
  `collected` tinyint NOT NULL,
  `quality` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_messages_user_time`(`to_user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_messages_user` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `publish_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `created_by_id` bigint NULL DEFAULT NULL,
  `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notifications_status_updated_at`(`status` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_notifications_status_publish_at`(`status` ASC, `publish_at` ASC) USING BTREE,
  INDEX `idx_notifications_priority_status`(`priority` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `os` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `browser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `user_guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_logs_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_operation_logs_user_created_at`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_operation_logs_action_created_at`(`action` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 379 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for role_menus
-- ----------------------------
DROP TABLE IF EXISTS `role_menus`;
CREATE TABLE `role_menus`  (
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `role_id` bigint NOT NULL,
  `permission` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`role_id`, `permission`) USING BTREE,
  CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for security_captcha_settings
-- ----------------------------
DROP TABLE IF EXISTS `security_captcha_settings`;
CREATE TABLE `security_captcha_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `captcha_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `drag_captcha_width` int NULL DEFAULT NULL,
  `drag_captcha_height` int NULL DEFAULT NULL,
  `drag_captcha_threshold` int NULL DEFAULT NULL,
  `image_captcha_length` int NULL DEFAULT NULL,
  `image_captcha_noise_lines` int NULL DEFAULT NULL,
  `captcha_enabled` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for security_password_policy
-- ----------------------------
DROP TABLE IF EXISTS `security_password_policy`;
CREATE TABLE `security_password_policy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password_min_length` int NULL DEFAULT NULL,
  `password_require_uppercase` tinyint NULL DEFAULT NULL,
  `password_require_lowercase` tinyint NULL DEFAULT NULL,
  `password_require_special` tinyint NULL DEFAULT NULL,
  `password_allow_sequential` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for security_token_settings
-- ----------------------------
DROP TABLE IF EXISTS `security_token_settings`;
CREATE TABLE `security_token_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_timeout_minutes` int NULL DEFAULT NULL,
  `token_timeout_minutes` int NULL DEFAULT NULL,
  `token_refresh_grace_minutes` int NULL DEFAULT NULL,
  `allow_url_token_param` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for verification_sms_settings
-- ----------------------------
DROP TABLE IF EXISTS `verification_sms_settings`;
CREATE TABLE `verification_sms_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sms_enabled` tinyint NULL DEFAULT NULL,
  `sms_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_enabled` tinyint NULL DEFAULT NULL,
  `sms_aliyun_access_key_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_access_key_secret` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_sign_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_region_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_enabled` tinyint NULL DEFAULT NULL,
  `sms_tencent_secret_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_secret_key` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_sign_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_template_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_sdk_app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for verification_email_settings
-- ----------------------------
DROP TABLE IF EXISTS `verification_email_settings`;
CREATE TABLE `verification_email_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email_enabled` tinyint NULL DEFAULT NULL,
  `email_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_port` int NULL DEFAULT NULL,
  `email_username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_from` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_ssl` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_page_settings
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_page_settings`;
CREATE TABLE `sensitive_page_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `page_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enabled` tinyint NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `page_key`(`page_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_settings
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_settings`;
CREATE TABLE `sensitive_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` tinyint NOT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_words
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `enabled` tinyint NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `word`(`word` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for storage_settings
-- ----------------------------
DROP TABLE IF EXISTS `storage_settings`;
CREATE TABLE `storage_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `access_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `custom_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `path_prefix` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `public_read` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu_items
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_items`;
CREATE TABLE `sys_menu_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `node_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `route_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `title_zh_cn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title_en_us` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hidden` tinyint NOT NULL DEFAULT 0,
  `keep_alive` tinyint NOT NULL DEFAULT 1,
  `frame_src` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `frame_blank` tinyint NOT NULL DEFAULT 0,
  `enabled` tinyint NOT NULL DEFAULT 1,
  `required_modules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `require_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `require_permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `order_no` int NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `actions` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_menu_route_name`(`route_name` ASC) USING BTREE,
  INDEX `idx_sys_menu_parent_order`(`parent_id` ASC, `order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1045 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_brand_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_brand_settings`;
CREATE TABLE `ui_brand_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `website_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `logo_expanded_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `logo_collapsed_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `favicon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `qr_code_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_footer_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_footer_settings`;
CREATE TABLE `ui_footer_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `footer_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `footer_icp` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `copyright_start_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_layout_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_layout_settings`;
CREATE TABLE `ui_layout_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `default_home` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `show_footer` tinyint NULL DEFAULT NULL,
  `is_sidebar_compact` tinyint NULL DEFAULT NULL,
  `show_breadcrumb` tinyint NULL DEFAULT NULL,
  `menu_auto_collapsed` tinyint NULL DEFAULT NULL,
  `layout` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `split_menu` tinyint NULL DEFAULT NULL,
  `side_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_footer_aside` tinyint NULL DEFAULT NULL,
  `is_sidebar_fixed` tinyint NULL DEFAULT NULL,
  `is_header_fixed` tinyint NULL DEFAULT NULL,
  `is_use_tabs_router` tinyint NULL DEFAULT NULL,
  `show_header` tinyint NULL DEFAULT NULL,
  `header_github_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `header_help_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_legal_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_legal_settings`;
CREATE TABLE `ui_legal_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `privacy_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_login_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_login_settings`;
CREATE TABLE `ui_login_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `login_bg_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `allow_multi_device_login` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_system_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_system_settings`;
CREATE TABLE `ui_system_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `log_retention_days` int NULL DEFAULT NULL,
  `ai_assistant_enabled` tinyint NULL DEFAULT NULL,
  `maintenance_enabled` tinyint NULL DEFAULT NULL,
  `maintenance_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for ui_theme_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_theme_settings`;
CREATE TABLE `ui_theme_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auto_theme` tinyint NULL DEFAULT NULL,
  `light_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dark_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `brand_theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sort` int NOT NULL DEFAULT 0,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_dict_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------
  (2001, 1, '性别', 'gender', 1, '性别选项', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2002, 2, '证件-类型', 'id_document_type', 1, '证件类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2004, 4, '地址-区县', 'address_district', 1, '地址区县', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2005, 5, '公告-类型', 'announcement_type', 1, '公告类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2006, 6, '公告-优先级', 'announcement_priority', 1, '公告优先级', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2007, 7, '公告-状态', 'announcement_status', 1, '公告状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2008, 8, '通知-类型', 'notification_type', 1, '通知类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2009, 9, '通知-优先级', 'notification_priority', 1, '通知优先级', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2010, 10, '通知-状态', 'notification_status', 1, '通知状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2011, 11, '消息-优先级', 'message_quality', 1, '消息优先级', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2012, 12, '日志-操作类型', 'log_action', 1, '操作日志类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2013, 13, '用户-状态', 'user_status', 1, '用户状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2014, 14, '机构-类型', 'org_type', 1, '机构类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2015, 15, '机构-状态', 'org_status', 1, '机构状态', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2016, 16, '菜单-节点类型', 'menu_node_type', 1, '菜单节点类型', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2017, 17, '菜单-权限动作', 'menu_action', 1, '菜单权限动作', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2018, 18, '存储-提供商', 'storage_provider', 1, '存储提供商', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2019, 19, '短信-通道', 'sms_provider', 1, '短信通道', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2020, 20, 'AI-厂商', 'ai_vendor', 1, 'AI厂商', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Table structure for sys_di_gender_de9feaea
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_gender_de9feaea`;
CREATE TABLE `sys_di_gender_de9feaea`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_gender_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2101, 1, '男', 'male', 'string', 1, 'success', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2102, 2, '女', 'female', 'string', 1, 'danger', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2103, 3, '未知', 'unknown', 'string', 1, 'warning', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Table structure for sys_di_id_document_ty_e77fcd76
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_id_document_ty_e77fcd76`;
CREATE TABLE `sys_di_id_document_ty_e77fcd76`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_id_document_ty_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2201, 1, '居民身份证', 'resident_id_card', 'string', 1, 'success', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2202, 2, '护照', 'passport', 'string', 1, 'warning', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Table structure for sys_di_address_distri_a4210592
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_address_distri_a4210592`;
CREATE TABLE `sys_di_address_distri_a4210592`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_address_distri_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2401, 1, '南山区', '南山区', 'string', 1, NULL, '广东省', '深圳市', '南山区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2402, 2, '福田区', '福田区', 'string', 1, NULL, '广东省', '深圳市', '福田区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2403, 3, '宝安区', '宝安区', 'string', 1, NULL, '广东省', '深圳市', '宝安区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2404, 4, '龙岗区', '龙岗区', 'string', 1, NULL, '广东省', '深圳市', '龙岗区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2405, 5, '天河区', '天河区', 'string', 1, NULL, '广东省', '广州市', '天河区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2406, 6, '越秀区', '越秀区', 'string', 1, NULL, '广东省', '广州市', '越秀区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2407, 7, '黄浦区', '黄浦区', 'string', 1, NULL, '上海市', '上海市', '黄浦区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2408, 8, '徐汇区', '徐汇区', 'string', 1, NULL, '上海市', '上海市', '徐汇区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2409, 9, '朝阳区', '朝阳区', 'string', 1, NULL, '北京市', '北京市', '朝阳区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2410, 10, '海淀区', '海淀区', 'string', 1, NULL, '北京市', '北京市', '海淀区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2411, 11, '西湖区', '西湖区', 'string', 1, NULL, '浙江省', '杭州市', '西湖区', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2412, 12, '滨江区', '滨江区', 'string', 1, NULL, '浙江省', '杭州市', '滨江区', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Shared sys_di_* table template
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_announcement_t_de30e733`;
CREATE TABLE `sys_di_announcement_t_de30e733`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_announcement_t_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2501, 1, '公告', 'announcement', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_announcement_p_ef193e74`;
CREATE TABLE `sys_di_announcement_p_ef193e74`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_announcement_p_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2511, 1, '高', 'high', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2512, 2, '中', 'middle', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2513, 3, '低', 'low', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_announcement_s_f91c61fe`;
CREATE TABLE `sys_di_announcement_s_f91c61fe`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_announcement_s_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2521, 1, '草稿', 'draft', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2522, 2, '已发布', 'published', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2523, 3, '已撤回', 'withdrawn', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_notification_t_301379fe`;
CREATE TABLE `sys_di_notification_t_301379fe`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_notification_t_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2601, 1, '通知', 'notification', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_notification_p_d682ffca`;
CREATE TABLE `sys_di_notification_p_d682ffca`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_notification_p_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2611, 1, '高', 'high', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2612, 2, '中', 'middle', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2613, 3, '低', 'low', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_notification_s_32716fa4`;
CREATE TABLE `sys_di_notification_s_32716fa4`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_notification_s_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2621, 1, '草稿', 'draft', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2622, 2, '已发布', 'published', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2623, 3, '已撤回', 'withdrawn', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_message_qualit_6ab11001`;
CREATE TABLE `sys_di_message_qualit_6ab11001`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_message_qualit_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2701, 1, '高', 'high', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2702, 2, '中', 'middle', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2703, 3, '低', 'low', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_log_action_8705ceac`;
CREATE TABLE `sys_di_log_action_8705ceac`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_log_action_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2801, 1, '登录', 'LOGIN', 'string', 1, 'primary', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2802, 2, '新增', 'CREATE', 'string', 1, 'success', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2803, 3, '修改', 'UPDATE', 'string', 1, 'warning', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2804, 4, '删除', 'DELETE', 'string', 1, 'danger', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_user_status_e4abeb4f`;
CREATE TABLE `sys_di_user_status_e4abeb4f`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_user_status_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (2901, 1, '正常', '1', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2902, 2, '停用', '0', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_org_type_5a06609e`;
CREATE TABLE `sys_di_org_type_5a06609e`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_org_type_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3001, 1, '单位', 'UNIT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3002, 2, '部门', 'DEPARTMENT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3003, 3, '科室', 'SECTION', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3004, 4, '班组', 'TEAM', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3005, 5, '用户', 'USER', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_org_status_adc56f2a`;
CREATE TABLE `sys_di_org_status_adc56f2a`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_org_status_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3011, 1, '正常', '1', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3012, 2, '停用', '0', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_menu_node_type_83d1630e`;
CREATE TABLE `sys_di_menu_node_type_83d1630e`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_menu_node_type_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3101, 1, '目录', 'DIR', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3102, 2, '页面', 'PAGE', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3103, 3, '按钮', 'BTN', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_menu_action_6118cb39`;
CREATE TABLE `sys_di_menu_action_6118cb39`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_menu_action_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3201, 1, '查询', 'query', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3202, 2, '新增', 'create', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3203, 3, '修改', 'update', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3204, 4, '删除', 'delete', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_storage_provid_f9bb387a`;
CREATE TABLE `sys_di_storage_provid_f9bb387a`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_storage_provid_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3301, 1, '本地存储', 'LOCAL', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3302, 2, '阿里云-OSS', 'ALIYUN', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3303, 3, '腾讯云-COS', 'TENCENT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_sms_provider_d65c9878`;
CREATE TABLE `sys_di_sms_provider_d65c9878`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_sms_provider_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3401, 1, '阿里云短信服务', 'aliyun', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3402, 2, '腾讯云短信服务', 'tencent', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_ai_vendor_f0a49eba`;
CREATE TABLE `sys_di_ai_vendor_f0a49eba`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_ai_vendor_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
  (3501, 1, 'OpenAI / 兼容', 'OPENAI', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3502, 2, 'Azure OpenAI', 'AZURE_OPENAI', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3503, 3, 'DeepSeek', 'DEEPSEEK', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3504, 4, '月之暗面 / Moonshot', 'MOONSHOT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3505, 5, '通义千问 (兼容模式)', 'QWEN', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3506, 6, 'Ollama 本地部署', 'OLLAMA', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Table structure for user_parameters
-- ----------------------------
DROP TABLE IF EXISTS `user_parameters`;
CREATE TABLE `user_parameters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `param_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `user_id` bigint NOT NULL,
  `role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`user_id`, `role`) USING BTREE,
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `entity` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `leader` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `join_day` date NULL DEFAULT NULL,
  `team` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province_id` int NULL DEFAULT NULL,
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city_id` int NULL DEFAULT NULL,
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district_id` int NULL DEFAULT NULL,
  `district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `town_id` int NULL DEFAULT NULL,
  `town` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `street_id` int NULL DEFAULT NULL,
  `street` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zip_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_valid_from` date NULL DEFAULT NULL,
  `id_valid_to` date NULL DEFAULT NULL,
  `guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account`(`account` ASC) USING BTREE,
  INDEX `idx_users_mobile`(`mobile` ASC) USING BTREE,
  INDEX `idx_users_account_name`(`account` ASC, `name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 149 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------
  (1, 'admin', '管理员', '$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu', '+86 13800000000', 'admin@qq.com', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827', 1, NOW(), NOW()),
  (3, 'bob', 'Bob', '$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu', '+86 13923456789', 'bob@tencent.com', '700e0805-c571-4e6a-873b-00899a32ede2', 1, NOW(), NOW());

-- ----------------------------
-- Table structure for areas
-- ----------------------------
DROP TABLE IF EXISTS `areas`;
CREATE TABLE `areas`  (
  `id` int NOT NULL,
  `parent_id` int NOT NULL DEFAULT 0,
  `name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `zip_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `level` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent` (`parent_id`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for org_units
-- ----------------------------
DROP TABLE IF EXISTS `org_units`;
CREATE TABLE `org_units`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `short_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for org_unit_leaders
-- ----------------------------
DROP TABLE IF EXISTS `org_unit_leaders`;
CREATE TABLE `org_unit_leaders`  (
  `org_unit_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`org_unit_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_org_units
-- ----------------------------
DROP TABLE IF EXISTS `user_org_units`;
CREATE TABLE `user_org_units`  (
  `user_id` bigint NOT NULL,
  `org_unit_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `org_unit_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_departments
-- ----------------------------
DROP TABLE IF EXISTS `user_departments`;
CREATE TABLE `user_departments`  (
  `user_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `department_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for module_registry
-- ----------------------------
DROP TABLE IF EXISTS `module_registry`;
CREATE TABLE `module_registry`  (
  `module_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enabled` tinyint NULL DEFAULT 1,
  `install_state` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `installed_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`module_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Table structure for watermark_settings
-- ----------------------------
DROP TABLE IF EXISTS `watermark_settings`;
CREATE TABLE `watermark_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `opacity` double NOT NULL,
  `size` int NOT NULL,
  `gap_x` int NOT NULL,
  `gap_y` int NOT NULL,
  `rotate` int NOT NULL,
  `enabled` tinyint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Data consistency cleanup
-- ----------------------------
DELETE ur
FROM user_roles ur
LEFT JOIN users u ON u.id = ur.user_id
WHERE u.id IS NULL;

DELETE uou
FROM user_org_units uou
LEFT JOIN users u ON u.id = uou.user_id
WHERE u.id IS NULL;

DELETE ud
FROM user_departments ud
LEFT JOIN users u ON u.id = ud.user_id
WHERE u.id IS NULL;

DELETE oul
FROM org_unit_leaders oul
LEFT JOIN users u ON u.id = oul.user_id
WHERE u.id IS NULL;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table/Column comments
-- ----------------------------
ALTER TABLE `ai_provider_settings` COMMENT = '表 ai_provider_settings';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `vendor` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'vendor';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'base_url';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `endpoint_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'endpoint_path';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'model';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `api_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api_key';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `api_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api_version';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `temperature` double NULL DEFAULT NULL COMMENT 'temperature';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `max_tokens` int NULL DEFAULT NULL COMMENT 'max_tokens';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `is_default` tinyint NULL DEFAULT NULL COMMENT 'is_default';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `enabled` tinyint NULL DEFAULT NULL COMMENT 'enabled';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `extra_headers` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'extra_headers';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'remark';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `last_test_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'last_test_status';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `last_test_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'last_test_message';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `last_tested_at` datetime NULL DEFAULT NULL COMMENT 'last_tested_at';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `announcements` COMMENT = '表 announcements';
ALTER TABLE `announcements` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `announcements` MODIFY COLUMN `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'title';
ALTER TABLE `announcements` MODIFY COLUMN `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'summary';
ALTER TABLE `announcements` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `announcements` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'type';
ALTER TABLE `announcements` MODIFY COLUMN `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'priority';
ALTER TABLE `announcements` MODIFY COLUMN `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'status';
ALTER TABLE `announcements` MODIFY COLUMN `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cover_url';
ALTER TABLE `announcements` MODIFY COLUMN `publish_at` datetime NULL DEFAULT NULL COMMENT 'publish_at';
ALTER TABLE `announcements` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `announcements` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `announcements` MODIFY COLUMN `created_by_id` bigint NULL DEFAULT NULL COMMENT 'created_by_id';
ALTER TABLE `announcements` MODIFY COLUMN `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'created_by_name';
ALTER TABLE `announcements` MODIFY COLUMN `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_url';
ALTER TABLE `announcements` MODIFY COLUMN `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_name';
ALTER TABLE `announcements` MODIFY COLUMN `is_broadcasted` tinyint NOT NULL DEFAULT 0 COMMENT 'is_broadcasted';
ALTER TABLE `file_resources` COMMENT = '表 file_resources';
ALTER TABLE `file_resources` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `file_resources` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `file_resources` MODIFY COLUMN `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'file_name';
ALTER TABLE `file_resources` MODIFY COLUMN `suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'suffix';
ALTER TABLE `file_resources` MODIFY COLUMN `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'file_url';
ALTER TABLE `file_resources` MODIFY COLUMN `created_by_id` bigint NULL DEFAULT NULL COMMENT 'created_by_id';
ALTER TABLE `file_resources` MODIFY COLUMN `created_by_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'created_by_name';
ALTER TABLE `file_resources` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `file_resources` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `messages` COMMENT = '表 messages';
ALTER TABLE `messages` MODIFY COLUMN `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'id';
ALTER TABLE `messages` MODIFY COLUMN `to_user_id` bigint NOT NULL COMMENT 'to_user_id';
ALTER TABLE `messages` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `messages` MODIFY COLUMN `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'type';
ALTER TABLE `messages` MODIFY COLUMN `status` tinyint NOT NULL COMMENT 'status';
ALTER TABLE `messages` MODIFY COLUMN `collected` tinyint NOT NULL COMMENT 'collected';
ALTER TABLE `messages` MODIFY COLUMN `quality` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'quality';
ALTER TABLE `messages` MODIFY COLUMN `created_at` datetime NOT NULL COMMENT 'created_at';
ALTER TABLE `notifications` COMMENT = '表 notifications';
ALTER TABLE `notifications` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `notifications` MODIFY COLUMN `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'title';
ALTER TABLE `notifications` MODIFY COLUMN `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'summary';
ALTER TABLE `notifications` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `notifications` MODIFY COLUMN `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'priority';
ALTER TABLE `notifications` MODIFY COLUMN `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'status';
ALTER TABLE `notifications` MODIFY COLUMN `publish_at` datetime NULL DEFAULT NULL COMMENT 'publish_at';
ALTER TABLE `notifications` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `notifications` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `notifications` MODIFY COLUMN `created_by_id` bigint NULL DEFAULT NULL COMMENT 'created_by_id';
ALTER TABLE `notifications` MODIFY COLUMN `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'created_by_name';
ALTER TABLE `notifications` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'type';
ALTER TABLE `notifications` MODIFY COLUMN `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cover_url';
ALTER TABLE `notifications` MODIFY COLUMN `attachment_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_url';
ALTER TABLE `notifications` MODIFY COLUMN `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_name';
ALTER TABLE `operation_logs` COMMENT = '表 operation_logs';
ALTER TABLE `operation_logs` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `operation_logs` MODIFY COLUMN `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'action';
ALTER TABLE `operation_logs` MODIFY COLUMN `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'module';
ALTER TABLE `operation_logs` MODIFY COLUMN `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'detail';
ALTER TABLE `operation_logs` MODIFY COLUMN `user_id` bigint NULL DEFAULT NULL COMMENT 'user_id';
ALTER TABLE `operation_logs` MODIFY COLUMN `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'account';
ALTER TABLE `operation_logs` MODIFY COLUMN `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ip_address';
ALTER TABLE `operation_logs` MODIFY COLUMN `device_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'device_model';
ALTER TABLE `operation_logs` MODIFY COLUMN `os` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'os';
ALTER TABLE `operation_logs` MODIFY COLUMN `browser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'browser';
ALTER TABLE `operation_logs` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `operation_logs` MODIFY COLUMN `user_guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'user_guid';
ALTER TABLE `role_menus` COMMENT = '表 role_menus';
ALTER TABLE `role_menus` MODIFY COLUMN `role_id` bigint NOT NULL COMMENT 'role_id';
ALTER TABLE `role_menus` MODIFY COLUMN `menu_id` bigint NOT NULL COMMENT 'menu_id';
ALTER TABLE `role_permissions` COMMENT = '表 role_permissions';
ALTER TABLE `role_permissions` MODIFY COLUMN `role_id` bigint NOT NULL COMMENT 'role_id';
ALTER TABLE `role_permissions` MODIFY COLUMN `permission` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'permission';
ALTER TABLE `roles` COMMENT = '表 roles';
ALTER TABLE `roles` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `roles` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `roles` MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'description';
ALTER TABLE `security_captcha_settings` COMMENT = '表 security_captcha_settings';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `captcha_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'captcha_type';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `drag_captcha_width` int NULL DEFAULT NULL COMMENT 'drag_captcha_width';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `drag_captcha_height` int NULL DEFAULT NULL COMMENT 'drag_captcha_height';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `drag_captcha_threshold` int NULL DEFAULT NULL COMMENT 'drag_captcha_threshold';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `image_captcha_length` int NULL DEFAULT NULL COMMENT 'image_captcha_length';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `image_captcha_noise_lines` int NULL DEFAULT NULL COMMENT 'image_captcha_noise_lines';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `captcha_enabled` tinyint NULL DEFAULT NULL COMMENT 'captcha_enabled';
ALTER TABLE `security_password_policy` COMMENT = '表 security_password_policy';
ALTER TABLE `security_password_policy` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_min_length` int NULL DEFAULT NULL COMMENT 'password_min_length';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_require_uppercase` tinyint NULL DEFAULT NULL COMMENT 'password_require_uppercase';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_require_lowercase` tinyint NULL DEFAULT NULL COMMENT 'password_require_lowercase';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_require_special` tinyint NULL DEFAULT NULL COMMENT 'password_require_special';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_allow_sequential` tinyint NULL DEFAULT NULL COMMENT 'password_allow_sequential';
ALTER TABLE `security_token_settings` COMMENT = '表 security_token_settings';
ALTER TABLE `security_token_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `security_token_settings` MODIFY COLUMN `session_timeout_minutes` int NULL DEFAULT NULL COMMENT 'session_timeout_minutes';
ALTER TABLE `security_token_settings` MODIFY COLUMN `token_timeout_minutes` int NULL DEFAULT NULL COMMENT 'token_timeout_minutes';
ALTER TABLE `security_token_settings` MODIFY COLUMN `token_refresh_grace_minutes` int NULL DEFAULT NULL COMMENT 'token_refresh_grace_minutes';
ALTER TABLE `security_token_settings` MODIFY COLUMN `allow_url_token_param` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'allow_url_token_param';
ALTER TABLE `sensitive_page_settings` COMMENT = '表 sensitive_page_settings';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'page_key';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `page_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'page_name';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `sensitive_settings` COMMENT = '表 sensitive_settings';
ALTER TABLE `sensitive_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sensitive_settings` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
ALTER TABLE `sensitive_settings` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `sensitive_words` COMMENT = '表 sensitive_words';
ALTER TABLE `sensitive_words` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sensitive_words` MODIFY COLUMN `word` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'word';
ALTER TABLE `sensitive_words` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
ALTER TABLE `sensitive_words` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `sensitive_words` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `storage_settings` COMMENT = '表 storage_settings';
ALTER TABLE `storage_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `storage_settings` MODIFY COLUMN `provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'provider';
ALTER TABLE `storage_settings` MODIFY COLUMN `bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bucket';
ALTER TABLE `storage_settings` MODIFY COLUMN `region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'region';
ALTER TABLE `storage_settings` MODIFY COLUMN `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'endpoint';
ALTER TABLE `storage_settings` MODIFY COLUMN `access_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'access_key';
ALTER TABLE `storage_settings` MODIFY COLUMN `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'secret_key';
ALTER TABLE `storage_settings` MODIFY COLUMN `custom_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'custom_domain';
ALTER TABLE `storage_settings` MODIFY COLUMN `path_prefix` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'path_prefix';
ALTER TABLE `storage_settings` MODIFY COLUMN `public_read` tinyint NULL DEFAULT NULL COMMENT 'public_read';
ALTER TABLE `sys_menu_items` COMMENT = '表 sys_menu_items';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `parent_id` bigint NULL DEFAULT NULL COMMENT 'parent_id';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `node_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'node_type';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'path';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `route_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'route_name';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'component';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'redirect';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `title_zh_cn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'title_zh_cn';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `title_en_us` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'title_en_us';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'icon';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `hidden` tinyint NOT NULL DEFAULT 0 COMMENT 'hidden';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `keep_alive` tinyint NOT NULL DEFAULT 1 COMMENT 'keep_alive';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `frame_src` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'frame_src';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `frame_blank` tinyint NOT NULL DEFAULT 0 COMMENT 'frame_blank';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `enabled` tinyint NOT NULL DEFAULT 1 COMMENT 'enabled';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `require_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'require_role';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `require_permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'require_permission';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `order_no` int NOT NULL DEFAULT 0 COMMENT 'order_no';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `version` int NOT NULL DEFAULT 0 COMMENT 'version';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `actions` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'actions';
ALTER TABLE `ui_brand_settings` COMMENT = '表 ui_brand_settings';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `website_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'website_name';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'app_version';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `logo_expanded_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'logo_expanded_url';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `logo_collapsed_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'logo_collapsed_url';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `favicon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'favicon_url';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `qr_code_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'qr_code_url';
ALTER TABLE `ui_footer_settings` COMMENT = '表 ui_footer_settings';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `footer_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'footer_company';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `footer_icp` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'footer_icp';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `copyright_start_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'copyright_start_year';
ALTER TABLE `ui_layout_settings` COMMENT = '表 ui_layout_settings';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `default_home` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'default_home';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `show_footer` tinyint NULL DEFAULT NULL COMMENT 'show_footer';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_sidebar_compact` tinyint NULL DEFAULT NULL COMMENT 'is_sidebar_compact';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `show_breadcrumb` tinyint NULL DEFAULT NULL COMMENT 'show_breadcrumb';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `menu_auto_collapsed` tinyint NULL DEFAULT NULL COMMENT 'menu_auto_collapsed';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `layout` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'layout';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `split_menu` tinyint NULL DEFAULT NULL COMMENT 'split_menu';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `side_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'side_mode';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_footer_aside` tinyint NULL DEFAULT NULL COMMENT 'is_footer_aside';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_sidebar_fixed` tinyint NULL DEFAULT NULL COMMENT 'is_sidebar_fixed';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_header_fixed` tinyint NULL DEFAULT NULL COMMENT 'is_header_fixed';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_use_tabs_router` tinyint NULL DEFAULT NULL COMMENT 'is_use_tabs_router';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `show_header` tinyint NULL DEFAULT NULL COMMENT 'show_header';
ALTER TABLE `ui_legal_settings` COMMENT = '表 ui_legal_settings';
ALTER TABLE `ui_legal_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_legal_settings` MODIFY COLUMN `user_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'user_agreement';
ALTER TABLE `ui_legal_settings` MODIFY COLUMN `privacy_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'privacy_agreement';
ALTER TABLE `ui_login_settings` COMMENT = '表 ui_login_settings';
ALTER TABLE `ui_login_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_login_settings` MODIFY COLUMN `login_bg_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'login_bg_url';
ALTER TABLE `ui_login_settings` MODIFY COLUMN `allow_multi_device_login` tinyint NULL DEFAULT NULL COMMENT 'allow_multi_device_login';
ALTER TABLE `ui_system_settings` COMMENT = '表 ui_system_settings';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `log_retention_days` int NULL DEFAULT NULL COMMENT 'log_retention_days';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `ai_assistant_enabled` tinyint NULL DEFAULT NULL COMMENT 'ai_assistant_enabled';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `maintenance_enabled` tinyint NULL DEFAULT NULL COMMENT 'maintenance_enabled';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `maintenance_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'maintenance_message';
ALTER TABLE `ui_theme_settings` COMMENT = '表 ui_theme_settings';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `auto_theme` tinyint NULL DEFAULT NULL COMMENT 'auto_theme';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `light_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'light_start_time';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `dark_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'dark_start_time';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mode';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `brand_theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'brand_theme';
ALTER TABLE `user_parameters` COMMENT = '表 user_parameters';
ALTER TABLE `user_parameters` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `user_parameters` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_parameters` MODIFY COLUMN `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'param_key';
ALTER TABLE `user_parameters` MODIFY COLUMN `param_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'param_value';
ALTER TABLE `user_parameters` MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'description';
ALTER TABLE `user_parameters` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `user_parameters` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `user_roles` COMMENT = '表 user_roles';
ALTER TABLE `user_roles` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_roles` MODIFY COLUMN `role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'role';
ALTER TABLE `users` COMMENT = '表 users';
ALTER TABLE `users` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `users` MODIFY COLUMN `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'account';
ALTER TABLE `users` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `users` MODIFY COLUMN `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'password_hash';
ALTER TABLE `users` MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mobile';
ALTER TABLE `users` MODIFY COLUMN `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'email';
ALTER TABLE `users` MODIFY COLUMN `seat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'seat';
ALTER TABLE `users` MODIFY COLUMN `entity` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'entity';
ALTER TABLE `users` MODIFY COLUMN `leader` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'leader';
ALTER TABLE `users` MODIFY COLUMN `position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'position';
ALTER TABLE `users` MODIFY COLUMN `join_day` date NULL DEFAULT NULL COMMENT 'join_day';
ALTER TABLE `users` MODIFY COLUMN `team` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'team';
ALTER TABLE `users` MODIFY COLUMN `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'gender';
ALTER TABLE `users` MODIFY COLUMN `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'nickname';
ALTER TABLE `users` MODIFY COLUMN `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'province';
ALTER TABLE `users` MODIFY COLUMN `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'city';
ALTER TABLE `users` MODIFY COLUMN `district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'district';
ALTER TABLE `users` MODIFY COLUMN `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'address';
ALTER TABLE `users` MODIFY COLUMN `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'introduction';
ALTER TABLE `users` MODIFY COLUMN `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'avatar';
ALTER TABLE `users` MODIFY COLUMN `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'tags';
ALTER TABLE `users` MODIFY COLUMN `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'id_card';
ALTER TABLE `users` MODIFY COLUMN `id_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'id_type';
ALTER TABLE `users` MODIFY COLUMN `id_valid_from` date NULL DEFAULT NULL COMMENT 'id_valid_from';
ALTER TABLE `users` MODIFY COLUMN `id_valid_to` date NULL DEFAULT NULL COMMENT 'id_valid_to';
ALTER TABLE `users` MODIFY COLUMN `guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'guid';
ALTER TABLE `users` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT 'status';
ALTER TABLE `users` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `users` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `org_units` COMMENT = '表 org_units';
ALTER TABLE `org_units` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `org_units` MODIFY COLUMN `parent_id` bigint NULL DEFAULT NULL COMMENT 'parent_id';
ALTER TABLE `org_units` MODIFY COLUMN `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `org_units` MODIFY COLUMN `short_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'short_name';
ALTER TABLE `org_units` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'type';
ALTER TABLE `org_units` MODIFY COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT 'sort_order';
ALTER TABLE `org_units` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT 'status';
ALTER TABLE `org_units` MODIFY COLUMN `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'phone';
ALTER TABLE `org_units` MODIFY COLUMN `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'email';
ALTER TABLE `org_units` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `org_units` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `org_unit_leaders` COMMENT = '表 org_unit_leaders';
ALTER TABLE `org_unit_leaders` MODIFY COLUMN `org_unit_id` bigint NOT NULL COMMENT 'org_unit_id';
ALTER TABLE `org_unit_leaders` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_org_units` COMMENT = '表 user_org_units';
ALTER TABLE `user_org_units` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_org_units` MODIFY COLUMN `org_unit_id` bigint NOT NULL COMMENT 'org_unit_id';
ALTER TABLE `module_registry` COMMENT = '表 module_registry';
ALTER TABLE `module_registry` MODIFY COLUMN `module_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'module_key';
ALTER TABLE `module_registry` MODIFY COLUMN `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'name';
ALTER TABLE `module_registry` MODIFY COLUMN `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'version';
ALTER TABLE `module_registry` MODIFY COLUMN `enabled` tinyint NULL DEFAULT 1 COMMENT 'enabled';
ALTER TABLE `module_registry` MODIFY COLUMN `install_state` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'install_state';
ALTER TABLE `module_registry` MODIFY COLUMN `installed_at` datetime NULL DEFAULT NULL COMMENT 'installed_at';
ALTER TABLE `sys_dict` COMMENT = '表 sys_dict';
ALTER TABLE `sys_dict` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sys_dict` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `sys_dict` MODIFY COLUMN `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'code';
ALTER TABLE `sys_dict` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT 'status';
ALTER TABLE `sys_dict` MODIFY COLUMN `sort` int NOT NULL DEFAULT 0 COMMENT 'sort';
ALTER TABLE `sys_dict` MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'remark';
ALTER TABLE `sys_dict` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `sys_dict` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `watermark_settings` COMMENT = '表 watermark_settings';
ALTER TABLE `watermark_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `watermark_settings` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'type';
ALTER TABLE `watermark_settings` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'content';
ALTER TABLE `watermark_settings` MODIFY COLUMN `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'image_url';
ALTER TABLE `watermark_settings` MODIFY COLUMN `opacity` double NOT NULL COMMENT 'opacity';
ALTER TABLE `watermark_settings` MODIFY COLUMN `size` int NOT NULL COMMENT 'size';
ALTER TABLE `watermark_settings` MODIFY COLUMN `gap_x` int NOT NULL COMMENT 'gap_x';
ALTER TABLE `watermark_settings` MODIFY COLUMN `gap_y` int NOT NULL COMMENT 'gap_y';
ALTER TABLE `watermark_settings` MODIFY COLUMN `rotate` int NOT NULL COMMENT 'rotate';
ALTER TABLE `watermark_settings` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';

-- ----------------------------
SET FOREIGN_KEY_CHECKS = 1;
