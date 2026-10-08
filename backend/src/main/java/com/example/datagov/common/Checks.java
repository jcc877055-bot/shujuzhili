package com.example.datagov.common;
import java.math.BigInteger;
public final class Checks {
 private Checks() {}
 public static void require(boolean ok,int status,ErrorCode code,String message) { if(!ok) throw new BusinessException(code,status,message); }
 public static String id(String id) { require(id!=null&&id.matches("[1-9][0-9]{0,19}")&&new BigInteger(id).compareTo(new BigInteger("18446744073709551615"))<=0,400,ErrorCode.INVALID_REQUEST,"资源ID格式错误"); return id; }
 public static void version(long actual,long expected) { require(actual==expected,409,ErrorCode.VERSION_CONFLICT,"对象已改变，请刷新后重试"); }
 public static String text(String value,int max) { require(value!=null&&!value.isBlank()&&value.length()<=max,400,ErrorCode.INVALID_REQUEST,"必填文本缺失或超过长度"); return value.strip(); }
 public static String sha256(byte[] value) { try { return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(value)); } catch(java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); } }
}
