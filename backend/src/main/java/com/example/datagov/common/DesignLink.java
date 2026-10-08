package com.example.datagov.common;
import java.lang.annotation.*;
/** Stable D09 / D18 / D22 links, inspected by contract tests. */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface DesignLink { String[] requirements(); String api(); String[] tests(); }
