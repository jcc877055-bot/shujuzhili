package com.example.datagov.common;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
@RestControllerAdvice
public class ExceptionAdvice {
 private final com.example.datagov.modules.audit.application.SecurityEvents events;
 public ExceptionAdvice(com.example.datagov.modules.audit.application.SecurityEvents events){this.events=events;}
 private static final Logger LOG=LoggerFactory.getLogger(ExceptionAdvice.class);
 @ExceptionHandler(BusinessException.class) ResponseEntity<ApiResponse<Void>> business(BusinessException ex,HttpServletRequest req) { var a=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();var p=a!=null&&a.getPrincipal() instanceof com.example.datagov.security.PrincipalContext x?x:null;try{events.record((String)req.getAttribute("traceId"),ex.code().name(),p);}catch(Exception ignored){LOG.error("Security event unavailable, traceId={}",req.getAttribute("traceId"));}return ResponseEntity.status(ex.status()).body(new ApiResponse<>(ex.code().name(),ex.getMessage(),null,(String)req.getAttribute("traceId"))); }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentNotValidException.class,IllegalArgumentException.class,ClassCastException.class,org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.multipart.support.MissingServletRequestPartException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) ResponseEntity<ApiResponse<Void>> invalid(Exception ex,HttpServletRequest req) { return ResponseEntity.badRequest().body(new ApiResponse<>("INVALID_REQUEST","请求格式或字段不正确",null,(String)req.getAttribute("traceId"))); }
 @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class) ResponseEntity<ApiResponse<Void>> duplicate(Exception ex,HttpServletRequest req){return ResponseEntity.status(409).body(new ApiResponse<>("STATE_CONFLICT","记录已存在或并发状态冲突",null,(String)req.getAttribute("traceId")));}
 @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class) ResponseEntity<ApiResponse<Void>> concurrency(Exception ex,HttpServletRequest req){return ResponseEntity.status(409).body(new ApiResponse<>("STATE_CONFLICT","并发修改冲突，请刷新后重试",null,(String)req.getAttribute("traceId")));}
 @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class) ResponseEntity<ApiResponse<Void>> size(Exception ex,HttpServletRequest req){return ResponseEntity.status(413).body(new ApiResponse<>("INVALID_REQUEST","材料超过上传限制",null,(String)req.getAttribute("traceId")));}
 @ExceptionHandler(NoResourceFoundException.class) ResponseEntity<ApiResponse<Void>> missing(Exception ex,HttpServletRequest req) { return ResponseEntity.status(404).body(new ApiResponse<>("NOT_FOUND","资源不存在",null,(String)req.getAttribute("traceId"))); }
 @ExceptionHandler(Exception.class) ResponseEntity<ApiResponse<Void>> failure(Exception ex,HttpServletRequest req) { LOG.error("Unhandled error type={}, traceId={}",ex.getClass().getName(),req.getAttribute("traceId"));var a=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();var p=a!=null&&a.getPrincipal() instanceof com.example.datagov.security.PrincipalContext x?x:null;try{events.record((String)req.getAttribute("traceId"),"INTERNAL_ERROR",p);}catch(Exception ignored){LOG.error("Security event unavailable, traceId={}",req.getAttribute("traceId"));}return ResponseEntity.internalServerError().body(new ApiResponse<>("INTERNAL_ERROR","系统异常，请提供追踪号",null,(String)req.getAttribute("traceId"))); }
}
