package com.example.datagov.modules.standard.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.modules.standard.application.StandardService;
import com.example.datagov.common.DesignLink;
@RestController @RequestMapping("/api/v1")
public class StandardController {
    private final StandardService service;
    public StandardController(StandardService service) { this.service=service; }
    /** D18-API-015: 标准版本查询; permission=standard:read. */
    @DesignLink(api="D18-API-015",requirements={"FR-STANDARD-001"},tests={"TC-STANDARD-001","TC-STANDARD-004"})
    @GetMapping("/standards") public Object api015(@RequestParam(required=false) String orgId,@RequestParam(required=false) String fieldId,@RequestParam(required=false) String keyword,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.list(false,orgId,fieldId,keyword,status,page,size); }
    /** D18-API-016: 标准版本候选扩展; permission=standard:write. */
    @DesignLink(api="D18-API-016",requirements={"FR-STANDARD-001"},tests={"TC-STANDARD-001","TC-STANDARD-003"})
    @PostMapping("/standards/{id}/versions") public Object api016(@PathVariable String id,@RequestBody StandardService.StandardVersionInput b,@RequestHeader(value="X-Request-Id",required=false) String key) { return service.standardVersion(key,id,b); }
    /** D18-API-017: 发布候选扩展; permission=standard:publish. */
    @DesignLink(api="D18-API-017",requirements={"FR-STANDARD-001"},tests={"TC-STANDARD-001","TC-STANDARD-003"})
    @PostMapping("/standard-versions/{id}/publish") public Object api017(@PathVariable String id,@RequestBody StandardService.Publish b,@RequestHeader(value="X-Request-Id",required=false) String key) { return service.publishStandard(key,id,b); }
    /** D18-API-018: 规则快照目录; permission=rule:read. */
    @DesignLink(api="D18-API-018",requirements={"FR-RULE-001"},tests={"TC-RULE-001","TC-STANDARD-004"})
    @GetMapping("/rules") public Object api018(@RequestParam(required=false) String orgId,@RequestParam(required=false) String fieldId,@RequestParam(required=false) String keyword,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.list(true,orgId,fieldId,keyword,status,page,size); }
    /** D18-API-019: 规则编写候选扩展; permission=rule:write. */
    @DesignLink(api="D18-API-019",requirements={"FR-RULE-001","FR-RULE-002"},tests={"TC-RULE-001","TC-RULE-002"})
    @PostMapping("/rules/{id}/versions") public Object api019(@PathVariable String id,@RequestBody StandardService.RuleVersionInput b,@RequestHeader(value="X-Request-Id",required=false) String key) { return service.ruleVersion(key,id,b); }

    @DesignLink(api="D18-API-053",requirements={"FR-STANDARD-001"},tests={"TC-STANDARD-001","TC-STANDARD-004"})
    @PostMapping("/standards") public Object api053(@RequestBody StandardService.StandardInput b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.createStandard(key,b);}
    @DesignLink(api="D18-API-054",requirements={"FR-STANDARD-001"},tests={"TC-STANDARD-001","TC-STANDARD-004"})
    @GetMapping("/standards/{id}") public Object api054(@PathVariable String id){return service.detail(id,false);}
    @DesignLink(api="D18-API-055",requirements={"FR-STANDARD-001"},tests={"TC-STANDARD-003"})
    @PutMapping("/standard-versions/{id}") public Object api055(@PathVariable String id,@RequestBody StandardService.StandardEdit b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.editStandard(key,id,b);}
    @DesignLink(api="D18-API-056",requirements={"FR-RULE-001"},tests={"TC-RULE-001","TC-RULE-002"})
    @PostMapping("/rules") public Object api056(@RequestBody StandardService.RuleInput b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.createRule(key,b);}
    @DesignLink(api="D18-API-057",requirements={"FR-RULE-001"},tests={"TC-RULE-001","TC-STANDARD-004"})
    @GetMapping("/rules/{id}") public Object api057(@PathVariable String id){return service.detail(id,true);}
    @DesignLink(api="D18-API-058",requirements={"FR-RULE-001","FR-RULE-002"},tests={"TC-RULE-002","TC-RULE-003"})
    @PutMapping("/rule-versions/{id}") public Object api058(@PathVariable String id,@RequestBody StandardService.RuleEdit b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.editRule(key,id,b);}
    @DesignLink(api="D18-API-059",requirements={"FR-RULE-001","FR-RULE-002"},tests={"TC-RULE-001","TC-RULE-003"})
    @PostMapping("/rule-versions/{id}/publish") public Object api059(@PathVariable String id,@RequestBody StandardService.Publish b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.publishRule(key,id,b);}
    @DesignLink(api="D18-API-060",requirements={"FR-STANDARD-002"},tests={"TC-STANDARD-002","TC-STANDARD-004"})
    @PostMapping("/fields/{id}/standard-bindings") public Object api060(@PathVariable String id,@RequestBody StandardService.Binding b,@RequestHeader(value="X-Request-Id",required=false) String key){return service.bind(key,id,b);}
}
