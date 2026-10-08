package com.example.datagov.unit;

import com.example.datagov.common.BusinessException;
import com.example.datagov.modules.standard.domain.RulePolicy;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** TC-RULE-001..003 and TC-DETECTION-002..004: executable rule semantics. */
class RulePolicyTest {
 @Test void completenessTrimsWhitespace(){var p=RulePolicy.parameters("COMPLETENESS",Map.of("trim",true));assertEquals("MISSING_VALUE",RulePolicy.violation("COMPLETENESS","  ",null,p,Set.of()));assertNull(RulePolicy.violation("COMPLETENESS"," x ",null,p,Set.of()));}
 @Test void uniquenessNormalizesDecimalAndCaseOnlyWhenConfigured(){var p=RulePolicy.parameters("UNIQUENESS",Map.of("ignoreCase",true));assertEquals("DUPLICATE_VALUE",RulePolicy.violation("UNIQUENESS"," AbC ",null,p,Set.of("abc")));assertEquals(new BigDecimal("1"),RulePolicy.normalized(new BigDecimal("1.00"),p));}
 @Test void validityChecksRangeLengthEnumAndFormats(){var range=RulePolicy.parameters("VALIDITY",Map.of("min","1.5","max","9.5"));assertEquals("OUTSIDE_RANGE",RulePolicy.violation("VALIDITY","10",null,range,Set.of()));assertEquals("NOT_NUMERIC",RulePolicy.violation("VALIDITY","x",null,range,Set.of()));var email=RulePolicy.parameters("VALIDITY",Map.of("format","EMAIL"));assertNull(RulePolicy.violation("VALIDITY","a.b@example.com",null,email,Set.of()));assertEquals("INVALID_FORMAT",RulePolicy.violation("VALIDITY","bad",null,email,Set.of()));}
 @Test void dateValidationRejectsImpossibleCalendarDate(){var p=RulePolicy.parameters("VALIDITY",Map.of("format","ISO_DATE"));assertNull(RulePolicy.violation("VALIDITY","2026-09-20",null,p,Set.of()));assertEquals("INVALID_FORMAT",RulePolicy.violation("VALIDITY","2026-02-30",null,p,Set.of()));}
 @Test void consistencyHandlesEqualNotEqualAndMissingComparison(){var eq=RulePolicy.parameters("CONSISTENCY",Map.of("otherFieldId","1","operator","EQUALS","ignoreCase",true));assertNull(RulePolicy.violation("CONSISTENCY"," A ","a",eq,Set.of()));assertEquals("MISSING_COMPARISON",RulePolicy.violation("CONSISTENCY","a",null,eq,Set.of()));var ne=RulePolicy.parameters("CONSISTENCY",Map.of("otherFieldId","1","operator","NOT_EQUALS"));assertEquals("INCONSISTENT_VALUE",RulePolicy.violation("CONSISTENCY","a","a",ne,Set.of()));}
 @Test void invalidOrExecutableParametersAreRejected(){assertThrows(BusinessException.class,()->RulePolicy.parameters("VALIDITY",Map.of()));assertThrows(BusinessException.class,()->RulePolicy.parameters("VALIDITY",Map.of("regex",".*")));assertThrows(BusinessException.class,()->RulePolicy.parameters("VALIDITY",Map.of("min",9,"max",1)));assertThrows(BusinessException.class,()->RulePolicy.parameters("CONSISTENCY",Map.of("otherFieldId","1","operator","SQL")));}
}
