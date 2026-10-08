package com.example.datagov.modules.evidence.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.common.DesignLink;
import com.example.datagov.modules.evidence.application.EvidenceService;
import java.util.*;
import java.time.Instant;
import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/v1")
public class EvidenceController {
 private final EvidenceService service;
 public EvidenceController(EvidenceService service) { this.service=service; }
 @DesignLink(api="D18-API-034",requirements={"FR-WO-003","NFR-SEC-001"},tests={"TC-WO-004","TC-EVIDENCE-001"})
 @PostMapping("/evidence-files") public Object api034(@RequestParam String workOrderId,@RequestPart MultipartFile file,@RequestHeader(value="X-Request-Id",required=false) String key) { return service.upload(key,workOrderId,file); }
 @DesignLink(api="D18-API-035",requirements={"NFR-SEC-001"},tests={"TC-EVIDENCE-001","TC-AUTH-003"})
 @GetMapping("/evidence-files/{id}/content") public Object api035(@PathVariable String id) { return service.download(id); }
}
