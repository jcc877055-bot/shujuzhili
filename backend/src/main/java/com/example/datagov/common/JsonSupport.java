package com.example.datagov.common;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.stereotype.Component;
import java.util.*;
@Component public class JsonSupport {
 private final ObjectMapper mapper; public JsonSupport(ObjectMapper mapper) { this.mapper=mapper; }
 public String json(Object value) { try { return mapper.writeValueAsString(value); } catch(Exception ex) { throw new IllegalArgumentException("Invalid JSON value",ex); } }
 public Object parse(String value) { try { return mapper.readerFor(Object.class).with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,DeserializationFeature.USE_BIG_INTEGER_FOR_INTS).readValue(value); } catch(Exception ex) { throw new IllegalStateException("Invalid stored JSON",ex); } }
 public String canonical(Object value) { return json(sorted(mapper.valueToTree(value))); }
 private JsonNode sorted(JsonNode node) { if(node.isObject()) { ObjectNode result=mapper.createObjectNode();TreeSet<String> keys=new TreeSet<>();node.fieldNames().forEachRemaining(keys::add);keys.forEach(k->result.set(k,sorted(node.get(k))));return result; } if(node.isArray()) { ArrayNode result=mapper.createArrayNode();node.forEach(n->result.add(sorted(n)));return result; } return node; }
}
