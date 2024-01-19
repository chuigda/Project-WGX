package tech.icey.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tech.icey.util.RuntimeError.runtimeError;

public record IniParser() {
    public static Pair<HashMap<String, HashMap<String, String>>, List<String>> parse(String ini) {
        HashMap<String, HashMap<String, String>> result = new HashMap<>();
        List<String> parsingErrors = new ArrayList<>();

        String currentSectionName = "default";
        HashMap<String, String> currentSection = new HashMap<>();

        String[] lines = ini.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("#") ||
                    line.startsWith(";") ||
                    line.startsWith("--") ||
                    line.isEmpty() ||
                    line.isBlank()) {
                continue;
            }

            if (line.startsWith("[")) {
                if (line.endsWith("]")) {
                    result.put(currentSectionName, currentSection);
                    currentSectionName = line.substring(1, line.length() - 1);
                    currentSection = new HashMap<>();
                } else {
                    parsingErrors.add(String.format("第 %d 行：节名格式错误", i + 1));
                }
            } else {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    currentSection.put(parts[0].trim(), parts[1].trim());
                } else {
                    parsingErrors.add(String.format("第 %d 行：键值对格式错误", i + 1));
                }
            }
        }

        result.put(currentSectionName, currentSection);
        return new Pair<>(result, parsingErrors);
    }

    public static <T> Pair<T, List<String>> deserialise(Class<T> clazz, HashMap<String, HashMap<String, String>> ini) {
        T instance;
        List<String> errors = new ArrayList<>();

        try {
            Constructor<T> ctor = clazz.getConstructor();
            instance = ctor.newInstance();
        } catch (Exception e) {
            return runtimeError(
                    "无法构造类型 %s 的实例: %s: %s",
                    clazz.getName(),
                    e.getClass().getName(),
                    e.getMessage()
            );
        }

        for (Field field : clazz.getDeclaredFields()) {
            IniField iniField = field.getAnnotation(IniField.class);
            if (iniField == null) {
                continue;
            }

            HashMap<String, String> section = ini.get(iniField.section());
            if (section == null) {
                continue;
            }

            String key = iniField.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            String value = section.get(key);
            if (value == null) {
                continue;
            }

            try {
                field.setAccessible(true);
                field.set(instance, parseFieldValue(field.getGenericType(), value));
            } catch (NumberFormatException e) {
                errors.add(String.format(
                        "无法将节 %s 中 %s 的值解析为类型 %s: %s",
                        iniField.section(),
                        key,
                        field.getType().getName(),
                        e.getMessage()
                ));
            } catch (Exception e) {
                e.printStackTrace();
                runtimeError(
                        "发生了以下异常，无法解析并设置类型 %s 的字段 %s: %s: %s",
                        clazz.getName(),
                        field.getName(),
                        e.getClass().getName(),
                        e.getMessage()
                );
            }
        }

        return new Pair<>(instance, errors);
    }
    
    public static Object parseFieldValue(Type ty, String value) {
        if (ty instanceof ParameterizedType pTy && pTy.getRawType() == Optional.class) {
            Type innerTy = pTy.getActualTypeArguments()[0];
            return Optional.fromNullable(parseFieldValue(innerTy, value));
        } else if (ty == String.class) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t");
            }
            return value;
        } else if (ty == int.class || ty == Integer.class) {
            return Integer.parseInt(value);
        } else if (ty == long.class || ty == Long.class) {
            return Long.parseLong(value);
        } else if (ty == float.class || ty == Float.class) {
            return Float.parseFloat(value);
        } else if (ty == double.class || ty == Double.class) {
            return Double.parseDouble(value);
        } else if (ty == boolean.class || ty == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            return runtimeError("不支持的类型: %s", ty.getTypeName());
        }
    }
}
