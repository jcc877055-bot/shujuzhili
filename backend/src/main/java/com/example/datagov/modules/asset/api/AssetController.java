package com.example.datagov.modules.asset.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.common.DesignLink;
import com.example.datagov.modules.asset.application.AssetService;
@RestController @RequestMapping("/api/v1") public class AssetController {
 private final AssetService service;public AssetController(AssetService service){this.service=service;}
 public record StateCommand(long expectedVersion,String reason){}
 @DesignLink(api="D18-API-010",requirements={"FR-ASSET-001","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-001","TC-ASSET-008"})
 @GetMapping("/assets") public Object api010(@RequestParam(required=false) String orgId,@RequestParam(required=false) String keyword,@RequestParam(required=false) String status,@RequestParam(required=false) String classification,@RequestParam(required=false) String ownerId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){return service.list(orgId,keyword,status,classification,ownerId,page,size);}
 @DesignLink(api="D18-API-011",requirements={"FR-ASSET-001","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-002","TC-ASSET-008"})
 @PostMapping("/assets") public Object api011(@RequestBody AssetService.AssetInput b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.create(key,b);}
 @DesignLink(api="D18-API-012",requirements={"FR-ASSET-001","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-001","TC-ASSET-008"})
 @GetMapping("/assets/{id}") public Object api012(@PathVariable String id){return service.detail(id);}
 @DesignLink(api="D18-API-013",requirements={"FR-ASSET-002","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-003","TC-ASSET-008"})
 @GetMapping("/assets/{id}/fields") public Object api013(@PathVariable String id){return service.fields(id);}
 @DesignLink(api="D18-API-014",requirements={"FR-ASSET-002","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-003","TC-ASSET-008"})
 @PostMapping("/assets/{id}/fields") public Object api014(@PathVariable String id,@RequestBody AssetService.FieldInput b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.field(key,id,b);}
 @DesignLink(api="D18-API-046",requirements={"FR-ASSET-001","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-004","TC-ASSET-008"})
 @PutMapping("/assets/{id}") public Object api046(@PathVariable String id,@RequestBody AssetService.AssetEdit b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.edit(key,id,b);}
 @DesignLink(api="D18-API-047",requirements={"FR-ASSET-001","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-004","TC-ASSET-008"})
 @PostMapping("/assets/{id}/publish") public Object api047(@PathVariable String id,@RequestBody StateCommand b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.state(key,id,b.expectedVersion(),"PUBLISHED",b.reason(),b);}
 @DesignLink(api="D18-API-048",requirements={"FR-ASSET-001","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-004","TC-ASSET-008"})
 @PostMapping("/assets/{id}/disable") public Object api048(@PathVariable String id,@RequestBody StateCommand b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.state(key,id,b.expectedVersion(),"DISABLED",b.reason(),b);}
 @DesignLink(api="D18-API-049",requirements={"FR-ASSET-002","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-005","TC-ASSET-008"})
 @PutMapping("/assets/{id}/fields/{fieldId}") public Object api049(@PathVariable String id,@PathVariable String fieldId,@RequestBody AssetService.FieldEdit b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.editField(key,id,fieldId,b);}
 @DesignLink(api="D18-API-050",requirements={"FR-ASSET-003","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-006","TC-ASSET-008"})
 @GetMapping("/data-sources") public Object api050(@RequestParam(required=false) String orgId,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){return service.sources(orgId,status,page,size);}
 @DesignLink(api="D18-API-051",requirements={"FR-ASSET-003","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-006","TC-ASSET-008"})
 @PostMapping("/data-sources") public Object api051(@RequestBody AssetService.SourceInput b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.createSource(key,b);}
 @DesignLink(api="D18-API-052",requirements={"FR-ASSET-003","FR-ASSET-004","NFR-SEC-001"},tests={"TC-ASSET-007","TC-ASSET-008"})
 @PutMapping("/data-sources/{id}") public Object api052(@PathVariable String id,@RequestBody AssetService.SourceEdit b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.editSource(key,id,b);}
}
