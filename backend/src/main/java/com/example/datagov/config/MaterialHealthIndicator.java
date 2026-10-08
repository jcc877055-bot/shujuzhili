package com.example.datagov.config;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;
@Component("materials") public class MaterialHealthIndicator implements HealthIndicator {
 private final Path directory; public MaterialHealthIndicator(@Value("${app.materials-dir}") String directory) { this.directory=Path.of(directory); }
 public Health health() { return Files.isDirectory(directory)&&Files.isReadable(directory)&&Files.isWritable(directory) ? Health.up().build() : Health.down().build(); }
}
