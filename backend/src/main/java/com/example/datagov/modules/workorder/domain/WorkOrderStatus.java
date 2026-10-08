package com.example.datagov.modules.workorder.domain;
/** D16 states; overdue and canClose are derived attributes. */
public enum WorkOrderStatus { WAIT_DISPATCH, WAIT_CLAIM, PROCESSING, WAIT_REVIEW, CLOSED }
