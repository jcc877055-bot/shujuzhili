package com.example.datagov.modules.quality.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.common.DesignLink;
import com.example.datagov.modules.quality.application.QualityService;
import com.example.datagov.modules.quality.application.DetectionService;
import java.util.*;
import java.time.Instant;

@RestController @RequestMapping("/api/v1")
public class QualityController {
 @SuppressWarnings("unchecked") private static Map<String,Object> locator(Object x){return (Map<String,Object>)x;}
 private final QualityService service;
 private final DetectionService detection;
 public QualityController(QualityService service,DetectionService detection) { this.service=service;this.detection=detection; }
 @DesignLink(api="D18-API-020",requirements={"FR-QUALITY-001"},tests={"TC-QUALITY-001"})
 @GetMapping("/detection-batches") public Object api020(@RequestParam(required=false) String orgId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.list("detection_batch",orgId,page,size); }
 @DesignLink(api="D18-API-021",requirements={"FR-QUALITY-001"},tests={"TC-QUALITY-001"})
 @PostMapping("/detection-batches") public Object api021(@RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.batch(key,(String)b.get("orgId"),(String)b.get("ruleVersionId"),(String)b.get("batchNo"),(String)b.getOrDefault("sourceType","MANUAL"),b); }
 @DesignLink(api="D18-API-022",requirements={"FR-DETECTION-001","FR-DETECTION-002","FR-QUALITY-001"},tests={"TC-DETECTION-001","TC-DETECTION-002","TC-DETECTION-003","TC-DETECTION-004","TC-DETECTION-005"})
 @PostMapping("/detection-batches/{id}/run") public Object api022(@PathVariable String id,@RequestBody DetectionService.Run b,@RequestHeader(value="X-Request-Id",required=false) String key) { return detection.run(key,id,b); }
 @DesignLink(api="D18-API-023",requirements={"FR-QUALITY-001","PRM-001"},tests={"TC-QUALITY-001","TC-AUTH-003"})
 @GetMapping("/quality/issues") public Object api023(@RequestParam(required=false) String orgId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.list("quality_issue",orgId,page,size); }
 @DesignLink(api="D18-API-024",requirements={"FR-QUALITY-001","BR-SYS-001"},tests={"TC-QUALITY-001","TC-QUALITY-002"})
 @PostMapping("/quality/issues") public Object api024(@RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.register(key,(String)b.get("orgId"),(String)b.get("assetId"),(String)b.get("fieldId"),(String)b.get("ruleVersionId"),(String)b.get("batchId"),(String)b.get("summary"),locator(b.get("locator")),b); }
 @DesignLink(api="D18-API-025",requirements={"FR-QUALITY-001","PRM-001"},tests={"TC-QUALITY-001","TC-AUTH-003"})
 @GetMapping("/quality/issues/{id}") public Object api025(@PathVariable String id) { return service.detail(id); }
 @DesignLink(api="D18-API-026",requirements={"FR-QUALITY-002"},tests={"TC-QUALITY-003"})
 @PostMapping("/quality/issues/{id}/decision") public Object api026(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.decision(key,id,((Number)b.getOrDefault("expectedVersion",-1)).longValue(),(String)b.get("decision"),(String)b.get("relatedIssueId"),(String)b.get("reason"),b); }
 @DesignLink(api="D18-API-061",requirements={"FR-DETECTION-001"},tests={"TC-DETECTION-006"})
 @GetMapping("/detection-batches/{id}") public Object api061(@PathVariable String id){return detection.detail(id);}
 @DesignLink(api="D18-API-062",requirements={"FR-DETECTION-001"},tests={"TC-DETECTION-001","TC-DETECTION-006","TC-DETECTION-007"})
 @PostMapping("/detection-batches/{id}/dataset") public Object api062(@PathVariable String id,@RequestBody DetectionService.Dataset b,@RequestHeader(value="X-Request-Id",required=false) String key){return detection.submit(key,id,b);}
 @DesignLink(api="D18-API-063",requirements={"FR-DETECTION-002"},tests={"TC-DETECTION-005","TC-DETECTION-006"})
 @GetMapping("/detection-batches/{id}/findings") public Object api063(@PathVariable String id,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){return detection.findings(id,page,size);}
}
