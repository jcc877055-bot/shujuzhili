package com.example.datagov.modules.dashboard.application;
import com.example.datagov.common.*;
import com.example.datagov.security.*;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;
import static com.example.datagov.common.Checks.*;

/** Shared UTC window and explicit ORG scope for audit/statistical queries. */
@Component public class GovernanceWindow {
 private final ScopePolicy scope;public GovernanceWindow(ScopePolicy scope){this.scope=scope;}
 public record Window(Instant from,Instant to,Instant asOf,Set<String> orgIds){
  public String sql(String alias,List<Object> args){if(orgIds.isEmpty())return "1=0";args.addAll(orgIds);return alias+".org_id IN ("+String.join(",",Collections.nCopies(orgIds.size(),"?"))+")";}
  public Map<String,Object> dto(){return Map.of("from",from.toString(),"to",to.toString(),"asOf",asOf.toString(),"orgIds",orgIds.stream().sorted().toList(),"timeZone","UTC","interval","[from,to)");}
 }
 public Window resolve(Instant from,Instant to,Instant asOf,String org){Instant now=Instant.now();if(asOf==null)asOf=now;require(!asOf.isAfter(now.plusSeconds(5)),400,ErrorCode.INVALID_REQUEST,"asOf不能晚于当前时间");if(to==null)to=asOf;if(from==null)from=to.minus(Duration.ofDays(30));require(from.isBefore(to)&&!to.isAfter(asOf)&&Duration.between(from,to).compareTo(Duration.ofDays(366))<=0,400,ErrorCode.INVALID_REQUEST,"时间窗口需有效、不超过366天且to不晚于asOf");var ids=org==null?PrincipalContext.current().orgScopes():Set.of(id(org));if(org!=null)scope.org(org);return new Window(from,to,asOf,ids);}
}
