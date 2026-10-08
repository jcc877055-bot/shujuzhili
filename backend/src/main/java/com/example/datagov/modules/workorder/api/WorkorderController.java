package com.example.datagov.modules.workorder.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.common.DesignLink;
import com.example.datagov.modules.workorder.application.WorkorderService;
import java.util.*;
import java.time.Instant;

@RestController @RequestMapping("/api/v1")
public class WorkorderController {
 @SuppressWarnings("unchecked") private static List<String> strings(Object x) { return (List<String>)x; }
 private final WorkorderService service;
 public WorkorderController(WorkorderService service) { this.service=service; }
 @DesignLink(api="D18-API-027",requirements={"FR-WO-001","PRM-001"},tests={"TC-AUTH-003","TC-WO-006"})
 @GetMapping("/work-orders") public Object api027(@RequestParam(required=false) String status,@RequestParam(required=false) String assigneeId,@RequestParam(required=false) Boolean overdue,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.list(status,assigneeId,overdue,page,size); }
 @DesignLink(api="D18-API-028",requirements={"FR-WO-001"},tests={"TC-WO-001"})
 @PostMapping("/work-orders") public Object api028(@RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.create(key,(String)b.get("issueId"),b); }
 @DesignLink(api="D18-API-029",requirements={"FR-WO-001","PRM-001"},tests={"TC-WO-001","TC-AUTH-003"})
 @GetMapping("/work-orders/{id}") public Object api029(@PathVariable String id) { return service.detail(id); }
 @DesignLink(api="D18-API-030",requirements={"FR-WO-001"},tests={"TC-WO-001","TC-WO-003"})
 @PostMapping("/work-orders/{id}/dispatch") public Object api030(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.dispatch(key,id,(String)b.get("assigneeId"),b.get("dueAt")==null?null:Instant.parse((String)b.get("dueAt")),(String)b.get("reason"),((Number)b.getOrDefault("expectedVersion",-1)).longValue(),b); }
 @DesignLink(api="D18-API-031",requirements={"FR-WO-002","PRM-002"},tests={"TC-WO-001","TC-WO-002"})
 @PostMapping("/work-orders/{id}/claim") public Object api031(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.claim(key,id,((Number)b.getOrDefault("expectedVersion",-1)).longValue(),b); }
 @DesignLink(api="D18-API-032",requirements={"FR-WO-002"},tests={"TC-WO-007"})
 @PostMapping("/work-orders/{id}/transfer-requests") public Object api032(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.transfer(key,id,(String)b.get("targetUserId"),(String)b.get("reason"),((Number)b.getOrDefault("expectedVersion",-1)).longValue(),b); }
 @DesignLink(api="D18-API-033",requirements={"FR-WO-002"},tests={"TC-WO-007"})
 @PostMapping("/transfer-requests/{id}/decision") public Object api033(@PathVariable String id) { return service.pending("D18-API-033"); }
 @DesignLink(api="D18-API-036",requirements={"FR-WO-003","DATA-003"},tests={"TC-WO-004","TC-WO-005"})
 @PostMapping("/work-orders/{id}/rounds") public Object api036(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.submit(key,id,((Number)b.getOrDefault("roundNo",-1)).intValue(),((Number)b.getOrDefault("expectedVersion",-1)).longValue(),(String)b.get("rootCause"),(String)b.get("remediationText"),strings(b.get("evidenceIds")),b); }
 @DesignLink(api="D18-API-040",requirements={"FR-WO-004","BR-SYS-004"},tests={"TC-REVIEW-001","TC-REVIEW-004","TC-STATE-001"})
 @PostMapping("/work-orders/{id}/close") public Object api040(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.close(key,id,((Number)b.getOrDefault("roundNo",-1)).intValue(),((Number)b.getOrDefault("expectedVersion",-1)).longValue(),(String)b.get("reviewResultId"),(String)b.get("reason"),b); }
 @DesignLink(api="D18-API-041",requirements={"FR-AUDIT-001","AUD-003"},tests={"TC-AUDIT-001","TC-AUDIT-003"})
 @GetMapping("/work-orders/{id}/history") public Object api041(@PathVariable String id, @RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.history(id,page,size); }
}
