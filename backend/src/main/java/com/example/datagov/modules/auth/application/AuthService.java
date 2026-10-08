package com.example.datagov.modules.auth.application;
import com.example.datagov.common.*;
import com.example.datagov.infrastructure.Db;
import com.example.datagov.infrastructure.idempotency.CommandExecutor;
import com.example.datagov.modules.audit.application.*;
import com.example.datagov.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static com.example.datagov.common.Checks.*;

/** D15-M01; FR-AUTH-001 / PRM-001..004. No built-in production account. */
@Service public class AuthService {
 private final Db db;private final PasswordEncoder passwords;private final ScopePolicy scope;private final AuditWriter audit;private final CommandExecutor commands;private final Duration ttl;
 private final String dummyHash;private final ConcurrentHashMap<String,Failures> failures=new ConcurrentHashMap<>();
 private record Failures(int count,Instant expires) {}
 public AuthService(Db db,PasswordEncoder passwords,ScopePolicy scope,AuditWriter audit,CommandExecutor commands,@Value("${app.candidate.session-ttl}") Duration ttl) { this.db=db;this.passwords=passwords;this.scope=scope;this.audit=audit;this.commands=commands;this.ttl=ttl;dummyHash=passwords.encode(UUID.randomUUID().toString()); }
 @Transactional public Object login(String orgCode,String username,String password) {
  text(orgCode,64);text(username,64);text(password,72);require(password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72,400,ErrorCode.INVALID_REQUEST,"密码UTF-8长度不能超过72字节");String bucket=sha256((orgCode+"\n"+username).getBytes(java.nio.charset.StandardCharsets.UTF_8));Instant now=Instant.now();
  failures.entrySet().removeIf(e->e.getValue().expires().isBefore(now));var blocked=failures.get(bucket);
  if(blocked!=null&&blocked.count()>=5)throw new BusinessException(ErrorCode.UNAUTHENTICATED,401,"组织、账号或密码不正确");
  var rows=db.list("SELECT u.* FROM user_account u JOIN org o ON o.id=u.org_id WHERE o.code=? AND u.username=? AND u.status='ENABLED' AND o.status='ENABLED'",orgCode,username);
  boolean match=passwords.matches(password,rows.isEmpty()?dummyHash:Db.str(rows.get(0),"password_hash"));
  if(rows.isEmpty()||!match) { require(failures.size()<10000||failures.containsKey(bucket),429,ErrorCode.FORBIDDEN,"登录请求过多，请稍后重试");failures.compute(bucket,(k,v)->new Failures(v==null?1:v.count()+1,now.plusSeconds(900)));throw new BusinessException(ErrorCode.UNAUTHENTICATED,401,"组织、账号或密码不正确"); }
  failures.remove(bucket);var row=rows.get(0);byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);String hash=sha256(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));Instant expiry=now.plus(ttl);
  db.insert("INSERT INTO auth_session(user_id,token_hash,permission_version,expires_at) VALUES(?,?,?,?)",Db.str(row,"id"),hash,Db.num(row,"permission_version"),expiry);
  db.update("INSERT INTO security_event(org_id,actor_id,trace_id,event_code,outcome) VALUES(?,?,?,'LOGIN_SUCCESS','SUCCESS')",Db.str(row,"org_id"),Db.str(row,"id"),AuditWriter.trace());
  return Map.of("accessToken",token,"expiresAt",expiry.toString(),"user",me(load(row,hash)));
 }
 public PrincipalContext authenticate(String token) {
  if(token==null||!token.matches("[A-Za-z0-9_-]{43}"))return null;String hash=sha256(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  var rows=db.list("SELECT u.* FROM auth_session s JOIN user_account u ON u.id=s.user_id JOIN org o ON o.id=u.org_id WHERE s.token_hash=? AND s.revoked_at IS NULL AND s.expires_at>UTC_TIMESTAMP(3) AND s.permission_version=u.permission_version AND u.status='ENABLED' AND o.status='ENABLED'",hash);
  return rows.size()==1?load(rows.get(0),hash):null;
 }
 private PrincipalContext load(Map<String,Object> row,String hash) {
  String id=Db.str(row,"id");Set<String> permissions=new HashSet<>();db.list("SELECT DISTINCT p.code FROM permission p JOIN role_permission rp ON rp.permission_id=p.id JOIN role r ON r.id=rp.role_id JOIN user_role ur ON ur.role_id=r.id WHERE ur.user_id=? AND r.status='ENABLED'",id).forEach(r->permissions.add(Db.str(r,"code")));
  var scopes=db.list("SELECT org_id,include_descendants,scope_type FROM user_scope WHERE user_id=?",id);Set<String> orgs=new HashSet<>(),assigned=new HashSet<>();
  for(var s:scopes) { String org=Db.str(s,"org_id");var ids=Db.num(s,"include_descendants")==1?scope.descendants(org):Set.of(org);if("ORG".equals(s.get("scope_type")))orgs.addAll(ids);else assigned.addAll(ids); }
  var roles=db.list("SELECT r.code FROM role r JOIN user_role ur ON ur.role_id=r.id WHERE ur.user_id=? AND r.status='ENABLED'",id).stream().map(r->Db.str(r,"code")).toList();
  return new PrincipalContext(id,Db.str(row,"org_id"),Db.str(row,"display_name"),Db.num(row,"permission_version"),hash,Set.copyOf(permissions),Set.copyOf(orgs),Set.copyOf(assigned),scopes.stream().map(Db::dto).toList(),roles);
 }
 @Transactional public Object logout() { var p=PrincipalContext.current();db.update("UPDATE auth_session SET revoked_at=UTC_TIMESTAMP(3),version=version+1,updated_at=UTC_TIMESTAMP(3) WHERE token_hash=? AND revoked_at IS NULL",p.tokenHash());db.update("INSERT INTO security_event(org_id,actor_id,trace_id,event_code,outcome) VALUES(?,?,?,'LOGOUT_SUCCESS','SUCCESS')",p.orgId(),p.id(),AuditWriter.trace());return Map.of("revoked",true); }
 public Object todos() { var p=PrincipalContext.current();p.permission("workorder:read");List<Object> args=new ArrayList<>();String where=scope.orderSql(p,args);return db.list("SELECT w.id,w.order_no,w.status,w.current_round,w.due_at,w.assignee_id FROM governance_work_order w WHERE "+where+" AND w.status<>'CLOSED' ORDER BY w.due_at,w.id LIMIT 100",args.toArray()).stream().map(Db::dto).toList(); }
 public Object me() { return me(PrincipalContext.current()); }
 private Object me(PrincipalContext p) { Map<String,Object> result=new LinkedHashMap<>();result.put("id",p.id());result.put("orgId",p.orgId());result.put("displayName",p.displayName());result.put("roleCodes",p.roleCodes());result.put("permissions",p.permissions());result.put("scopes",p.scopes());
  if(p.permissions().contains("auth:grant"))result.put("roleCatalog",db.list("SELECT id,code,name FROM role WHERE status='ENABLED'").stream().filter(r->rolePermissions(Db.str(r,"id")).stream().allMatch(p.permissions()::contains)).map(Db::dto).toList());return result;
 }
 private Set<String> rolePermissions(String roleId) { Set<String> result=new HashSet<>();db.list("SELECT p.code FROM permission p JOIN role_permission rp ON rp.permission_id=p.id WHERE rp.role_id=?",roleId).forEach(r->result.add(Db.str(r,"code")));return result; }
 public Object orgs() { var p=PrincipalContext.current();p.permission("auth:read");return db.list("SELECT id,parent_id,code,name,status,version FROM org ORDER BY id").stream().filter(r->p.orgScopes().contains(Db.str(r,"id"))).map(Db::dto).toList(); }
 public Object createOrg(String key,String code,String name,String parentId,Object body) { return commands.execute("auth:grant",key,body,()->{id(parentId);scope.org(parentId);db.one("SELECT id FROM org WHERE id=? AND status='ENABLED'",parentId);String org=db.insert("INSERT INTO org(parent_id,code,name,status) VALUES(?,?,?,'ENABLED')",parentId,text(code,64),text(name,128));audit.write(parentId,"ORG_CREATE","org",org,null,Map.of("code",code,"name",name),"新增组织");return Db.dto(db.one("SELECT id,parent_id,code,name,status,version FROM org WHERE id=?",org));}); }
 public Object users(String orgId,String status,int page,int size) { var p=PrincipalContext.current();p.permission("auth:read");Db.paging(page,size);var ids=orgId==null?p.orgScopes():Set.of(id(orgId));if(orgId!=null)scope.org(orgId);if(ids.isEmpty())return Db.page(List.of(),page,size,0);List<Object> args=new ArrayList<>(ids);String where="org_id IN ("+String.join(",",Collections.nCopies(ids.size(),"?"))+")";if(status!=null){require(Set.of("ENABLED","DISABLED").contains(status),400,ErrorCode.INVALID_REQUEST,"账号状态无效");where+=" AND status=?";args.add(status);}long total=db.count("SELECT COUNT(*) FROM user_account WHERE "+where,args.toArray());List<Object> pageArgs=new ArrayList<>(args);pageArgs.add(size);pageArgs.add((page-1)*size);var rows=db.list("SELECT id,org_id,username,display_name,status,permission_version,version FROM user_account WHERE "+where+" ORDER BY id LIMIT ? OFFSET ?",pageArgs.toArray());for(var row:rows){row.put("role_ids",db.list("SELECT role_id FROM user_role WHERE user_id=?",Db.str(row,"id")).stream().map(r->Db.str(r,"role_id")).toList());row.put("scopes",db.list("SELECT org_id,include_descendants,scope_type FROM user_scope WHERE user_id=?",Db.str(row,"id")).stream().map(Db::dto).toList());}return Db.page(rows,page,size,total); }
 public Object createUser(String key,String orgId,String username,String displayName,String password,Object body) { return commands.execute("auth:grant",key,body,()->{id(orgId);scope.org(orgId);db.one("SELECT id FROM org WHERE id=? AND status='ENABLED'",orgId);text(password,72);require(password.length()>=12&&password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72,400,ErrorCode.INVALID_REQUEST,"密码至少12字符，UTF-8长度不超过72字节");String user=db.insert("INSERT INTO user_account(org_id,username,password_hash,display_name,status,permission_version) VALUES(?,?,?,?,'ENABLED',1)",orgId,text(username,64),passwords.encode(password),text(displayName,64));audit.write(orgId,"USER_CREATE","user_account",user,null,Map.of("displayName",displayName),"新增账号");return Db.dto(db.one("SELECT id,org_id,username,display_name,status,version FROM user_account WHERE id=?",user));}); }
 public record ScopeGrant(String orgId,boolean includeDescendants,String scopeType) {}
 public Object grants(String key,String userId,List<String> roleIds,List<ScopeGrant> scopes,long expectedVersion,Object body) { return commands.execute("auth:grant",key,body,()->{id(userId);var user=db.one("SELECT id,org_id,version FROM user_account WHERE id=? FOR UPDATE",userId);scope.org(Db.str(user,"org_id"));version(Db.num(user,"version"),expectedVersion);var p=PrincipalContext.current();require(roleIds!=null&&scopes!=null&&roleIds.size()<=7&&scopes.size()<=100,400,ErrorCode.INVALID_REQUEST,"授权列表无效");Set<String> unique=new HashSet<>();for(String role:roleIds){id(role);require(unique.add(role),400,ErrorCode.INVALID_REQUEST,"重复角色");db.one("SELECT id FROM role WHERE id=? AND status='ENABLED'",role);require(p.permissions().containsAll(rolePermissions(role)),403,ErrorCode.FORBIDDEN,"不能授予超过自身的动作权限");}
   Set<String> scopeKeys=new HashSet<>();for(var s:scopes){id(s.orgId());require(s!=null&&s.scopeType()!=null&&Set.of("ORG","ASSIGNED").contains(s.scopeType())&&scopeKeys.add(s.orgId()+s.scopeType()),400,ErrorCode.INVALID_REQUEST,"重复或无效范围");db.one("SELECT id FROM org WHERE id=? AND status='ENABLED'",s.orgId());var ids=s.includeDescendants()?scope.descendants(s.orgId()):Set.of(s.orgId());require(p.orgScopes().containsAll(ids),403,ErrorCode.FORBIDDEN,"不能授予超过自身的组织范围");}
   db.update("DELETE FROM user_role WHERE user_id=?",userId);db.update("DELETE FROM user_scope WHERE user_id=?",userId);for(String role:roleIds)db.update("INSERT INTO user_role(user_id,role_id) VALUES(?,?)",userId,role);for(var s:scopes)db.update("INSERT INTO user_scope(user_id,org_id,include_descendants,scope_type) VALUES(?,?,?,?)",userId,s.orgId(),s.includeDescendants()?1:0,s.scopeType());db.update("UPDATE user_account SET permission_version=permission_version+1,version=version+1,updated_at=UTC_TIMESTAMP(3) WHERE id=?",userId);audit.write(Db.str(user,"org_id"),"USER_GRANT","user_account",userId,null,Map.of("roleIds",roleIds,"scopes",scopes),"替换角色和组织范围");return Map.of("id",userId,"version",expectedVersion+1,"sessionsInvalidated",true);
  }); }
}
