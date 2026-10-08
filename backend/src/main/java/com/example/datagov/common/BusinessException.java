package com.example.datagov.common;
public class BusinessException extends RuntimeException {
 private final ErrorCode code; private final int status;
 public BusinessException(ErrorCode code, int status, String message) { super(message); this.code=code; this.status=status; }
 public ErrorCode code() { return code; } public int status() { return status; }
}
