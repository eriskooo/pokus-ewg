package sk.lorman.pokus.ewg.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import sk.lorman.pokus.ewg.dto.StartupMessageDto;

/**
 * Deserializer pre {@link StartupMessageDto} pre SmallRye Kafka.
 * Použije Jackson ObjectMapper dodaný Quarkusom.
 */
public class StartupMessageDtoDeserializer extends ObjectMapperDeserializer<StartupMessageDto> {
    public StartupMessageDtoDeserializer() {
        super(StartupMessageDto.class);
    }
}
