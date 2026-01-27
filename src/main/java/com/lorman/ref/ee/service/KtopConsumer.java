package com.lorman.ref.ee.service;

import com.lorman.ref.ee.dto.StartupMessageDto;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

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
