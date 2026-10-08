package com.example.datagov.modules.quality.infrastructure;
/** SQL names and D17 IDs; never accept table names from client input. */
public final class QualityTables {
 private QualityTables() {}
 public static final String DETECTION_BATCH = "detection_batch"; // D17-T18
 public static final String QUALITY_ISSUE = "quality_issue"; // D17-T19
 public static final String ISSUE_IDENTITY = "issue_identity"; // D17-T20
}
