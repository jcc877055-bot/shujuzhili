package com.example.datagov.common;
import java.util.*;
/** D19: explicit writable fields; actor, status, valid and closedBy are server-owned. */
public final class CommandFields {
 private CommandFields(){}
 private static final Map<String,Set<String>> FIELDS=Map.ofEntries(
  Map.entry("/orgs",Set.of("code","name","parentId")),Map.entry("/users",Set.of("orgId","username","displayName","password")),
  Map.entry("/detection-batches",Set.of("orgId","ruleVersionId","batchNo","sourceType")),Map.entry("/quality/issues",Set.of("orgId","assetId","fieldId","ruleVersionId","batchId","summary","locator")),
  Map.entry("/decision",Set.of("decision","relatedIssueId","reason","expectedVersion")),Map.entry("/work-orders",Set.of("issueId")),
  Map.entry("/dispatch",Set.of("assigneeId","dueAt","reason","expectedVersion","roundNo")),Map.entry("/claim",Set.of("expectedVersion","roundNo")),
  Map.entry("/transfer-requests",Set.of("targetUserId","reason","expectedVersion","roundNo")),Map.entry("/rounds",Set.of("roundNo","expectedVersion","rootCause","remediationText","evidenceIds")),
  Map.entry("/review-tasks",Set.of("roundNo","expectedVersion","sourceType")),Map.entry("/reviews",Set.of("taskId","roundNo","expectedVersion","result","reason","proofEvidenceId")),
  Map.entry("/close",Set.of("roundNo","expectedVersion","reviewResultId","reason")),Map.entry("/evidence-files",Set.of("workOrderId","name","sha256")));
 public static void validate(String uri,Object body){if(!(body instanceof Map<?,?> map))return;String route=uri.substring("/api/v1".length());var allowed=FIELDS.get(route);if(allowed==null)allowed=FIELDS.get(route.substring(route.lastIndexOf('/')));Checks.require(allowed!=null&&allowed.containsAll(map.keySet()),400,ErrorCode.INVALID_REQUEST,"请求包含不支持或由服务器维护的字段");}
}
