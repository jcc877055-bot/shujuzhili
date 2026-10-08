package com.example.datagov.common;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
@RestControllerAdvice public class ResponseAdvice implements ResponseBodyAdvice<Object> {
 public boolean supports(MethodParameter method,Class<? extends HttpMessageConverter<?>> converter) { return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converter); }
 public Object beforeBodyWrite(Object body,MethodParameter method,MediaType type,Class<? extends HttpMessageConverter<?>> converter,ServerHttpRequest req,ServerHttpResponse res) {
  if(body instanceof ApiResponse<?>||!req.getURI().getPath().startsWith("/api/v1"))return body;
  var servlet=((ServletServerHttpRequest)req).getServletRequest();return new ApiResponse<>("OK","操作成功",body,(String)servlet.getAttribute("traceId"));
 }
}
