package com.example.datagov.modules.review.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.common.DesignLink;
import com.example.datagov.modules.review.application.ReviewService;
import java.util.*;
import java.time.Instant;

@RestController @RequestMapping("/api/v1")
public class ReviewController {
 private final ReviewService service;
 public ReviewController(ReviewService service) { this.service=service; }
 @DesignLink(api="D18-API-037",requirements={"FR-REVIEW-001","PRM-003"},tests={"TC-REVIEW-004"})
 @PostMapping("/work-orders/{id}/review-tasks") public Object api037(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.task(key,id,((Number)b.getOrDefault("roundNo",-1)).intValue(),((Number)b.getOrDefault("expectedVersion",-1)).longValue(),(String)b.get("sourceType"),b); }
 @DesignLink(api="D18-API-038",requirements={"FR-REVIEW-002","PRM-003"},tests={"TC-REVIEW-001","TC-REVIEW-002","TC-REVIEW-003"})
 @PostMapping("/work-orders/{id}/reviews") public Object api038(@PathVariable String id, @RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.result(key,id,(String)b.get("taskId"),((Number)b.getOrDefault("roundNo",-1)).intValue(),((Number)b.getOrDefault("expectedVersion",-1)).longValue(),(String)b.get("result"),(String)b.get("reason"),(String)b.get("proofEvidenceId"),b); }
 @DesignLink(api="D18-API-039",requirements={"PRM-003"},tests={"TC-REVIEW-005"})
 @PostMapping("/internal/review-tasks/{id}/result") public Object api039(@PathVariable String id) { return service.pending(id); }
}
