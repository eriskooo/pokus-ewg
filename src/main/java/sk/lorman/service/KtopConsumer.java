package sk.lorman.service;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
@Slf4j
public class KtopConsumer {

    @Incoming("ktop-in")
    public void consume(String message) {
        // Tu spracuješ prijatú správu
        log.info("Received message from ktop: {}", message);

        // Tvoje business spracovanie...
    }
}
