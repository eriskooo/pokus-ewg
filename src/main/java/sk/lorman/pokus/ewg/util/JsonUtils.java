package sk.lorman.pokus.ewg.util;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Jednoduchý JSON util pre serializáciu objektov pomocou Jackson ObjectMapperu.
 * <p>
 * Použijeme lokálnu inštanciu ObjectMapperu (transitívne dostupnú cez Kafka serializer),
 * aby sme sa vyhli priamym CDI injekciám v statických kontextoch (napr. toString()).
 */
@Slf4j
public final class JsonUtils {

    private static final Jsonb jsonb = JsonbBuilder.create();

    private JsonUtils() {
        // empty
    }

    public static String toJson(Object object) {
        try {
            return jsonb.toJson(object);
        } catch (Exception e) {
            log.error("Can not convert Object to JSON String.", e);
        }
        return "";
    }

    public static <T> T fromJson(InputStream inputStream, Class<T> c) {
        return jsonb.fromJson(inputStream, c);
    }

    public static <T> T fromJson(String json, Class<T> c) {
        return jsonb.fromJson(json, c);
    }

}