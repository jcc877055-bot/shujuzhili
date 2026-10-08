package com.example.datagov.modules.evidence.application;
import com.example.datagov.common.*;import com.example.datagov.infrastructure.Db;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Component;
import java.nio.file.*;import java.util.*;import static com.example.datagov.common.Checks.*;
/** D17-T23: immutable, generated keys; callers never control filesystem paths. */
@Component public class FileStorage {
 private final Path root;public FileStorage(@Value("${app.materials-dir}") String root){this.root=Path.of(root).toAbsolutePath().normalize();}
 public Path path(String key){require(key!=null&&key.matches("[0-9a-f-]{36}\\.bin"),422,ErrorCode.INVALID_PROOF,"材料索引无效");return root.resolve(key);}
 public void put(String key,byte[] bytes){try{Files.write(path(key),bytes,StandardOpenOption.CREATE_NEW);}catch(java.io.IOException e){throw new BusinessException(ErrorCode.INTERNAL_ERROR,503,"材料存储不可用");}}
 public byte[] verified(Map<String,Object> f){try{var p=path(Db.str(f,"storage_key"));require(!Files.isSymbolicLink(p)&&Files.size(p)==Db.num(f,"size_bytes")&&Files.size(p)<=10485760,422,ErrorCode.INVALID_PROOF,"材料缺失或已变更");var bytes=Files.readAllBytes(p);require(sha256(bytes).equals(Db.str(f,"sha256")),422,ErrorCode.INVALID_PROOF,"材料摘要不一致");return bytes;}catch(java.io.IOException e){throw new BusinessException(ErrorCode.INVALID_PROOF,422,"材料缺失或不可读取");}}
 public void rollback(String key){try{Files.deleteIfExists(path(key));}catch(java.io.IOException ignored){/* Unreferenced file; database transaction never marked it READY. */}}
}
