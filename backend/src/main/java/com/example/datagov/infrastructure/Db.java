package com.example.datagov.infrastructure;
import com.example.datagov.common.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Component;
import java.sql.*;
import java.time.*;
import java.util.*;
@Component public class Db {
 public final JdbcTemplate jdbc; public Db(JdbcTemplate jdbc) { this.jdbc=jdbc; }
 public List<Map<String,Object>> list(String sql,Object... args) { return jdbc.queryForList(sql,args); }
 public Map<String,Object> one(String sql,Object... args) { var rows=list(sql,args);Checks.require(rows.size()==1,404,ErrorCode.NOT_FOUND,"资源不可见或不存在");return rows.get(0); }
 public long count(String sql,Object... args) { return jdbc.queryForObject(sql,Long.class,args); }
 public int update(String sql,Object... args) { return jdbc.update(sql,args); }
 public String insert(String sql,Object... args) { KeyHolder keys=new GeneratedKeyHolder();jdbc.update(c->{PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS); for(int i=0;i<args.length;i++)p.setObject(i+1,args[i] instanceof Instant t?Timestamp.from(t):args[i]);return p;},keys);return Objects.requireNonNull(keys.getKey()).toString(); }
 public static String str(Map<String,Object> row,String key) { var v=row.get(key);return v==null?null:v.toString(); }
 public static long num(Map<String,Object> row,String key) { return ((Number)row.get(key)).longValue(); }
 public static Map<String,Object> dto(Map<String,Object> row) { Map<String,Object> out=new LinkedHashMap<>(); row.forEach((key,value)->{String[] parts=key.split("_");StringBuilder camel=new StringBuilder(parts[0]);for(int i=1;i<parts.length;i++)camel.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));if(value!=null&&(key.equals("id")||key.endsWith("_id")||key.endsWith("_by")))value=value.toString();if(value instanceof Timestamp stamp)value=stamp.toInstant().toString();if(value instanceof LocalDateTime time)value=time.toInstant(ZoneOffset.UTC).toString();out.put(camel.toString(),value);});return out; }
 public static Map<String,Object> page(List<Map<String,Object>> rows,int page,int size,long total) { return Map.of("items",rows.stream().map(Db::dto).toList(),"page",page,"size",size,"total",total); }
 public static void paging(int page,int size) { Checks.require(page>=1&&size>=1&&size<=100&&page<=100000,400,ErrorCode.INVALID_REQUEST,"分页参数无效"); }
}
