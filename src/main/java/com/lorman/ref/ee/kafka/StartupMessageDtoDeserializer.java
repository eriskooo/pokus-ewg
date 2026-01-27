package com.lorman.ref.ee.kafka;

import com.lorman.ref.ee.dto.StartupMessageDto;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

/**
 * Deserializer pre {@link StartupMessageDto} pre SmallRye Kafka.
 * Použije Jackson ObjectMapper dodaný Quarkusom.
 */
public class StartupMessageDtoDeserializer extends ObjectMapperDeserializer<StartupMessageDto> {
    public StartupMessageDtoDeserializer() {
        super(StartupMessageDto.class);
    }
}
