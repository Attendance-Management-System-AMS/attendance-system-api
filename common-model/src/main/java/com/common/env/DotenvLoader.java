package com.common.env;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Loads a {@code .env} file into JVM system properties before Spring starts.
 * OS environment variables and existing system properties are not overwritten.
 */
public final class DotenvLoader {

    private DotenvLoader() {
    }

    public static void load() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int depth = 0; depth < 8 && dir != null; depth++) {
            Path envFile = dir.resolve(".env");
            if (Files.isRegularFile(envFile)) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(dir.toString())
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, Objects.requireNonNullElse(entry.getValue(), ""));
                    }
                });
                return;
            }
            dir = dir.getParent();
        }
    }
}
