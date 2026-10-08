package com.example.datagov.security;
import com.example.datagov.modules.auth.application.AuthService;
import com.example.datagov.modules.audit.application.SecurityEvents;
import com.example.datagov.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
public class TokenFilter extends OncePerRequestFilter {
 private final AuthService auth;private final ObjectMapper json;private final SecurityEvents events;
 public TokenFilter(AuthService auth,ObjectMapper json,SecurityEvents events) { this.auth=auth;this.json=json;this.events=events; }
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  if(req.getRequestURI().equals("/api/v1/auth/login")||req.getRequestURI().startsWith("/actuator/health")) { chain.doFilter(req,res);return; }
  String header=req.getHeader("Authorization");PrincipalContext p;
  try {p=header!=null&&header.startsWith("Bearer ")?auth.authenticate(header.substring(7)):null;}catch(org.springframework.dao.DataAccessException ex){res.setStatus(503);res.setContentType("application/json;charset=UTF-8");json.writeValue(res.getOutputStream(),new ApiResponse<>("INTERNAL_ERROR","身份服务暂不可用",null,(String)req.getAttribute("traceId")));return;}
  if(p!=null) { var authentication=new UsernamePasswordAuthenticationToken(p,null,p.permissions().stream().map(SimpleGrantedAuthority::new).toList());SecurityContextHolder.getContext().setAuthentication(authentication);chain.doFilter(req,res);return; }
  try{events.record((String)req.getAttribute("traceId"),"UNAUTHENTICATED",null);}catch(org.springframework.dao.DataAccessException ignored){/* Database outage cannot bypass authentication. */}res.setStatus(401);res.setContentType("application/json;charset=UTF-8");json.writeValue(res.getOutputStream(),new ApiResponse<>("UNAUTHENTICATED","需要有效登录会话",null,(String)req.getAttribute("traceId")));
 }
}
