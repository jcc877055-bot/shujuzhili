import type { Endpoint } from "../types/api"
export const endpoints = {
  "D18-API-001": {
    "id": "D18-API-001",
    "method": "POST",
    "path": "/api/v1/auth/login",
    "permission": "公开"
  },
  "D18-API-002": {
    "id": "D18-API-002",
    "method": "POST",
    "path": "/api/v1/auth/logout",
    "permission": "登录"
  },
  "D18-API-003": {
    "id": "D18-API-003",
    "method": "GET",
    "path": "/api/v1/me",
    "permission": "登录"
  },
  "D18-API-004": {
    "id": "D18-API-004",
    "method": "GET",
    "path": "/api/v1/workspace/todos",
    "permission": "登录"
  },
  "D18-API-005": {
    "id": "D18-API-005",
    "method": "GET",
    "path": "/api/v1/orgs",
    "permission": "auth:read"
  },
  "D18-API-006": {
    "id": "D18-API-006",
    "method": "POST",
    "path": "/api/v1/orgs",
    "permission": "auth:grant"
  },
  "D18-API-007": {
    "id": "D18-API-007",
    "method": "GET",
    "path": "/api/v1/users",
    "permission": "auth:read"
  },
  "D18-API-008": {
    "id": "D18-API-008",
    "method": "POST",
    "path": "/api/v1/users",
    "permission": "auth:grant"
  },
  "D18-API-009": {
    "id": "D18-API-009",
    "method": "PUT",
    "path": "/api/v1/users/{id}/grants",
    "permission": "auth:grant"
  },
  "D18-API-010": {
    "id": "D18-API-010",
    "method": "GET",
    "path": "/api/v1/assets",
    "permission": "asset:read"
  },
  "D18-API-011": {
    "id": "D18-API-011",
    "method": "POST",
    "path": "/api/v1/assets",
    "permission": "asset:write"
  },
  "D18-API-012": {
    "id": "D18-API-012",
    "method": "GET",
    "path": "/api/v1/assets/{id}",
    "permission": "asset:read"
  },
  "D18-API-013": {
    "id": "D18-API-013",
    "method": "GET",
    "path": "/api/v1/assets/{id}/fields",
    "permission": "asset:read"
  },
  "D18-API-014": {
    "id": "D18-API-014",
    "method": "POST",
    "path": "/api/v1/assets/{id}/fields",
    "permission": "asset:write"
  },
  "D18-API-015": {
    "id": "D18-API-015",
    "method": "GET",
    "path": "/api/v1/standards",
    "permission": "standard:read"
  },
  "D18-API-016": {
    "id": "D18-API-016",
    "method": "POST",
    "path": "/api/v1/standards/{id}/versions",
    "permission": "standard:write"
  },
  "D18-API-017": {
    "id": "D18-API-017",
    "method": "POST",
    "path": "/api/v1/standard-versions/{id}/publish",
    "permission": "standard:publish"
  },
  "D18-API-018": {
    "id": "D18-API-018",
    "method": "GET",
    "path": "/api/v1/rules",
    "permission": "rule:read"
  },
  "D18-API-019": {
    "id": "D18-API-019",
    "method": "POST",
    "path": "/api/v1/rules/{id}/versions",
    "permission": "rule:write"
  },
  "D18-API-020": {
    "id": "D18-API-020",
    "method": "GET",
    "path": "/api/v1/detection-batches",
    "permission": "quality:read"
  },
  "D18-API-021": {
    "id": "D18-API-021",
    "method": "POST",
    "path": "/api/v1/detection-batches",
    "permission": "quality:register"
  },
  "D18-API-022": {
    "id": "D18-API-022",
    "method": "POST",
    "path": "/api/v1/detection-batches/{id}/run",
    "permission": "quality:execute"
  },
  "D18-API-023": {
    "id": "D18-API-023",
    "method": "GET",
    "path": "/api/v1/quality/issues",
    "permission": "quality:read"
  },
  "D18-API-024": {
    "id": "D18-API-024",
    "method": "POST",
    "path": "/api/v1/quality/issues",
    "permission": "quality:register"
  },
  "D18-API-025": {
    "id": "D18-API-025",
    "method": "GET",
    "path": "/api/v1/quality/issues/{id}",
    "permission": "quality:read"
  },
  "D18-API-026": {
    "id": "D18-API-026",
    "method": "POST",
    "path": "/api/v1/quality/issues/{id}/decision",
    "permission": "quality:decide"
  },
  "D18-API-027": {
    "id": "D18-API-027",
    "method": "GET",
    "path": "/api/v1/work-orders",
    "permission": "workorder:read"
  },
  "D18-API-028": {
    "id": "D18-API-028",
    "method": "POST",
    "path": "/api/v1/work-orders",
    "permission": "workorder:dispatch"
  },
  "D18-API-029": {
    "id": "D18-API-029",
    "method": "GET",
    "path": "/api/v1/work-orders/{id}",
    "permission": "workorder:read"
  },
  "D18-API-030": {
    "id": "D18-API-030",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/dispatch",
    "permission": "workorder:dispatch"
  },
  "D18-API-031": {
    "id": "D18-API-031",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/claim",
    "permission": "workorder:claim"
  },
  "D18-API-032": {
    "id": "D18-API-032",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/transfer-requests",
    "permission": "workorder:transfer"
  },
  "D18-API-033": {
    "id": "D18-API-033",
    "method": "POST",
    "path": "/api/v1/transfer-requests/{id}/decision",
    "permission": "workorder:approve-transfer"
  },
  "D18-API-034": {
    "id": "D18-API-034",
    "method": "POST",
    "path": "/api/v1/evidence-files",
    "permission": "evidence:upload"
  },
  "D18-API-035": {
    "id": "D18-API-035",
    "method": "GET",
    "path": "/api/v1/evidence-files/{id}/content",
    "permission": "evidence:read"
  },
  "D18-API-036": {
    "id": "D18-API-036",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/rounds",
    "permission": "workorder:submit"
  },
  "D18-API-037": {
    "id": "D18-API-037",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/review-tasks",
    "permission": "review:execute"
  },
  "D18-API-038": {
    "id": "D18-API-038",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/reviews",
    "permission": "review:execute"
  },
  "D18-API-039": {
    "id": "D18-API-039",
    "method": "POST",
    "path": "/api/v1/internal/review-tasks/{id}/result",
    "permission": "服务身份"
  },
  "D18-API-040": {
    "id": "D18-API-040",
    "method": "POST",
    "path": "/api/v1/work-orders/{id}/close",
    "permission": "workorder:close"
  },
  "D18-API-041": {
    "id": "D18-API-041",
    "method": "GET",
    "path": "/api/v1/work-orders/{id}/history",
    "permission": "workorder:read"
  },
  "D18-API-042": {
    "id": "D18-API-042",
    "method": "GET",
    "path": "/api/v1/audit-logs",
    "permission": "audit:read"
  },
  "D18-API-043": {
    "id": "D18-API-043",
    "method": "POST",
    "path": "/api/v1/audit-exports",
    "permission": "audit:export"
  },
  "D18-API-044": {
    "id": "D18-API-044",
    "method": "GET",
    "path": "/api/v1/dashboard/metrics",
    "permission": "dashboard:read"
  },
  "D18-API-045": {
    "id": "D18-API-045",
    "method": "GET",
    "path": "/api/v1/dashboard/drilldown",
    "permission": "dashboard:read"
  },
  "D18-API-046": {
    "id": "D18-API-046",
    "method": "PUT",
    "path": "/api/v1/assets/{id}",
    "permission": "asset:write"
  },
  "D18-API-047": {
    "id": "D18-API-047",
    "method": "POST",
    "path": "/api/v1/assets/{id}/publish",
    "permission": "asset:write"
  },
  "D18-API-048": {
    "id": "D18-API-048",
    "method": "POST",
    "path": "/api/v1/assets/{id}/disable",
    "permission": "asset:write"
  },
  "D18-API-049": {
    "id": "D18-API-049",
    "method": "PUT",
    "path": "/api/v1/assets/{id}/fields/{fieldId}",
    "permission": "asset:write"
  },
  "D18-API-050": {
    "id": "D18-API-050",
    "method": "GET",
    "path": "/api/v1/data-sources",
    "permission": "asset:read"
  },
  "D18-API-051": {
    "id": "D18-API-051",
    "method": "POST",
    "path": "/api/v1/data-sources",
    "permission": "asset:write"
  },
  "D18-API-052": {
    "id": "D18-API-052",
    "method": "PUT",
    "path": "/api/v1/data-sources/{id}",
    "permission": "asset:write"
  },
  "D18-API-053": {
    "id": "D18-API-053",
    "method": "POST",
    "path": "/api/v1/standards",
    "permission": "standard:write"
  },
  "D18-API-054": {
    "id": "D18-API-054",
    "method": "GET",
    "path": "/api/v1/standards/{id}",
    "permission": "standard:read"
  },
  "D18-API-055": {
    "id": "D18-API-055",
    "method": "PUT",
    "path": "/api/v1/standard-versions/{id}",
    "permission": "standard:write"
  },
  "D18-API-056": {
    "id": "D18-API-056",
    "method": "POST",
    "path": "/api/v1/rules",
    "permission": "rule:write"
  },
  "D18-API-057": {
    "id": "D18-API-057",
    "method": "GET",
    "path": "/api/v1/rules/{id}",
    "permission": "rule:read"
  },
  "D18-API-058": {
    "id": "D18-API-058",
    "method": "PUT",
    "path": "/api/v1/rule-versions/{id}",
    "permission": "rule:write"
  },
  "D18-API-059": {
    "id": "D18-API-059",
    "method": "POST",
    "path": "/api/v1/rule-versions/{id}/publish",
    "permission": "rule:publish"
  },
  "D18-API-060": {
    "id": "D18-API-060",
    "method": "POST",
    "path": "/api/v1/fields/{id}/standard-bindings",
    "permission": "standard:write"
  },
  "D18-API-061": {
    "id": "D18-API-061",
    "method": "GET",
    "path": "/api/v1/detection-batches/{id}",
    "permission": "quality:read"
  },
  "D18-API-062": {
    "id": "D18-API-062",
    "method": "POST",
    "path": "/api/v1/detection-batches/{id}/dataset",
    "permission": "quality:execute"
  },
  "D18-API-063": {
    "id": "D18-API-063",
    "method": "GET",
    "path": "/api/v1/detection-batches/{id}/findings",
    "permission": "quality:read"
  }
} as const satisfies Record<string, Endpoint>
