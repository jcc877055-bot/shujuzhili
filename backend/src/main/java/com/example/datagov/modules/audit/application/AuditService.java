package com.example.datagov.modules.audit.application;
import com.example.datagov.common.*;
import com.example.datagov.infrastructure.Db;
import com.example.datagov.infrastructure.idempotency.CommandExecutor;
import com.example.datagov.modules.dashboard.application.GovernanceWindow;
import com.example.datagov.security.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import static com.example.datagov.common.Checks.*;

/** D15-M08; D17-T29/T30. Read-only business/security timelines and bounded audited CSV export. */
@Service public class AuditService {
 private final Db db;private final GovernanceWindow windows;private final JsonSupport json;private final CommandExecutor commands;private final AuditWriter audit;
 public AuditService(Db db,GovernanceWindow windows,JsonSupport json,CommandExecutor commands,AuditWriter audit){this.db=db;this.windows=windows;this.json=json;this.commands=commands;this.audit=audit;}
 public record Filter(String kind,String orgId,String objectType,String objectId,String actorId,String action,String outcome,String traceId,Instant from,Instant to){}
 public record Export(Filter filters,Integer limit){}
 private record Query(String table,String where,List<Object> args,GovernanceWindow.Window window,boolean security){}
 private Query query(Filter f){
  String kind=f.kind()==null?"BUSINESS":f.kind();require(Set.of("BUSINESS","SECURITY").contains(kind),400,ErrorCode.INVALID_REQUEST,"日志类型无效");boolean security=kind.equals("SECURITY");var win=windows.resolve(f.from(),f.to(),null,f.orgId());List<Object> args=new ArrayList<>();String where=win.sql("a",args)+" AND a.created_at>=? AND a.created_at<?";args.add(java.sql.Timestamp.from(win.from()));args.add(java.sql.Timestamp.from(win.to()));
  if(f.actorId()!=null){where+=" AND a.actor_id=?";args.add(id(f.actorId()));}if(f.action()!=null){where+=" AND a."+(security?"event_code":"action")+"=?";args.add(text(f.action(),security?64:96));}if(f.outcome()!=null){require(Set.of("SUCCESS","DENIED","ERROR").contains(f.outcome()),400,ErrorCode.INVALID_REQUEST,"结果类型无效");where+=" AND a.outcome=?";args.add(f.outcome());}if(f.traceId()!=null){where+=" AND a.trace_id=?";args.add(text(f.traceId(),64));}
  if(f.objectType()!=null){require(!security,400,ErrorCode.INVALID_REQUEST,"安全事件不支持业务对象过滤");where+=" AND a.object_type=?";args.add(text(f.objectType(),64));}if(f.objectId()!=null){require(!security,400,ErrorCode.INVALID_REQUEST,"安全事件不支持业务对象ID过滤");where+=" AND a.object_id=?";args.add(id(f.objectId()));}
  return new Query(security?"security_event":"audit_log",where,args,win,security);
 }
 private String select(Query q){return q.security()?"a.id,a.created_at,a.org_id,a.actor_id,a.trace_id,a.event_code AS action,a.object_ref,a.outcome,a.details":"a.*";}
 private Map<String,Object> present(Map<String,Object> row,boolean security){var out=Db.dto(row);for(String key:List.of("beforeJson","afterJson","details"))if(out.get(key) instanceof String s)out.put(key,json.parse(s));out.put("kind",security?"SECURITY":"BUSINESS");return out;}
 @Transactional(readOnly=true) public Object list(Filter f,int page,int size){PrincipalContext.current().permission("audit:read");Db.paging(page,size);var q=query(f);long total=db.count("SELECT COUNT(*) FROM "+q.table()+" a WHERE "+q.where(),q.args().toArray());q.args().add(size);q.args().add((page-1)*size);return Map.of("items",db.list("SELECT "+select(q)+" FROM "+q.table()+" a WHERE "+q.where()+" ORDER BY a.created_at DESC,a.id DESC LIMIT ? OFFSET ?",q.args().toArray()).stream().map(r->present(r,q.security())).toList(),"page",page,"size",size,"total",total,"window",q.window().dto());}
 public Object export(String key,Export b){return commands.execute("audit:export",key,b,()->{
  PrincipalContext.current().permission("audit:read");require(b.filters()!=null,400,ErrorCode.INVALID_REQUEST,"导出需指定过滤条件");int limit=b.limit()==null?2000:b.limit();require(limit>=1&&limit<=2000,400,ErrorCode.INVALID_REQUEST,"导出最多2000条");var q=query(b.filters());require(!q.window().orgIds().isEmpty(),404,ErrorCode.NOT_FOUND,"没有可导出的组织范围");q.args().add(limit+1);var rows=db.list("SELECT "+select(q)+" FROM "+q.table()+" a WHERE "+q.where()+" ORDER BY a.created_at DESC,a.id DESC LIMIT ?",q.args().toArray());boolean truncated=rows.size()>limit;if(truncated)rows=rows.subList(0,limit);
  String[] columns={"id","created_at","org_id","actor_id","action","object_type","object_id","outcome","trace_id"};StringBuilder csv=new StringBuilder("\ufeff").append(String.join(",",columns)).append("\r\n");for(var row:rows){for(int i=0;i<columns.length;i++){if(i>0)csv.append(',');String value=row.get(columns[i])==null?"":String.valueOf(Db.dto(row).get(columns[i].replace("created_at","createdAt").replace("org_id","orgId").replace("actor_id","actorId").replace("object_type","objectType").replace("object_id","objectId").replace("trace_id","traceId")));if(value.matches("^[=+@\\-\\t\\r].*"))value="'"+value;csv.append('"').append(value.replace("\"","\"\"")).append('"');}csv.append("\r\n");}
  String org=b.filters().orgId()==null?q.window().orgIds().stream().sorted().findFirst().orElseThrow():b.filters().orgId();audit.write(org,"AUDIT_EXPORT","audit_export",PrincipalContext.current().id(),null,Map.of("rowCount",rows.size(),"truncated",truncated,"kind",q.security()?"SECURITY":"BUSINESS"),"生成有限范围CSV，不导出变更JSON或业务原值");return Map.of("filename","governance-audit-"+Instant.now().toEpochMilli()+".csv","contentType","text/csv;charset=UTF-8","text",csv.toString(),"rowCount",rows.size(),"truncated",truncated,"window",q.window().dto());
 });}
}
