package com.example.datagov.modules.audit.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.modules.audit.application.AuditService;
import com.example.datagov.common.DesignLink;
import java.time.Instant;
@RestController @RequestMapping("/api/v1")
public class AuditController {
    private final AuditService service;
    public AuditController(AuditService service) { this.service=service; }
    /** D18-API-042: 只读审计; permission=audit:read. */
    @DesignLink(api="D18-API-042",requirements={"FR-AUDIT-001","AUD-001","AUD-002","AUD-003"},tests={"TC-AUDIT-001","TC-AUDIT-005"})
    @GetMapping("/audit-logs") public Object api042(@RequestParam(defaultValue="BUSINESS") String kind,@RequestParam(required=false) String orgId,@RequestParam(required=false) String objectType,@RequestParam(required=false) String objectId,@RequestParam(required=false) String actorId,@RequestParam(required=false) String action,@RequestParam(required=false) String outcome,@RequestParam(required=false) String traceId,@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.list(new AuditService.Filter(kind,orgId,objectType,objectId,actorId,action,outcome,traceId,from,to),page,size); }
    /** D18-API-043: 审计导出候选扩展; permission=audit:export. */
    @DesignLink(api="D18-API-043",requirements={"FR-AUDIT-001","AUD-001","AUD-002"},tests={"TC-AUDIT-005","TC-AUDIT-006"})
    @PostMapping("/audit-exports") public Object api043(@RequestBody AuditService.Export body,@RequestHeader(value="X-Request-Id",required=false) String key) { return service.export(key,body); }
}
