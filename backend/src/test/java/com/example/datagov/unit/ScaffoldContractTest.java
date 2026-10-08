package com.example.datagov.unit;
import com.example.datagov.common.*;import com.example.datagov.modules.workorder.domain.WorkOrderPolicy;import com.example.datagov.security.*;
import org.junit.jupiter.api.Test;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import com.fasterxml.jackson.databind.ObjectMapper;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
/** TC-AUTH-002/003; TC-REVIEW-002/003/004; TC-STATE-001/003. HTTP tests supplement these policy tests. */
class ScaffoldContractTest {
 @Test void independentReviewerCannotBeTheSubmitter(){assertThrows(BusinessException.class,()->WorkOrderPolicy.independent("17","17"));assertDoesNotThrow(()->WorkOrderPolicy.independent("18","17"));}
 @Test void nonAssigneeCannotClaim(){assertThrows(BusinessException.class,()->WorkOrderPolicy.owner("18","17"));}
 @Test void invalidStateAndOldRoundAreRejected(){assertThrows(BusinessException.class,()->WorkOrderPolicy.state("PROCESSING","WAIT_REVIEW"));assertThrows(BusinessException.class,()->WorkOrderPolicy.round(2,1));}
 @Test void failStartsNewRoundAndClearsPass(){var x=WorkOrderPolicy.review("FAIL",2);assertEquals("PROCESSING",x.state());assertEquals(3,x.round());assertFalse(x.validPass());}
 @Test void technicalErrorKeepsRoundAndCannotClose(){var x=WorkOrderPolicy.review("ERROR",2);assertEquals("WAIT_REVIEW",x.state());assertEquals(2,x.round());assertFalse(x.validPass());}
 @Test void passStillRequiresExplicitClose(){var x=WorkOrderPolicy.review("PASS",2);assertEquals("WAIT_REVIEW",x.state());assertTrue(x.validPass());}
 @Test void assignedScopeNeverGrantsAnotherUsersOrder(){var p=new PrincipalContext("17","1","A",1,"hash",Set.of(),Set.of(),Set.of("1"),List.of(),List.of());assertTrue(ScopePolicy.canRead(p,"1","17",false));assertFalse(ScopePolicy.canRead(p,"1","18",false));assertFalse(ScopePolicy.canRead(p,"2","17",true));assertTrue(ScopePolicy.canRead(p,"1","18",true));}
 @Test void idempotencyCanonicalizesObjectsButPreservesArrayOrder(){var json=new JsonSupport(new ObjectMapper());assertEquals(json.canonical(Map.of("a",1,"b",2)),json.canonical(new LinkedHashMap<>(Map.of("b",2,"a",1))));assertNotEquals(json.canonical(List.of(1,2)),json.canonical(List.of(2,1)));}
 @Test void passwordHashesAreSaltedAndVerifyOnlyCorrectPassword(){var p=new BCryptPasswordEncoder(10);String raw="synthetic-password-only";var a=p.encode(raw);var b=p.encode(raw);assertNotEquals(a,b);assertTrue(p.matches(raw,a));assertFalse(p.matches("wrong",a));assertNotEquals(raw,a);}
 @Test void unsignedIdentifiersRetainPrecisionAndRejectInjection(){assertEquals("18446744073709551615",Checks.id("18446744073709551615"));assertThrows(BusinessException.class,()->Checks.id("18446744073709551616"));assertThrows(BusinessException.class,()->Checks.id("1 OR 1=1"));}
}
