-- MySQL 8.0.16+; UTC; Flyway is the only schema version manager.
-- D17-T25: 可信复检任务绑定
-- 各FK对应实体；请求人不同于submitted_by，手工提交结果身份还须等于requested_by。active_round_id为STORED生成列，PENDING或RUNNING时取round_id，保证同提交快照最多一个活动任务；DONE或ERROR变NULL后可重试。MANUAL同样创建任务绑定材料，外部异步执行属于扩展。
CREATE TABLE `review_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `version` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `work_order_id` BIGINT UNSIGNED NOT NULL,
  `round_id` BIGINT UNSIGNED NOT NULL,
  `rule_version_id` BIGINT UNSIGNED NOT NULL,
  `bound_work_order_version` BIGINT UNSIGNED NOT NULL,
  `requested_by` BIGINT UNSIGNED NOT NULL,
  `source_type` VARCHAR(16) NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `attempt_no` INT UNSIGNED NOT NULL DEFAULT 1,
  CONSTRAINT `ck_review_task_attempt_no` CHECK (`attempt_no` >= 1),
  `lease_until` DATETIME(3) NULL,
  `active_round_id` BIGINT UNSIGNED GENERATED ALWAYS AS (CASE WHEN `status` IN ('PENDING','RUNNING') THEN `round_id` ELSE NULL END) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_task_1` (`active_round_id`),
  KEY `idx_review_task_2` (`work_order_id`,`round_id`,`status`),
  KEY `idx_review_task_3` (`status`,`lease_until`),
  CONSTRAINT `ck_review_task_status` CHECK (`status` IN ('PENDING','RUNNING','DONE','ERROR')),
  CONSTRAINT `ck_review_task_source_type` CHECK (`source_type` IN ('MANUAL','ENGINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T25 可信复检任务绑定';

-- D17-T26: 不可变复检结论
-- task_id→review_task；reviewer_id→用户；proof_evidence_id→文件。valid由服务端计算，旧轮次结果只记录为无效不能关闭。ERROR新任务重试，不覆盖旧结果。
CREATE TABLE `review_result` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `task_id` BIGINT UNSIGNED NOT NULL,
  `reviewer_id` BIGINT UNSIGNED NOT NULL,
  `result` VARCHAR(8) NOT NULL,
  `reason` VARCHAR(2000) NOT NULL,
  `proof_evidence_id` BIGINT UNSIGNED NULL,
  `trusted_source` VARCHAR(16) NOT NULL,
  `result_hash` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
  `valid` TINYINT UNSIGNED NOT NULL,
  CONSTRAINT `ck_review_result_valid` CHECK (`valid` IN (0,1)),
  `completed_at` DATETIME(3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_result_1` (`task_id`),
  KEY `idx_review_result_2` (`reviewer_id`,`completed_at`),
  CONSTRAINT `ck_review_result_result` CHECK (`result` IN ('PASS','FAIL','ERROR')),
  CONSTRAINT `ck_review_result_trusted_source` CHECK (`trusted_source` IN ('MANUAL','ENGINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T26 不可变复检结论';
