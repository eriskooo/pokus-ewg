package sk.lorman.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

import java.time.OffsetDateTime;

@ApplicationScoped
@Slf4j
public class KtopStartupProducer {

    @Channel("ktop-out")
    Emitter<String> emitter;

    @ConfigProperty(name = "app.random")
    String appRandom;

    void onStart(@Observes StartupEvent ev) {
        String now = OffsetDateTime.now().toString();
        String message = "Application started at " + now;
        // Log the random property value at INFO level on startup
        log.info("Random property app.random={} ", appRandom);
        log.info("Odosielam správu o štarte aplikácie do Kafka topicu 'my.first.topic': {}", message);
        try {
            emitter.send(message);
        } catch (Exception e) {
            log.error("Zlyhalo odoslanie správy o štarte aplikácie do Kafka: {}", e.getMessage(), e);
        }
    }
}
