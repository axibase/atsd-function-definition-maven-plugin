package com.axibase;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TestGenerator {
    public static void generate(Path output, Class<?> clazz) throws IOException {
        if (Files.notExists(output.getParent())) {
            Files.createDirectories(output.getParent());
        }
        final String definitions = Arrays.stream(clazz.getDeclaredMethods())
                .map(m -> '"' + m.getName() + '"')
                .collect(Collectors.joining(", ", "[", "]"));
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.append(definitions);
            writer.flush();
        }
    }
}
