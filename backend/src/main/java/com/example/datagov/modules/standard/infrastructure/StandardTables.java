package com.example.datagov.modules.standard.infrastructure;
/** SQL names and D17 IDs; never accept table names from client input. */
public final class StandardTables {
 private StandardTables() {}
 public static final String BUSINESS_TERM = "business_term"; // D17-T12
 public static final String DATA_STANDARD = "data_standard"; // D17-T13
 public static final String STANDARD_VERSION = "standard_version"; // D17-T14
 public static final String FIELD_STANDARD = "field_standard"; // D17-T15
 public static final String QUALITY_RULE = "quality_rule"; // D17-T16
 public static final String RULE_VERSION = "rule_version"; // D17-T17
}
