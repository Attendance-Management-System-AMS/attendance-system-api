package com.attendance.common.env;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DevelopmentEnvLoader {

    private static final String DEFAULT_ENV_FILE = ".env.development";

    private DevelopmentEnvLoader() {
    }

    public static void load() {
        load(DEFAULT_ENV_FILE);
    }

    public static void load(String fileName) {
        findEnvFile(fileName).ifPresent(DevelopmentEnvLoader::loadFile);
    }

    private static java.util.Optional<Path> findEnvFile(String fileName) {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();

        while (current != null) {
            Path candidate = current.resolve(fileName);
            if (Files.isRegularFile(candidate)) {
                return java.util.Optional.of(candidate);
            }
            current = current.getParent();
        }

        return java.util.Optional.empty();
    }

    private static void loadFile(Path envFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return;
        }

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int separatorIndex = line.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }

            String key = line.substring(0, separatorIndex).trim();
            String value = stripWrappingQuotes(line.substring(separatorIndex + 1).trim());

            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
            }

            String relaxedKey = toSpringPropertyName(key);
            if (!relaxedKey.equals(key) && System.getProperty(relaxedKey) == null) {
                System.setProperty(relaxedKey, value);
            }
        }
    }

    private static String stripWrappingQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static String toSpringPropertyName(String key) {
        return key.toLowerCase().replace('_', '.');
    }
}
