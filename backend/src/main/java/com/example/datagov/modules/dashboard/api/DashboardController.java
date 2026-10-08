package com.example.datagov.modules.dashboard.api;
import org.springframework.web.bind.annotation.*;
import com.example.datagov.modules.dashboard.application.DashboardService;
import com.example.datagov.common.DesignLink;
import java.time.Instant;
@RestController @RequestMapping("/api/v1")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service=service; }
    /** D18-API-044: 指标查询; permission=dashboard:read. */
    @DesignLink(api="D18-API-044",requirements={"FR-DASH-001","DATA-004","PRM-004"},tests={"TC-DASH-001","TC-DASH-002","TC-DASH-003","TC-DASH-004"})
    @GetMapping("/dashboard/metrics") public Object api044(@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,@RequestParam(required=false) Instant asOf,@RequestParam(required=false) String orgId) { return service.metrics(from,to,asOf,orgId); }
    /** D18-API-045: 同范围下钻; permission=dashboard:read. */
    @DesignLink(api="D18-API-045",requirements={"FR-DASH-001","DATA-004","PRM-004"},tests={"TC-DASH-001","TC-DASH-004"})
    @GetMapping("/dashboard/drilldown") public Object api045(@RequestParam String metricCode,@RequestParam String metricVersion,@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,@RequestParam(required=false) Instant asOf,@RequestParam(required=false) String orgId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) { return service.drilldown(metricCode,metricVersion,from,to,asOf,orgId,page,size); }
}
