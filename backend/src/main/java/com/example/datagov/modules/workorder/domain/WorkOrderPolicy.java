package com.example.datagov.modules.workorder.domain;
import com.example.datagov.common.*;
import static com.example.datagov.common.Checks.*;
/** FR-WO / FR-REVIEW: domain checks shared by the real command handlers. */
public final class WorkOrderPolicy {
 private WorkOrderPolicy() {}
 public static void state(String actual,String expected) { require(expected.equals(actual),409,ErrorCode.STATE_CONFLICT,"工单状态不允许此操作"); }
 public static void owner(String actor,String assignee) { require(actor.equals(assignee),403,ErrorCode.FORBIDDEN,"仅当前责任人可以操作"); }
 public static void round(long actual,long expected) { require(actual==expected,409,ErrorCode.VERSION_CONFLICT,"整改轮次已改变"); }
 public static void independent(String actor,String submitter) { require(!actor.equals(submitter),403,ErrorCode.FORBIDDEN,"复检人必须独立于本轮整改提交人"); }
 public record Transition(String state,long round,boolean validPass) {}
 public static Transition review(String result,long round) { return switch(result) { case "PASS"->new Transition("WAIT_REVIEW",round,true);case "FAIL"->new Transition("PROCESSING",round+1,false);case "ERROR"->new Transition("WAIT_REVIEW",round,false);default->throw new BusinessException(ErrorCode.INVALID_REQUEST,400,"复检结论只能为PASS、FAIL或ERROR"); }; }
}
