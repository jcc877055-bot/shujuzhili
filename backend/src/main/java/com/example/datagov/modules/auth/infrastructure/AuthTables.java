package com.example.datagov.modules.auth.infrastructure;
/** SQL names and D17 IDs; never accept table names from client input. */
public final class AuthTables {
 private AuthTables() {}
 public static final String ORG = "org"; // D17-T01
 public static final String USER_ACCOUNT = "user_account"; // D17-T02
 public static final String ROLE = "role"; // D17-T03
 public static final String PERMISSION = "permission"; // D17-T04
 public static final String USER_ROLE = "user_role"; // D17-T05
 public static final String ROLE_PERMISSION = "role_permission"; // D17-T06
 public static final String USER_SCOPE = "user_scope"; // D17-T07
 public static final String AUTH_SESSION = "auth_session"; // D17-T08
}
