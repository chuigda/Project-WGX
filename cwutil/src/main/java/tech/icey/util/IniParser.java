package tech.icey.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
