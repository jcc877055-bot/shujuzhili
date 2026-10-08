package com.example.datagov.common;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {
 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
  String id=UUID.randomUUID().toString(); request.setAttribute("traceId",id); response.setHeader("X-Trace-Id",id); MDC.put("traceId",id);
  try { chain.doFilter(request,response); } finally { MDC.remove("traceId"); }
 }
}
