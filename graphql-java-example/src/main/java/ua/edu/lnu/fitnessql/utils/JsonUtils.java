package ua.edu.lnu.fitnessql.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.TimeZone;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = objectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("This class is not supposed to be instantiated");
    }

    public static <T> T loadFromJsonFile(String path, Class<T> clazz) {
        String json = FileUtils.readFileContent(path);
        return deserializeFromJson(json, clazz);
    }

    public static <T> List<T> loadListFromJsonFile(String path, Class<T[]> clazz) {
        return List.of(loadFromJsonFile(path, clazz));
    }

    public static <T> String serializeToJson(T data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while serializing object to JSON", e);
        }
    }

    public static <T> T deserializeFromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while deserializing object from JSON", e);
        }
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
