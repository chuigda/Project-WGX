package tech.icey.util;

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
        List<String> parseErrors = new ArrayList<>();

        try {
            var ctor = clazz.getConstructor();
            instance = ctor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();

            return runtimeError(
                    "无法构造类型 %s 的实例: %s: %s",
                    clazz.getName(),
                    e.getClass().getName(),
                    e.getMessage()
            );
        }

        for (var field : clazz.getDeclaredFields()) {
            var iniField = field.getAnnotation(IniField.class);
            if (iniField == null) {
                continue;
            }

            var section = ini.get(iniField.section());
            if (section == null) {
                continue;
            }

            var key = iniField.key();
            if (key.isEmpty()) {
                key = field.getName();
            }

            var value = section.get(key);
            if (value == null) {
                continue;
            }

            try {
                field.setAccessible(true);
                if (field.getType() == String.class) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1)
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                                .replace("\\n", "\n")
                                .replace("\\r", "\r")
                                .replace("\\t", "\t");
                    }
                    field.set(instance, value);
                } else if (field.getType() == int.class) {
                    field.set(instance, Integer.parseInt(value));
                } else if (field.getType() == long.class) {
                    field.set(instance, Long.parseLong(value));
                } else if (field.getType() == float.class) {
                    field.set(instance, Float.parseFloat(value));
                } else if (field.getType() == double.class) {
                    field.set(instance, Double.parseDouble(value));
                } else if (field.getType() == boolean.class) {
                    field.set(instance, Boolean.parseBoolean(value));
                } else {
                    runtimeError(
                            "类型 %s 的字段 %s 具有不支持的类型: %s",
                            clazz.getName(),
                            field.getName(),
                            field.getType().getName()
                    );
                }
            } catch (NumberFormatException e) {
                parseErrors.add(String.format(
                        "无法将节 %s 中 %s 的值解析为类型 %s: %s",
                        iniField.section(),
                        key,
                        field.getType().getName(),
                        e.getMessage()
                ));
            } catch (Exception e) {
                runtimeError(
                        "发生了以下异常，无法解析并设置类型 %s 的字段 %s: %s: %s",
                        clazz.getName(),
                        field.getName(),
                        e.getClass().getName(),
                        e.getMessage()
                );
            }
        }

        return new Pair<>(instance, parseErrors);
    }
}
