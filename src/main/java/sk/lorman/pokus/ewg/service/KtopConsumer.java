package sk.lorman.pokus.ewg.service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import sk.lorman.pokus.ewg.dto.StartupMessageDto;

@ApplicationScoped
@Slf4j
public class KtopConsumer {

    @Incoming("ktop-in")
    public void consume(StartupMessageDto message) {
        // Tu spracuješ prijatú JSON správu mapovanú na DTO
        log.info("Received JSON message from ktop: timestamp={}, appRandom={}, message={}",
                message.getTimestamp(), message.getAppRandom(), message.getMessage());

        // Tvoje business spracovanie...
    }
}
