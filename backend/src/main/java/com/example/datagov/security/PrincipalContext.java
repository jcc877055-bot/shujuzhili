package com.example.datagov.security;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.datagov.common.*;
public record PrincipalContext(String id,String orgId,String displayName,long permissionVersion,String tokenHash,Set<String> permissions,Set<String> orgScopes,Set<String> assignedScopes,List<Map<String,Object>> scopes,List<String> roleCodes) {
 public static PrincipalContext current() { var a=SecurityContextHolder.getContext().getAuthentication(); Checks.require(a!=null&&a.getPrincipal() instanceof PrincipalContext,401,ErrorCode.UNAUTHENTICATED,"需要有效登录会话");return (PrincipalContext)a.getPrincipal(); }
 public void permission(String code) { Checks.require(permissions.contains(code),403,ErrorCode.FORBIDDEN,"无此操作权限"); }
}
