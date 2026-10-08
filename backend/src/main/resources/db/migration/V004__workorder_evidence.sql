-- MySQL 8.0.16+; UTC; Flyway is the only schema version manager.
-- D17-T21: 工单聚合根
-- org_id→org；issue_id→quality_issue；assignee_id/closed_by→user_account。valid_pass_id在复检表后补FK。一问题一未关闭工单用生成列active_issue_id=CASE WHEN status<>CLOSED THEN issue_id ELSE NULL END加UK。逾期实时推导不作为状态。
CREATE TABLE `governance_work_order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `issue_id` BIGINT UNSIGNED NOT NULL,
  `order_no` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `assignee_id` BIGINT UNSIGNED NULL,
  `due_at` DATETIME(3) NULL,
  `status` VARCHAR(24) NOT NULL,
  `current_round` INT UNSIGNED NOT NULL DEFAULT 1,
  CONSTRAINT `ck_governance_work_order_current_round` CHECK (`current_round` >= 1),
  `valid_pass_id` BIGINT UNSIGNED NULL,
  `closed_by` BIGINT UNSIGNED NULL,
  `closed_at` DATETIME(3) NULL,
  `coordination_required` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `ck_governance_work_order_coordination_required` CHECK (`coordination_required` IN (0,1)),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_governance_work_order_1` (`order_no`),
  KEY `idx_governance_work_order_2` (`org_id`,`status`,`due_at`),
  KEY `idx_governance_work_order_3` (`assignee_id`,`status`,`updated_at`),
  KEY `idx_governance_work_order_4` (`issue_id`),
  `active_issue_id` BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN `status` <> 'CLOSED' THEN `issue_id` ELSE NULL END) STORED,
  UNIQUE KEY `uk_one_active_order` (`active_issue_id`),
  CONSTRAINT `ck_governance_work_order_status` CHECK (`status` IN ('WAIT_DISPATCH','WAIT_CLAIM','PROCESSING','WAIT_REVIEW','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T21 工单聚合根';

-- D17-T22: 整改提交快照
-- work_order_id→工单；submitted_by→用户；rule_version_id→规则快照。仅成功提交时创建，不预建空记录；FAIL后工单轮次递增，旧记录不可覆盖。
CREATE TABLE `remediation_round` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `work_order_id` BIGINT UNSIGNED NOT NULL,
  `round_no` INT UNSIGNED NOT NULL,
  CONSTRAINT `ck_remediation_round_round_no` CHECK (`round_no` >= 1),
  `submitted_by` BIGINT UNSIGNED NOT NULL,
  `root_cause` TEXT NOT NULL,
  `remediation_text` TEXT NOT NULL,
  `submitted_at` DATETIME(3) NOT NULL,
  `submission_version` BIGINT UNSIGNED NOT NULL,
  `rule_version_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_remediation_round_1` (`work_order_id`,`round_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T22 整改提交快照';

-- D17-T23: 受控材料索引
-- org_id→org；uploaded_by→用户。下载重新授权，禁止真实绝对路径出现在响应。TEMP孤儿材料按受控保留期清理。
CREATE TABLE `evidence_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `uploaded_by` BIGINT UNSIGNED NOT NULL,
  `original_name` VARCHAR(255) NOT NULL,
  `storage_key` VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
  `media_type` VARCHAR(64) NOT NULL,
  `size_bytes` BIGINT UNSIGNED NOT NULL,
  `sha256` CHAR(64) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_evidence_file_1` (`storage_key`),
  KEY `idx_evidence_file_2` (`org_id`,`uploaded_by`,`created_at`),
  CONSTRAINT `ck_evidence_file_status` CHECK (`status` IN ('TEMP','READY','QUARANTINED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T23 受控材料索引';

-- D17-T24: 整改材料关联
-- round_id→remediation_round；evidence_id→evidence_file。材料READY且与工单同组织才可绑定。
CREATE TABLE `round_evidence` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `round_id` BIGINT UNSIGNED NOT NULL,
  `evidence_id` BIGINT UNSIGNED NOT NULL,
  `purpose` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_round_evidence_1` (`round_id`,`evidence_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T24 整改材料关联';
