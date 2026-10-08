-- MySQL 8.0.16+; UTC; Flyway is the only schema version manager.
-- D17-T09: 数据源登记扩展
-- org_id→org.id。真实连接和采集不在当前D09最小实现范围，先登记来源元信息。
CREATE TABLE `data_source` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `source_type` VARCHAR(32) NOT NULL,
  `endpoint_ref` VARCHAR(255) NULL,
  `credential_ref` VARCHAR(128) NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_source_1` (`org_id`,`code`),
  CONSTRAINT `ck_data_source_source_type` CHECK (`source_type` IN ('MYSQL','MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T09 数据源登记扩展';

-- D17-T10: 治理资产
-- org_id→org；source_id→data_source；owner_id→user_account。发布前必须明确有效责任人。
CREATE TABLE `data_asset` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `source_id` BIGINT UNSIGNED NULL,
  `asset_code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `classification` VARCHAR(32) NOT NULL,
  `owner_id` BIGINT UNSIGNED NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_asset_1` (`org_id`,`asset_code`),
  KEY `idx_data_asset_2` (`org_id`,`status`,`owner_id`),
  CONSTRAINT `ck_data_asset_status` CHECK (`status` IN ('DRAFT','PUBLISHED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T10 治理资产';

-- D17-T11: 资产字段
-- asset_id→data_asset.id。代码与源表列名映射必须来自登记元数据白名单。
CREATE TABLE `metadata_field` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `asset_id` BIGINT UNSIGNED NOT NULL,
  `field_code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `display_name` VARCHAR(128) NOT NULL,
  `data_type` VARCHAR(64) NOT NULL,
  `nullable` TINYINT UNSIGNED NOT NULL,
  CONSTRAINT `ck_metadata_field_nullable` CHECK (`nullable` IN (0,1)),
  `sensitive_level` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metadata_field_1` (`asset_id`,`field_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T11 资产字段';

-- D17-T12: 候选业务术语
-- org_id→org.id。术语确认不等同标准发布，D11保留业务口径。
CREATE TABLE `business_term` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `definition` TEXT NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_business_term_1` (`org_id`,`code`),
  CONSTRAINT `ck_business_term_status` CHECK (`status` IN ('CANDIDATE','CONFIRMED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T12 候选业务术语';

-- D17-T13: 标准身份
-- org_id→org；term_id→business_term。标准可关联多个字段，通过桥接表表达。
CREATE TABLE `data_standard` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `term_id` BIGINT UNSIGNED NULL,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_standard_1` (`org_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T13 标准身份';

-- D17-T14: 不可变标准版本
-- standard_id→data_standard。PUBLISHED版本禁止原位修改；新要求创建新版本。
CREATE TABLE `standard_version` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `standard_id` BIGINT UNSIGNED NOT NULL,
  `version_no` INT UNSIGNED NOT NULL,
  CONSTRAINT `ck_standard_version_version_no` CHECK (`version_no` >= 1),
  `definition` JSON NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `published_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_standard_version_1` (`standard_id`,`version_no`),
  CONSTRAINT `ck_standard_version_status` CHECK (`status` IN ('DRAFT','PUBLISHED','RETIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T14 不可变标准版本';

-- D17-T15: 字段标准映射
-- 两外键指向metadata_field和standard_version。解决原设计把标准直接从属于单字段的限制。
CREATE TABLE `field_standard` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `field_id` BIGINT UNSIGNED NOT NULL,
  `standard_version_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_field_standard_1` (`field_id`,`standard_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T15 字段标准映射';

-- D17-T16: 规则身份
-- org_id→org；field_id→metadata_field。跨字段一致性引用在规则版本参数中按白名单记录。
CREATE TABLE `quality_rule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `field_id` BIGINT UNSIGNED NOT NULL,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `rule_type` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quality_rule_1` (`org_id`,`code`),
  KEY `idx_quality_rule_2` (`field_id`),
  CONSTRAINT `ck_quality_rule_rule_type` CHECK (`rule_type` IN ('COMPLETENESS','UNIQUENESS','VALIDITY','CONSISTENCY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T16 规则身份';

-- D17-T17: 不可变规则快照
-- rule_id→quality_rule；standard_version_id→standard_version。闭环采用rule_version_id绑定实际快照。
CREATE TABLE `rule_version` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `rule_id` BIGINT UNSIGNED NOT NULL,
  `version_no` INT UNSIGNED NOT NULL,
  CONSTRAINT `ck_rule_version_version_no` CHECK (`version_no` >= 1),
  `standard_version_id` BIGINT UNSIGNED NULL,
  `parameters` JSON NOT NULL,
  `content_hash` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_version_1` (`rule_id`,`version_no`),
  CONSTRAINT `ck_rule_version_status` CHECK (`status` IN ('DRAFT','PUBLISHED','RETIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T17 不可变规则快照';
