package ua.edu.lnu.fitnessql.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    private FileUtils() {
        throw new UnsupportedOperationException("This class is not supposed to be instantiated");
    }

    public static String readFileContent(String path) {
        try (InputStream resourceInputStream = FileUtils.class.getResourceAsStream(path)) {
            if (resourceInputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            } else {
                return new String(resourceInputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while reading file content", e);
        }
    }
}
