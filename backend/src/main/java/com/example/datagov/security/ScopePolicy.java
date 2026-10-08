package com.example.datagov.security;
import com.example.datagov.common.*;
import com.example.datagov.infrastructure.Db;
import org.springframework.stereotype.Component;
import java.util.*;
@Component public class ScopePolicy {
 private final Db db; public ScopePolicy(Db db) { this.db=db; }
 public Set<String> descendants(String root) { Set<String> ids=new HashSet<>();ArrayDeque<String> q=new ArrayDeque<>();q.add(root);while(!q.isEmpty()) { String id=q.remove();if(!ids.add(id))continue;Checks.require(ids.size()<=10000,422,ErrorCode.INVALID_ASSOCIATION,"组织范围过大"); db.list("SELECT id FROM org WHERE parent_id=?",id).forEach(r->q.add(Db.str(r,"id"))); }return ids; }
 public static boolean canRead(PrincipalContext p,String org,String assignee,boolean requestedReviewer) { return p.orgScopes().contains(org)||(p.assignedScopes().contains(org)&&(Objects.equals(p.id(),assignee)||requestedReviewer)); }
 public void org(String org) { Checks.require(PrincipalContext.current().orgScopes().contains(org),404,ErrorCode.NOT_FOUND,"资源不可见或不存在"); }
 public void order(Map<String,Object> w) { var p=PrincipalContext.current();boolean reviewer=db.count("SELECT COUNT(*) FROM review_task WHERE work_order_id=? AND requested_by=?",Db.str(w,"id"),p.id())>0;Checks.require(canRead(p,Db.str(w,"org_id"),Db.str(w,"assignee_id"),reviewer),404,ErrorCode.NOT_FOUND,"资源不可见或不存在"); }
 public String orderSql(PrincipalContext p,List<Object> args) { String result="("+orgSql(p.orgScopes(),args)+" OR ("+orgSql(p.assignedScopes(),args)+" AND (w.assignee_id=? OR EXISTS(SELECT 1 FROM review_task t WHERE t.work_order_id=w.id AND t.requested_by=?))))";args.add(p.id());args.add(p.id());return result; }
 public static String orgSql(Set<String> ids,List<Object> args) { if(ids.isEmpty())return "1=0";args.addAll(ids);return "w.org_id IN ("+String.join(",",Collections.nCopies(ids.size(),"?"))+")"; }
}
