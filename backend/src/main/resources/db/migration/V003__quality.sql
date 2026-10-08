-- MySQL 8.0.16+; UTC; Flyway is the only schema version manager.
-- D17-T18: 检测或异常接收批次
-- org_id→org；rule_version_id→rule_version。MANUAL不填写虚构扫描数。自动检测属于候选扩展。
CREATE TABLE `detection_batch` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `batch_no` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `rule_version_id` BIGINT UNSIGNED NOT NULL,
  `source_type` VARCHAR(16) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `scanned_count` BIGINT UNSIGNED NULL,
  `failed_count` BIGINT UNSIGNED NULL,
  `started_at` DATETIME(3) NULL,
  `ended_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_detection_batch_1` (`org_id`,`batch_no`),
  KEY `idx_detection_batch_2` (`rule_version_id`,`created_at`),
  CONSTRAINT `ck_detection_batch_source_type` CHECK (`source_type` IN ('MANUAL','ENGINE')),
  CONSTRAINT `ck_detection_batch_status` CHECK (`status` IN ('REGISTERED','RUNNING','SUCCEEDED','ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T18 检测或异常接收批次';

-- D17-T19: 质量问题候选与闭环对象
-- 各id指向相应实体。field必须属于asset，batch规则与问题规则一致，由应用在同事务校验。候选允许重复接收；人工确认去重需锁issue_identity。
CREATE TABLE `quality_issue` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `asset_id` BIGINT UNSIGNED NOT NULL,
  `field_id` BIGINT UNSIGNED NOT NULL,
  `rule_version_id` BIGINT UNSIGNED NOT NULL,
  `batch_id` BIGINT UNSIGNED NOT NULL,
  `issue_no` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `summary` VARCHAR(1000) NOT NULL,
  `fingerprint` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `status` VARCHAR(24) NOT NULL,
  `locator_json` JSON NOT NULL,
  `related_issue_id` BIGINT UNSIGNED NULL,
  `decision_reason` VARCHAR(1000) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quality_issue_1` (`issue_no`),
  KEY `idx_quality_issue_2` (`org_id`,`status`,`created_at`),
  KEY `idx_quality_issue_3` (`org_id`,`fingerprint`,`status`),
  CONSTRAINT `ck_quality_issue_status` CHECK (`status` IN ('CANDIDATE','CONFIRMED','MERGED','RESOLVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T19 质量问题候选与闭环对象';

-- D17-T20: 未闭环问题唯一占位
-- org_id→org；active_issue_id→quality_issue。确认前UPSERT占位后SELECT FOR UPDATE，解决MySQL没有部分唯一索引的并发去重问题。关闭清空占位但保留历史。
CREATE TABLE `issue_identity` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `fingerprint` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `active_issue_id` BIGINT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_issue_identity_1` (`org_id`,`fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T20 未闭环问题唯一占位';
