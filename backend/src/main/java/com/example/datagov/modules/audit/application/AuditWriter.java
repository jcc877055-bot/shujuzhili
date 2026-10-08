package com.example.datagov.modules.audit.application;
import com.example.datagov.common.*;
import com.example.datagov.infrastructure.Db;
import com.example.datagov.security.PrincipalContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.*;
/** AUD-001..003: successful writes join the business transaction. */
@Service public class AuditWriter {
 private final Db db;private final JsonSupport json;public AuditWriter(Db db,JsonSupport json) { this.db=db;this.json=json; }
 public static String trace() { var req=(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();return (String)req.getRequest().getAttribute("traceId"); }
 public void write(String org,String action,String type,String id,Object before,Object after,String reason) { var p=PrincipalContext.current();db.update("INSERT INTO audit_log(org_id,actor_id,action,object_type,object_id,trace_id,before_json,after_json,reason,outcome) VALUES(?,?,?,?,?,?,?,?,?,'SUCCESS')",org,p.id(),action,type,id,trace(),before==null?null:json.json(before),after==null?null:json.json(after),reason); }
 public void history(java.util.Map<String,Object> order,String old,String next,String action,String reason,String reviewId) { var p=PrincipalContext.current();db.update("INSERT INTO work_order_history(work_order_id,round_no,from_status,to_status,action,operator_id,reason,review_result_id,trace_id) VALUES(?,?,?,?,?,?,?,?,?)",Db.str(order,"id"),Db.num(order,"current_round"),old,next,action,p.id(),reason,reviewId,trace()); }
}
