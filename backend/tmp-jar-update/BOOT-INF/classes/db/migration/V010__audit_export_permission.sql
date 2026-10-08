-- D15-M08 / D17-T03/T04/T05/T02. No audit mutation or metric approval is introduced.
INSERT INTO permission(code,name,resource,action) VALUES('audit:export','导出只读审计记录','audit','export')
 ON DUPLICATE KEY UPDATE code=code;
INSERT INTO role_permission(role_id,permission_id)
 SELECT r.id,p.id FROM role r JOIN permission p ON p.code='audit:export'
 WHERE r.code IN ('ROLE-SYSADMIN','ROLE-AUDITOR')
 ON DUPLICATE KEY UPDATE role_id=role_id;
UPDATE user_account u SET permission_version=permission_version+1
 WHERE EXISTS(SELECT 1 FROM user_role ur JOIN role r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.code IN ('ROLE-SYSADMIN','ROLE-AUDITOR'));
-- D17-T33 candidate definitions only. This migration does not claim business approval.
INSERT INTO metric_definition(code,version_no,definition_json,status) VALUES
 ('ONTIME_CLOSE_RATE',1,JSON_OBJECT('semanticKey','ONTIME_CLOSE_RATE','implementationVersion','V1','formula','closedOnTimeWithDue / closedWithDue','window','closed_at in [from,to)'), 'CANDIDATE'),
 ('AVG_PROCESSING_HOURS',1,JSON_OBJECT('semanticKey','AVG_PROCESSING_HOURS','implementationVersion','V1','formula','sum(firstClaimToCloseHours) / measuredClosedOrders','window','closed_at in [from,to)'), 'CANDIDATE'),
 ('FIRST_REVIEW_PASS_RATE',1,JSON_OBJECT('semanticKey','FIRST_REVIEW_PASS_RATE','implementationVersion','V1','formula','firstValidBusinessPASS / firstValidBusinessReview','window','first PASS/FAIL completed_at in [from,to), exclude ERROR'), 'CANDIDATE')
 ON DUPLICATE KEY UPDATE code=code;
