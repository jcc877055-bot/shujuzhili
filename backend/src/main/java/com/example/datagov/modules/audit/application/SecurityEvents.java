package com.example.datagov.modules.audit.application;
import com.example.datagov.infrastructure.Db;
import com.example.datagov.security.PrincipalContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service public class SecurityEvents {
 private final Db db;public SecurityEvents(Db db) { this.db=db; }
 @Transactional(propagation=Propagation.REQUIRES_NEW) public void record(String trace,String code,PrincipalContext p) { db.update("INSERT INTO security_event(org_id,actor_id,trace_id,event_code,outcome) VALUES(?,?,?,?,?)",p==null?null:p.orgId(),p==null?null:p.id(),trace,code,"INTERNAL_ERROR".equals(code)?"ERROR":"DENIED"); }
}
