package com.eventloop.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight, zero-dependency JSON serializer and parser for EventLoop Web Server.
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) return "null";

        if (obj instanceof String) {
            return "\"" + escape((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Collection<?>) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Object[]) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Object[]) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map<?, ?>) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(entry.getKey().toString())).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }

        // Generic object serialization via reflection
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object val = field.get(obj);
                    if (!first) sb.append(",");
                    sb.append("\"").append(field.getName()).append("\":");
                    sb.append(toJson(val));
                    first = false;
                } catch (Exception e) {
                    // Ignore
                }
            }
            clazz = clazz.getSuperclass();
        }
        sb.append("}");
        return sb.toString();
    }

    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        Pattern pattern = Pattern.compile("(?:\"([^\"]+)\"|([a-zA-Z0-9_]+))\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\]]+))");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            String val = matcher.group(3) != null ? matcher.group(3) : (matcher.group(4) != null ? matcher.group(4).trim() : "");
            map.put(key, val);
        }
        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
