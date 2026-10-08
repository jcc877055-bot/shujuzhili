package com.example.datagov.config;
import com.example.datagov.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import com.example.datagov.modules.auth.application.AuthService;
import com.example.datagov.modules.audit.application.SecurityEvents;
import com.example.datagov.security.TokenFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwords() { return new BCryptPasswordEncoder(10); }
 @Bean AuthenticationManager authenticationManager() { return new ProviderManager(new AuthenticationProvider() {
  public Authentication authenticate(Authentication a) throws AuthenticationException { throw new BadCredentialsException("Authentication is not implemented"); }
  public boolean supports(Class<?> c) { return true; }
 }); }
 @Bean SecurityFilterChain security(HttpSecurity http,ObjectMapper mapper,AuthService auth,SecurityEvents events) throws Exception {
  return http.csrf(c->c.disable()).formLogin(c->c.disable()).httpBasic(c->c.disable()).logout(c->c.disable())
   .sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(c->c.requestMatchers("/actuator/health","/actuator/health/liveness","/actuator/health/readiness","/api/v1/auth/login").permitAll().anyRequest().authenticated())
   .addFilterBefore(new TokenFilter(auth,mapper,events),UsernamePasswordAuthenticationFilter.class)
   .exceptionHandling(c->c.authenticationEntryPoint((req,res,ex)->{ res.setStatus(401);res.setContentType("application/json;charset=UTF-8");mapper.writeValue(res.getOutputStream(),new ApiResponse<>("UNAUTHENTICATED","需要有效登录会话",null,(String)req.getAttribute("traceId"))); })
    .accessDeniedHandler((req,res,ex)->{res.setStatus(403);res.setContentType("application/json;charset=UTF-8");mapper.writeValue(res.getOutputStream(),new ApiResponse<>("FORBIDDEN","无操作权限",null,(String)req.getAttribute("traceId")));})).build();
 }
}
