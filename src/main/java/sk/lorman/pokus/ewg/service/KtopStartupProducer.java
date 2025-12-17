package sk.lorman.pokus.ewg.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

import java.time.OffsetDateTime;
import sk.lorman.pokus.ewg.dto.StartupMessageDto;

@ApplicationScoped
@Slf4j
public class KtopStartupProducer {

    @Channel("ktop-out")
    Emitter<StartupMessageDto> emitter;

    @ConfigProperty(name = "app.random")
    String appRandom;

    void onStart(@Observes StartupEvent ev) {
        String now = OffsetDateTime.now().toString();
        String message = "Application started at " + now;
        StartupMessageDto dto = new StartupMessageDto(now, message, appRandom);
        // Log the random property value at INFO level on startup
        log.info("Random property app.random={} ", appRandom);
        log.info("Odosielam JSON správu o štarte aplikácie do Kafka topicu 'my.first.topic': {}", dto);
        try {
            emitter.send(dto);
        } catch (Exception e) {
            log.error("Zlyhalo odoslanie správy o štarte aplikácie do Kafka: {}", e.getMessage(), e);
        }
    }
}
