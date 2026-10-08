-- D15-M03/M04 incremental implementation. Existing V001..V008 checksums remain unchanged.
-- D17-T34: immutable-after-run submitted dataset. A batch uses one fixed rule snapshot.
CREATE TABLE `detection_dataset` (
 `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 `batch_id` BIGINT UNSIGNED NOT NULL,
 `asset_id` BIGINT UNSIGNED NOT NULL,
 `rows_json` JSON NOT NULL,
 `content_hash` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
 `row_count` INT UNSIGNED NOT NULL,
 `created_by` BIGINT UNSIGNED NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY (`id`),
 UNIQUE KEY `uk_detection_dataset_batch` (`batch_id`),
 CONSTRAINT `fk_dataset_batch` FOREIGN KEY (`batch_id`) REFERENCES `detection_batch` (`id`) ON DELETE RESTRICT,
 CONSTRAINT `fk_dataset_asset` FOREIGN KEY (`asset_id`) REFERENCES `data_asset` (`id`) ON DELETE RESTRICT,
 CONSTRAINT `fk_dataset_actor` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT,
 CONSTRAINT `ck_dataset_row_count` CHECK (`row_count` BETWEEN 1 AND 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T34 提交检测数据集';

-- D17-T35: row-level masked findings. Raw business values never appear in this table.
CREATE TABLE `detection_finding` (
 `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 `batch_id` BIGINT UNSIGNED NOT NULL,
 `row_no` INT UNSIGNED NOT NULL,
 `record_key_hash` CHAR(64) COLLATE utf8mb4_bin NOT NULL,
 `reason_code` VARCHAR(64) NOT NULL,
 `issue_id` BIGINT UNSIGNED NOT NULL,
 `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 PRIMARY KEY (`id`),
 UNIQUE KEY `uk_detection_finding_row` (`batch_id`,`row_no`),
 KEY `idx_detection_finding_issue` (`issue_id`),
 CONSTRAINT `fk_finding_batch` FOREIGN KEY (`batch_id`) REFERENCES `detection_batch` (`id`) ON DELETE RESTRICT,
 CONSTRAINT `fk_finding_issue` FOREIGN KEY (`issue_id`) REFERENCES `quality_issue` (`id`) ON DELETE RESTRICT,
 CONSTRAINT `ck_finding_row_no` CHECK (`row_no` BETWEEN 1 AND 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='D17-T35 检测结果与问题关联';

INSERT INTO permission(code,name,resource,action) VALUES
 ('rule:publish','发布质量规则','rule','publish'),
 ('quality:execute','执行提交数据集检测','quality','execute')
 ON DUPLICATE KEY UPDATE code=code;
INSERT INTO role_permission(role_id,permission_id)
 SELECT r.id,p.id FROM role r JOIN permission p ON p.code IN ('rule:publish','quality:execute')
 WHERE r.code IN ('ROLE-SYSADMIN','ROLE-ADMIN')
 ON DUPLICATE KEY UPDATE role_id=role_id;
-- Existing tokens must be refreshed when their role's effective permission set changes.
UPDATE user_account u SET permission_version=permission_version+1
 WHERE EXISTS(SELECT 1 FROM user_role ur JOIN role r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.code IN ('ROLE-SYSADMIN','ROLE-ADMIN'));
