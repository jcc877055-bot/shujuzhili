package com.example.datagov.common;
public record ApiResponse<T>(String code, String message, T data, String traceId) {}
