package com.example.datagov.infrastructure.idempotency;
import com.example.datagov.common.*;
import com.example.datagov.infrastructure.Db;
import com.example.datagov.security.PrincipalContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
/** NFR-REL-001: replays, business state, history and audit commit atomically. */
@Service public class CommandExecutor {
 private final Db db;private final JsonSupport json;private final Duration retention;private final com.example.datagov.security.ScopePolicy scope;
 public CommandExecutor(Db db,JsonSupport json,com.example.datagov.security.ScopePolicy scope,@Value("${app.candidate.idempotency-retention}") Duration retention) { this.db=db;this.json=json;this.scope=scope;this.retention=retention; }
 @Transactional public Object execute(String permission,String key,Object body,Supplier<Object> action) {
  var p=PrincipalContext.current();p.permission(permission);Checks.require(key!=null&&key.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"),400,ErrorCode.INVALID_REQUEST,"写操作需要UUID格式X-Request-Id");
  var actor=db.one("SELECT permission_version,status FROM user_account WHERE id=? FOR UPDATE",p.id());
  Checks.require(Db.num(actor,"permission_version")==p.permissionVersion()&&"ENABLED".equals(actor.get("status"))&&db.count("SELECT COUNT(*) FROM auth_session WHERE token_hash=? AND revoked_at IS NULL AND expires_at>UTC_TIMESTAMP(3)",p.tokenHash())==1,401,ErrorCode.UNAUTHENTICATED,"会话已失效");
  var req=((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();String route=req.getMethod()+" "+req.getRequestURI();String hash=Checks.sha256(json.canonical(body).getBytes(StandardCharsets.UTF_8));String actorKey="USER:"+p.id();
  CommandFields.validate(req.getRequestURI(),body);
  var assetPath=java.util.regex.Pattern.compile("/api/v1/assets/([0-9]+)(?:/.*)?").matcher(req.getRequestURI());if(assetPath.matches())scope.org(Db.str(db.one("SELECT org_id FROM data_asset WHERE id=?",assetPath.group(1)),"org_id"));
  var sourcePath=java.util.regex.Pattern.compile("/api/v1/data-sources/([0-9]+)").matcher(req.getRequestURI());if(sourcePath.matches())scope.org(Db.str(db.one("SELECT org_id FROM data_source WHERE id=?",sourcePath.group(1)),"org_id"));
  String uri=req.getRequestURI();
  String[][] resources={{"standards","data_standard"},{"rules","quality_rule"},{"detection-batches","detection_batch"}};
  for(String[] resource:resources){var path=java.util.regex.Pattern.compile("/api/v1/"+resource[0]+"/([0-9]+)(?:/.*)?").matcher(uri);if(path.matches())scope.org(Db.str(db.one("SELECT org_id FROM "+resource[1]+" WHERE id=?",path.group(1)),"org_id"));}
  for(boolean rules:new boolean[]{false,true}){String entity=rules?"rule":"standard";var path=java.util.regex.Pattern.compile("/api/v1/"+entity+"-versions/([0-9]+)(?:/.*)?").matcher(uri);if(path.matches())scope.org(Db.str(db.one("SELECT p.org_id FROM "+entity+"_version v JOIN "+(rules?"quality_rule":"data_standard")+" p ON p.id=v."+entity+"_id WHERE v.id=?",path.group(1)),"org_id"));}
  var fieldPath=java.util.regex.Pattern.compile("/api/v1/fields/([0-9]+)/standard-bindings").matcher(uri);if(fieldPath.matches())scope.org(Db.str(db.one("SELECT a.org_id FROM metadata_field f JOIN data_asset a ON a.id=f.asset_id WHERE f.id=?",fieldPath.group(1)),"org_id"));
  if(Set.of("/api/v1/standards","/api/v1/rules","/api/v1/detection-batches").contains(uri)){var input=(Map<?,?>)json.parse(json.json(body));if(input.get("orgId") instanceof String oid)scope.org(Checks.id(oid));if(input.get("fieldId") instanceof String fid)scope.org(Db.str(db.one("SELECT a.org_id FROM metadata_field f JOIN data_asset a ON a.id=f.asset_id WHERE f.id=?",Checks.id(fid)),"org_id"));}
  var previous=db.list("SELECT request_hash,response_json FROM idempotency_record WHERE actor_key=? AND route_key=? AND request_key=?",actorKey,route,key.toLowerCase(Locale.ROOT));
  if(!previous.isEmpty()) { var match=java.util.regex.Pattern.compile("/api/v1/work-orders/([0-9]+)/.*").matcher(uri);if(match.matches())scope.order(db.one("SELECT * FROM governance_work_order WHERE id=?",match.group(1)));if(body instanceof Map<?,?> m&&m.get("workOrderId") instanceof String wid)scope.order(db.one("SELECT * FROM governance_work_order WHERE id=?",wid));Checks.require(hash.equals(previous.get(0).get("request_hash")),409,ErrorCode.IDEMPOTENCY_CONFLICT,"相同幂等键的请求内容不同");Object replay=json.parse(Db.str(previous.get(0),"response_json"));if(uri.equals("/api/v1/audit-exports")){p.permission("audit:read");if(replay instanceof Map<?,?> result&&result.get("window") instanceof Map<?,?> window&&window.get("orgIds") instanceof List<?> orgs)for(Object org:orgs)scope.org(String.valueOf(org));else throw new BusinessException(ErrorCode.NOT_FOUND,404,"原导出范围不可见，请使用新请求键");}return replay; }
  Object result=action.get();db.update("INSERT INTO idempotency_record(actor_key,route_key,request_key,request_hash,status,http_status,response_json,expires_at) VALUES(?,?,?,?,'SUCCEEDED',200,?,?)",actorKey,route,key.toLowerCase(Locale.ROOT),hash,json.json(result),java.sql.Timestamp.from(Instant.now().plus(retention)));return result;
 }
}
