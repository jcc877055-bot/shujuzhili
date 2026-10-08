-- MySQL 8.0.16+; UTC; Flyway is the only schema version manager.
-- D17-T01: 治理组织
-- parent_id→org.id。停用组织禁止新增派单，不删除历史。组织树禁止循环。
CREATE TABLE `org` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `parent_id` BIGINT UNSIGNED NULL,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_1` (`code`),
  KEY `idx_org_2` (`parent_id`),
  CONSTRAINT `ck_org_status` CHECK (`status` IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T01 治理组织';

-- D17-T02: 用户身份
-- org_id→org.id。改角色或停用时增加permission_version，凭证失效。
CREATE TABLE `user_account` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `username` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `password_hash` VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
  `display_name` VARCHAR(64) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `permission_version` INT UNSIGNED NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_account_1` (`org_id`,`username`),
  KEY `idx_user_account_2` (`org_id`,`status`),
  CONSTRAINT `ck_user_account_status` CHECK (`status` IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T02 用户身份';

-- D17-T03: 业务角色
-- 治理数据管理员不自动获得系统授权维护权限，分配auth:grant须单独审查。
CREATE TABLE `role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_1` (`code`),
  CONSTRAINT `ck_role_status` CHECK (`status` IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T03 业务角色';

-- D17-T04: 动作权限
-- 权限码在代码中作为常量引用，禁止按页面可见性推定权限。
CREATE TABLE `permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `code` VARCHAR(96) COLLATE utf8mb4_bin NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `resource` VARCHAR(64) NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_1` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T04 动作权限';

-- D17-T05: 账号角色关联
-- user_id→user_account.id；role_id→role.id。撤销关联保留授权审计。
CREATE TABLE `user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `user_id` BIGINT UNSIGNED NOT NULL,
  `role_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role_1` (`user_id`,`role_id`),
  KEY `idx_user_role_2` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T05 账号角色关联';

-- D17-T06: 角色动作关联
-- role_id→role.id；permission_id→permission.id。
CREATE TABLE `role_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `role_id` BIGINT UNSIGNED NOT NULL,
  `permission_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission_1` (`role_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T06 角色动作关联';

-- D17-T07: 组织授权范围
-- 两外键分别指向user_account与org。多个角色范围不自动提升到全局。
CREATE TABLE `user_scope` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `user_id` BIGINT UNSIGNED NOT NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `include_descendants` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `ck_user_scope_include_descendants` CHECK (`include_descendants` IN (0,1)),
  `scope_type` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_scope_1` (`user_id`,`org_id`,`scope_type`),
  CONSTRAINT `ck_user_scope_scope_type` CHECK (`scope_type` IN ('ORG','ASSIGNED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T07 组织授权范围';

-- D17-T08: 登录会话
-- user_id→user_account.id。仅保存随机令牌摘要，令牌本体不落日志。
CREATE TABLE `auth_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `token_hash` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `permission_version` INT UNSIGNED NOT NULL,
  `expires_at` DATETIME(3) NOT NULL,
  `revoked_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_auth_session_1` (`token_hash`),
  KEY `idx_auth_session_2` (`user_id`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T08 登录会话';
