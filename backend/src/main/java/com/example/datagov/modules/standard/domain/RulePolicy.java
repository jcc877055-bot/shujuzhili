package com.example.datagov.modules.standard.domain;

import com.example.datagov.common.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static com.example.datagov.common.Checks.*;

/** D15-M03/M04: declarative rules; no SQL, scripts or user-supplied regex. */
public final class RulePolicy {
 private RulePolicy() {}
 public static final Set<String> TYPES=Set.of("COMPLETENESS","UNIQUENESS","VALIDITY","CONSISTENCY");
 private static final Set<String> VALID=Set.of("allowNull","trim","ignoreCase","min","max","minLength","maxLength","enumValues","format");
 public static Map<String,Object> parameters(String type,Map<String,Object> input){
  require(type!=null&&TYPES.contains(type)&&input!=null,400,ErrorCode.INVALID_REQUEST,"规则类型或参数无效");
  Set<String> allowed=switch(type){case "COMPLETENESS"->Set.of("trim");case "UNIQUENESS"->Set.of("allowNull","trim","ignoreCase");case "VALIDITY"->VALID;default->Set.of("otherFieldId","operator","allowNull","trim","ignoreCase");};
  require(allowed.containsAll(input.keySet()),400,ErrorCode.INVALID_REQUEST,"规则包含未支持参数");
  Map<String,Object> p=new LinkedHashMap<>(input);
  for(String key:List.of("trim","ignoreCase","allowNull"))if(p.containsKey(key))require(p.get(key) instanceof Boolean,400,ErrorCode.INVALID_REQUEST,"布尔参数格式无效");
  p.putIfAbsent("trim",true);if(!type.equals("COMPLETENESS")){p.putIfAbsent("allowNull",false);p.putIfAbsent("ignoreCase",false);}
  if(type.equals("VALIDITY")){
   require(input.keySet().stream().anyMatch(Set.of("min","max","minLength","maxLength","enumValues","format")::contains),400,ErrorCode.INVALID_REQUEST,"有效性至少指定一种约束");
   for(String key:List.of("min","max"))if(p.containsKey(key))p.put(key,decimal(p.get(key)).toPlainString());
   if(p.containsKey("min")&&p.containsKey("max"))require(decimal(p.get("min")).compareTo(decimal(p.get("max")))<=0,400,ErrorCode.INVALID_REQUEST,"范围下限不能大于上限");
   for(String key:List.of("minLength","maxLength"))if(p.containsKey(key)){BigDecimal n=decimal(p.get(key));require(n.scale()<=0&&n.signum()>=0&&n.compareTo(BigDecimal.valueOf(1024))<=0,400,ErrorCode.INVALID_REQUEST,"长度需为0至1024整数");p.put(key,n.intValueExact());}
   if(p.containsKey("minLength")&&p.containsKey("maxLength"))require(((Number)p.get("minLength")).intValue()<=((Number)p.get("maxLength")).intValue(),400,ErrorCode.INVALID_REQUEST,"长度范围无效");
   if(p.containsKey("format"))require(p.get("format") instanceof String&&Set.of("EMAIL","PHONE_CN","ISO_DATE","INTEGER","DECIMAL").contains(p.get("format")),400,ErrorCode.INVALID_REQUEST,"格式仅支持预置白名单");
   if(p.containsKey("enumValues")){require(p.get("enumValues") instanceof List<?>,400,ErrorCode.INVALID_REQUEST,"枚举需为文本数组");List<?> vs=(List<?>)p.get("enumValues");require(!vs.isEmpty()&&vs.size()<=100,400,ErrorCode.INVALID_REQUEST,"枚举数量需为1至100");for(Object v:vs)require(v instanceof String s&&!s.isEmpty()&&s.length()<=256,400,ErrorCode.INVALID_REQUEST,"枚举项需为1至256字符文本");require(vs.stream().map(v->normalized(v,p)).distinct().count()==vs.size(),400,ErrorCode.INVALID_REQUEST,"归一化后枚举项重复");}
  }
  if(type.equals("CONSISTENCY")){require(p.get("otherFieldId") instanceof String,400,ErrorCode.INVALID_REQUEST,"需指定对照字段ID");id((String)p.get("otherFieldId"));require(p.get("operator") instanceof String&&Set.of("EQUALS","NOT_EQUALS").contains(p.get("operator")),400,ErrorCode.INVALID_REQUEST,"一致性操作符无效");}
  return p;
 }
 public static Map<String,Object> definition(Map<String,Object> input){require(input!=null&&Set.of("description","constraints").containsAll(input.keySet())&&input.get("description") instanceof String,400,ErrorCode.INVALID_REQUEST,"标准定义需有description及constraints");return Map.of("description",text((String)input.get("description"),2000),"constraints",parameters("VALIDITY",object(input.get("constraints"))));}
 @SuppressWarnings("unchecked") public static Map<String,Object> object(Object value){require(value instanceof Map<?,?>,400,ErrorCode.INVALID_REQUEST,"参数需为JSON对象");return (Map<String,Object>)value;}
 private static BigDecimal decimal(Object value){require((value instanceof Number||value instanceof String)&&value.toString().length()<=128,400,ErrorCode.INVALID_REQUEST,"数值约束格式无效");try{BigDecimal n=new BigDecimal(value.toString());require(n.precision()<=40&&Math.abs((long)n.scale())<=20,400,ErrorCode.INVALID_REQUEST,"数值精度超限");return n.stripTrailingZeros();}catch(NumberFormatException ex){throw new BusinessException(ErrorCode.INVALID_REQUEST,400,"数值约束格式无效");}}
 public static Object normalized(Object value,Map<String,Object> p){if(value instanceof String s){if(Boolean.TRUE.equals(p.get("trim")))s=s.strip();if(Boolean.TRUE.equals(p.get("ignoreCase")))s=s.toLowerCase(Locale.ROOT);return s;}if(value instanceof Number)return new BigDecimal(value.toString()).stripTrailingZeros();return value;}
 public static boolean missing(Object value,Map<String,Object> p){return value==null||normalized(value,p) instanceof String s&&s.isEmpty();}
 /** null=PASS; otherwise stable reason code, without disclosing the offending value. */
 public static String violation(String type,Object value,Object other,Map<String,Object> p,Set<Object> duplicates){
  if(missing(value,p))return type.equals("COMPLETENESS")||!Boolean.TRUE.equals(p.get("allowNull"))?"MISSING_VALUE":null;
  Object v=normalized(value,p);
  if(type.equals("COMPLETENESS"))return null;
  if(type.equals("UNIQUENESS"))return duplicates.contains(v)?"DUPLICATE_VALUE":null;
  if(type.equals("CONSISTENCY")){if(missing(other,p))return "MISSING_COMPARISON";return Objects.equals(v,normalized(other,p))=="EQUALS".equals(p.get("operator"))?null:"INCONSISTENT_VALUE";}
  String s=v.toString();int length=s.codePointCount(0,s.length());
  if(p.containsKey("minLength")&&length<((Number)p.get("minLength")).intValue())return "TOO_SHORT";
  if(p.containsKey("maxLength")&&length>((Number)p.get("maxLength")).intValue())return "TOO_LONG";
  if(p.containsKey("enumValues")&&((List<?>)p.get("enumValues")).stream().noneMatch(x->Objects.equals(normalized(x,p).toString(),s)))return "OUTSIDE_ENUM";
  if(p.containsKey("min")||p.containsKey("max")){if(!s.matches("[+-]?[0-9]+(?:\\.[0-9]+)?"))return "NOT_NUMERIC";BigDecimal n=new BigDecimal(s);if(p.containsKey("min")&&n.compareTo(new BigDecimal(p.get("min").toString()))<0||p.containsKey("max")&&n.compareTo(new BigDecimal(p.get("max").toString()))>0)return "OUTSIDE_RANGE";}
  if(p.containsKey("format")){boolean valid=switch((String)p.get("format")){case "EMAIL"->s.matches("[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]{1,190}\\.[A-Za-z]{2,20}");case "PHONE_CN"->s.matches("1[3-9][0-9]{9}");case "INTEGER"->s.matches("[+-]?[0-9]+");case "DECIMAL"->s.matches("[+-]?[0-9]+(?:\\.[0-9]+)?");case "ISO_DATE"->validDate(s);default->false;};if(!valid)return "INVALID_FORMAT";}
  return null;
 }
 private static boolean validDate(String s){if(!s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))return false;try{LocalDate.parse(s);return true;}catch(java.time.format.DateTimeParseException ex){return false;}}
}
