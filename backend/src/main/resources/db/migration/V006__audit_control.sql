-- MySQL 8.0.16+; UTC; Flyway is the only schema version manager.
-- D17-T27: 责任转派申请
-- 各FK到工单或用户。转派审批权限待确认；禁止在待复检存在有效任务时直接改责任。
CREATE TABLE `transfer_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `created_by` BIGINT UNSIGNED NULL,
  `work_order_id` BIGINT UNSIGNED NOT NULL,
  `requested_by` BIGINT UNSIGNED NOT NULL,
  `target_user_id` BIGINT UNSIGNED NOT NULL,
  `reason` VARCHAR(1000) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `decided_by` BIGINT UNSIGNED NULL,
  `decided_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_transfer_request_1` (`work_order_id`,`status`),
  CONSTRAINT `ck_transfer_request_status` CHECK (`status` IN ('PENDING','APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T27 责任转派申请';

-- D17-T28: 工单业务时间线
-- FK到工单、用户、结果。成功业务事件一事务追加一次；重放幂等不重复追加。
CREATE TABLE `work_order_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `work_order_id` BIGINT UNSIGNED NOT NULL,
  `round_no` INT UNSIGNED NOT NULL,
  CONSTRAINT `ck_work_order_history_round_no` CHECK (`round_no` >= 1),
  `from_status` VARCHAR(24) NULL,
  `to_status` VARCHAR(24) NOT NULL,
  `action` VARCHAR(64) NOT NULL,
  `operator_id` BIGINT UNSIGNED NOT NULL,
  `reason` VARCHAR(1000) NULL,
  `review_result_id` BIGINT UNSIGNED NULL,
  `trace_id` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_work_order_history_1` (`work_order_id`,`created_at`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T28 工单业务时间线';

-- D17-T29: 成功业务审计
-- org_id→org；actor_id→用户。多态object_id不设FK；不包含密码、令牌、原始业务数据。应用账号仅SELECT和INSERT。
CREATE TABLE `audit_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `org_id` BIGINT UNSIGNED NOT NULL,
  `actor_id` BIGINT UNSIGNED NULL,
  `action` VARCHAR(96) NOT NULL,
  `object_type` VARCHAR(64) NOT NULL,
  `object_id` BIGINT UNSIGNED NOT NULL,
  `trace_id` VARCHAR(64) NOT NULL,
  `before_json` JSON NULL,
  `after_json` JSON NULL,
  `reason` VARCHAR(1000) NULL,
  `outcome` VARCHAR(16) NOT NULL,
  `request_hash` CHAR(64) COLLATE utf8mb4_bin NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_log_1` (`org_id`,`object_type`,`object_id`,`created_at`,`id`),
  KEY `idx_audit_log_2` (`actor_id`,`created_at`),
  KEY `idx_audit_log_3` (`trace_id`),
  CONSTRAINT `ck_audit_log_outcome` CHECK (`outcome` IN ('SUCCESS')),
  CONSTRAINT `ck_audit_object` CHECK (`object_id` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T29 成功业务审计';

-- D17-T30: 被拒绝和失败安全事件
-- 独立短事务写入；业务回滚不能抹掉拒绝记录。未知用户输入只记录脱敏摘要，避免可枚举登录名。
CREATE TABLE `security_event` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `org_id` BIGINT UNSIGNED NULL,
  `actor_id` BIGINT UNSIGNED NULL,
  `trace_id` VARCHAR(64) NOT NULL,
  `event_code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `object_ref` VARCHAR(128) NULL,
  `outcome` VARCHAR(16) NOT NULL,
  `ip_hash` CHAR(64) COLLATE utf8mb4_bin NULL,
  `details` JSON NULL,
  PRIMARY KEY (`id`),
  KEY `idx_security_event_1` (`trace_id`),
  KEY `idx_security_event_2` (`org_id`,`created_at`),
  CONSTRAINT `ck_security_event_outcome` CHECK (`outcome` IN ('DENIED','ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T30 被拒绝和失败安全事件';

-- D17-T31: 请求重放控制
-- 成功记录与业务同事务提交，未提交时并发唯一键等待后读取；确定拒绝不消耗业务成功键。重放前仍检查账号与资源范围。
CREATE TABLE `idempotency_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `actor_key` VARCHAR(128) COLLATE utf8mb4_bin NOT NULL,
  `route_key` VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
  `request_key` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `request_hash` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `http_status` INT UNSIGNED NOT NULL,
  `response_json` JSON NOT NULL,
  `expires_at` DATETIME(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_idempotency_record_1` (`actor_key`,`route_key`,`request_key`),
  KEY `idx_idempotency_record_2` (`expires_at`),
  CONSTRAINT `ck_idempotency_record_status` CHECK (`status` IN ('SUCCEEDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T31 请求重放控制';

-- D17-T32: 事务外通知扩展
-- 工单变更同事务插入，投递失败不回滚闭环。无外部通知授权时只做站内待办。
CREATE TABLE `notification_outbox` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `org_id` BIGINT UNSIGNED NOT NULL,
  `aggregate_id` BIGINT UNSIGNED NOT NULL,
  `event_type` VARCHAR(64) NOT NULL,
  `payload_json` JSON NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `attempts` INT UNSIGNED NOT NULL DEFAULT 0,
  `next_attempt_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_outbox_1` (`status`,`next_attempt_at`),
  CONSTRAINT `ck_notification_outbox_status` CHECK (`status` IN ('PENDING','SENT','ERROR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T32 事务外通知扩展';

-- D17-T33: 候选指标口径
-- 查询返回口径状态，候选显示null并解释原因。禁止保存未经执行的指标结果冒充实测。
CREATE TABLE `metric_definition` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `code` VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `version_no` INT UNSIGNED NOT NULL,
  CONSTRAINT `ck_metric_definition_version_no` CHECK (`version_no` >= 1),
  `definition_json` JSON NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `effective_at` DATETIME(3) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_definition_1` (`code`,`version_no`),
  CONSTRAINT `ck_metric_definition_status` CHECK (`status` IN ('CANDIDATE','CONFIRMED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T33 候选指标口径';

ALTER TABLE `org` ADD CONSTRAINT `fk_org_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `user_account` ADD CONSTRAINT `fk_user_account_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `user_role` ADD CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `user_role` ADD CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `role_permission` ADD CONSTRAINT `fk_role_permission_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `role_permission` ADD CONSTRAINT `fk_role_permission_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `user_scope` ADD CONSTRAINT `fk_user_scope_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `user_scope` ADD CONSTRAINT `fk_user_scope_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `auth_session` ADD CONSTRAINT `fk_auth_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_source` ADD CONSTRAINT `fk_data_source_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_source` ADD CONSTRAINT `fk_data_source_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_asset` ADD CONSTRAINT `fk_data_asset_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_asset` ADD CONSTRAINT `fk_data_asset_source_id` FOREIGN KEY (`source_id`) REFERENCES `data_source` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_asset` ADD CONSTRAINT `fk_data_asset_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_asset` ADD CONSTRAINT `fk_data_asset_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `metadata_field` ADD CONSTRAINT `fk_metadata_field_asset_id` FOREIGN KEY (`asset_id`) REFERENCES `data_asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `metadata_field` ADD CONSTRAINT `fk_metadata_field_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `business_term` ADD CONSTRAINT `fk_business_term_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `business_term` ADD CONSTRAINT `fk_business_term_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_standard` ADD CONSTRAINT `fk_data_standard_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_standard` ADD CONSTRAINT `fk_data_standard_term_id` FOREIGN KEY (`term_id`) REFERENCES `business_term` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `data_standard` ADD CONSTRAINT `fk_data_standard_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `standard_version` ADD CONSTRAINT `fk_standard_version_standard_id` FOREIGN KEY (`standard_id`) REFERENCES `data_standard` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `standard_version` ADD CONSTRAINT `fk_standard_version_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `field_standard` ADD CONSTRAINT `fk_field_standard_field_id` FOREIGN KEY (`field_id`) REFERENCES `metadata_field` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `field_standard` ADD CONSTRAINT `fk_field_standard_standard_version_id` FOREIGN KEY (`standard_version_id`) REFERENCES `standard_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_rule` ADD CONSTRAINT `fk_quality_rule_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_rule` ADD CONSTRAINT `fk_quality_rule_field_id` FOREIGN KEY (`field_id`) REFERENCES `metadata_field` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_rule` ADD CONSTRAINT `fk_quality_rule_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `rule_version` ADD CONSTRAINT `fk_rule_version_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `quality_rule` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `rule_version` ADD CONSTRAINT `fk_rule_version_standard_version_id` FOREIGN KEY (`standard_version_id`) REFERENCES `standard_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `rule_version` ADD CONSTRAINT `fk_rule_version_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `detection_batch` ADD CONSTRAINT `fk_detection_batch_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `detection_batch` ADD CONSTRAINT `fk_detection_batch_rule_version_id` FOREIGN KEY (`rule_version_id`) REFERENCES `rule_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `detection_batch` ADD CONSTRAINT `fk_detection_batch_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_asset_id` FOREIGN KEY (`asset_id`) REFERENCES `data_asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_field_id` FOREIGN KEY (`field_id`) REFERENCES `metadata_field` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_rule_version_id` FOREIGN KEY (`rule_version_id`) REFERENCES `rule_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_batch_id` FOREIGN KEY (`batch_id`) REFERENCES `detection_batch` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_related_issue_id` FOREIGN KEY (`related_issue_id`) REFERENCES `quality_issue` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `quality_issue` ADD CONSTRAINT `fk_quality_issue_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `issue_identity` ADD CONSTRAINT `fk_issue_identity_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `issue_identity` ADD CONSTRAINT `fk_issue_identity_active_issue_id` FOREIGN KEY (`active_issue_id`) REFERENCES `quality_issue` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `governance_work_order` ADD CONSTRAINT `fk_governance_work_order_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `governance_work_order` ADD CONSTRAINT `fk_governance_work_order_issue_id` FOREIGN KEY (`issue_id`) REFERENCES `quality_issue` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `governance_work_order` ADD CONSTRAINT `fk_governance_work_order_assignee_id` FOREIGN KEY (`assignee_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `governance_work_order` ADD CONSTRAINT `fk_governance_work_order_valid_pass_id` FOREIGN KEY (`valid_pass_id`) REFERENCES `review_result` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `governance_work_order` ADD CONSTRAINT `fk_governance_work_order_closed_by` FOREIGN KEY (`closed_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `governance_work_order` ADD CONSTRAINT `fk_governance_work_order_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `remediation_round` ADD CONSTRAINT `fk_remediation_round_work_order_id` FOREIGN KEY (`work_order_id`) REFERENCES `governance_work_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `remediation_round` ADD CONSTRAINT `fk_remediation_round_submitted_by` FOREIGN KEY (`submitted_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `remediation_round` ADD CONSTRAINT `fk_remediation_round_rule_version_id` FOREIGN KEY (`rule_version_id`) REFERENCES `rule_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `evidence_file` ADD CONSTRAINT `fk_evidence_file_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `evidence_file` ADD CONSTRAINT `fk_evidence_file_uploaded_by` FOREIGN KEY (`uploaded_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `evidence_file` ADD CONSTRAINT `fk_evidence_file_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `round_evidence` ADD CONSTRAINT `fk_round_evidence_round_id` FOREIGN KEY (`round_id`) REFERENCES `remediation_round` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `round_evidence` ADD CONSTRAINT `fk_round_evidence_evidence_id` FOREIGN KEY (`evidence_id`) REFERENCES `evidence_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_task` ADD CONSTRAINT `fk_review_task_work_order_id` FOREIGN KEY (`work_order_id`) REFERENCES `governance_work_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_task` ADD CONSTRAINT `fk_review_task_round_id` FOREIGN KEY (`round_id`) REFERENCES `remediation_round` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_task` ADD CONSTRAINT `fk_review_task_rule_version_id` FOREIGN KEY (`rule_version_id`) REFERENCES `rule_version` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_task` ADD CONSTRAINT `fk_review_task_requested_by` FOREIGN KEY (`requested_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_result` ADD CONSTRAINT `fk_review_result_task_id` FOREIGN KEY (`task_id`) REFERENCES `review_task` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_result` ADD CONSTRAINT `fk_review_result_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `review_result` ADD CONSTRAINT `fk_review_result_proof_evidence_id` FOREIGN KEY (`proof_evidence_id`) REFERENCES `evidence_file` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `transfer_request` ADD CONSTRAINT `fk_transfer_request_work_order_id` FOREIGN KEY (`work_order_id`) REFERENCES `governance_work_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `transfer_request` ADD CONSTRAINT `fk_transfer_request_requested_by` FOREIGN KEY (`requested_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `transfer_request` ADD CONSTRAINT `fk_transfer_request_target_user_id` FOREIGN KEY (`target_user_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `transfer_request` ADD CONSTRAINT `fk_transfer_request_decided_by` FOREIGN KEY (`decided_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `transfer_request` ADD CONSTRAINT `fk_transfer_request_created_by` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `work_order_history` ADD CONSTRAINT `fk_work_order_history_work_order_id` FOREIGN KEY (`work_order_id`) REFERENCES `governance_work_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `work_order_history` ADD CONSTRAINT `fk_work_order_history_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `work_order_history` ADD CONSTRAINT `fk_work_order_history_review_result_id` FOREIGN KEY (`review_result_id`) REFERENCES `review_result` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `audit_log` ADD CONSTRAINT `fk_audit_log_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `audit_log` ADD CONSTRAINT `fk_audit_log_actor_id` FOREIGN KEY (`actor_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `security_event` ADD CONSTRAINT `fk_security_event_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `security_event` ADD CONSTRAINT `fk_security_event_actor_id` FOREIGN KEY (`actor_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `notification_outbox` ADD CONSTRAINT `fk_notification_outbox_org_id` FOREIGN KEY (`org_id`) REFERENCES `org` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `notification_outbox` ADD CONSTRAINT `fk_notification_outbox_aggregate_id` FOREIGN KEY (`aggregate_id`) REFERENCES `governance_work_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
