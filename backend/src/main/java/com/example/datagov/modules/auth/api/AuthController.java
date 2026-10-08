package com.example.datagov.modules.auth.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.common.DesignLink;
import com.example.datagov.modules.auth.application.AuthService;
import java.util.*;
import java.time.Instant;

@RestController @RequestMapping("/api/v1")
public class AuthController {
 public record Login(String orgCode,String username,String password) {}
 public record Grants(List<String> roleIds,List<AuthService.ScopeGrant> scopes,long expectedVersion) {}
 private final AuthService service;
 public AuthController(AuthService service) { this.service=service; }
 @DesignLink(api="D18-API-001",requirements={"FR-AUTH-001","NFR-SEC-001"},tests={"TC-AUTH-001","TC-AUTH-002"})
 @PostMapping("/auth/login") public Object api001(@RequestBody Login b) { return service.login(b.orgCode(),b.username(),b.password()); }
 @DesignLink(api="D18-API-002",requirements={"NFR-SEC-001"},tests={"TC-AUTH-002"})
 @PostMapping("/auth/logout") public Object api002() { return service.logout(); }
 @DesignLink(api="D18-API-003",requirements={"PRM-001"},tests={"TC-AUTH-001"})
 @GetMapping("/me") public Object api003() { return service.me(); }
 @DesignLink(api="D18-API-004",requirements={"FR-WO-001","PRM-001"},tests={"TC-WO-001","TC-AUTH-003"})
 @GetMapping("/workspace/todos") public Object api004() { return service.todos(); }
 @DesignLink(api="D18-API-005",requirements={"FR-AUTH-001","PRM-001"},tests={"TC-AUTH-003"})
 @GetMapping("/orgs") public Object api005() { return service.orgs(); }
 @DesignLink(api="D18-API-006",requirements={"FR-AUTH-001","PRM-001"},tests={"TC-AUTH-003"})
 @PostMapping("/orgs") public Object api006(@RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.createOrg(key,(String)b.get("code"),(String)b.get("name"),(String)b.get("parentId"),b); }
 @DesignLink(api="D18-API-007",requirements={"FR-AUTH-001","PRM-001"},tests={"TC-AUTH-003"})
 @GetMapping("/users") public Object api007(@RequestParam(required=false) String orgId,@RequestParam(required=false) String status,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.users(orgId,status,page,size); }
 @DesignLink(api="D18-API-008",requirements={"FR-AUTH-001","NFR-SEC-001"},tests={"TC-AUTH-002"})
 @PostMapping("/users") public Object api008(@RequestBody Map<String,Object> b, @RequestHeader(value="X-Request-Id",required=false) String key) { return service.createUser(key,(String)b.get("orgId"),(String)b.get("username"),(String)b.get("displayName"),(String)b.get("password"),b); }
 @DesignLink(api="D18-API-009",requirements={"FR-AUTH-001","PRM-001"},tests={"TC-AUTH-004"})
 @PutMapping("/users/{id}/grants") public Object api009(@PathVariable String id, @RequestBody Grants b,@RequestHeader(value="X-Request-Id",required=false) String key) { return service.grants(key,id,b.roleIds(),b.scopes(),b.expectedVersion(),b); }
}
