package com.example.datagov.modules.audit.infrastructure;
/** SQL names and D17 IDs; never accept table names from client input. */
public final class AuditTables {
 private AuditTables() {}
 public static final String AUDIT_LOG = "audit_log"; // D17-T29
 public static final String SECURITY_EVENT = "security_event"; // D17-T30
 public static final String IDEMPOTENCY_RECORD = "idempotency_record"; // D17-T31
 public static final String NOTIFICATION_OUTBOX = "notification_outbox"; // D17-T32
}
