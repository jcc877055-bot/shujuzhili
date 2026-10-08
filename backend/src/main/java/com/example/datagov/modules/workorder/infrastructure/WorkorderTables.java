package com.example.datagov.modules.workorder.infrastructure;
/** SQL names and D17 IDs; never accept table names from client input. */
public final class WorkorderTables {
 private WorkorderTables() {}
 public static final String GOVERNANCE_WORK_ORDER = "governance_work_order"; // D17-T21
 public static final String REMEDIATION_ROUND = "remediation_round"; // D17-T22
 public static final String TRANSFER_REQUEST = "transfer_request"; // D17-T27
 public static final String WORK_ORDER_HISTORY = "work_order_history"; // D17-T28
}
