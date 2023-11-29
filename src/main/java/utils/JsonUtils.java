package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonUtils {
    private static ObjectMapper INSTANCE = null;

    public static ObjectMapper getObjectMapperInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            INSTANCE.registerModule(module);
        }
        return INSTANCE;
    }
}
