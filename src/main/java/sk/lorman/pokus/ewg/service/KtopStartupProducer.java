package sk.lorman.pokus.ewg.service;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import sk.lorman.pokus.ewg.dto.StartupMessageDto;

import java.time.OffsetDateTime;

@ApplicationScoped
@Slf4j
public class KtopStartupProducer {

    @Channel("ktop-out")
    Emitter<StartupMessageDto> emitter;

    @ConfigProperty(name = "app.random")
    String appRandom;

    // Number of partitions configured for the outgoing topic (used for auto-creation in dev).
    // In environments where the topic already exists with a different partition count,
    // we will still cap the selected partition to a safe value (see below).
    @ConfigProperty(name = "mp.messaging.outgoing.ktop-out.topic.partitions", defaultValue = "1")
    int configuredPartitions;

    @ConfigProperty(name = "app.partition.routing.enabled", defaultValue = "false")
    boolean partitionRoutingEnabled;

    void onStart(@Observes StartupEvent ev) {
        String now = OffsetDateTime.now().toString();
        String message = "Application started at " + now;
        StartupMessageDto dto = new StartupMessageDto(now, message, appRandom);
        // Log the random property value at INFO level on startup
        log.info("Random property app.random={} ", appRandom);
        long threadId = Thread.currentThread().threadId();
        Integer partitionToUse = null;
        if (partitionRoutingEnabled && configuredPartitions > 1) {
            int safePartitions = Math.max(1, configuredPartitions);
            int partition;
            if (safePartitions == 1) {
                partition = 0;
            } else {
                partition = (int) (threadId % safePartitions);
            }
            partitionToUse = partition;
            log.info("Partition routing ENABLED: configuredPartitions={}, selectedPartition={}, threadId={}", configuredPartitions, partition, threadId);
            log.info("Odosielam JSON správu o štarte aplikácie do Kafka topicu 'my.first.topic' na partition {} (threadId={}): {}",
                    partition, threadId, dto);
        } else {
            if (!partitionRoutingEnabled) {
                log.info("Partition routing DISABLED: sending without explicit partition. threadId={}, configuredPartitions={}", threadId, configuredPartitions);
            } else {
                log.info("Partition routing requested but configuredPartitions={} -> sending without explicit partition.", configuredPartitions);
            }
            log.info("Odosielam JSON správu o štarte aplikácie do Kafka topicu 'my.first.topic' (bez explicitnej partition) (threadId={}): {}",
                    threadId, dto);
        }
        try {
            if (partitionToUse != null) {
                OutgoingKafkaRecordMetadata<Object> metadata = OutgoingKafkaRecordMetadata.builder()
                        .withPartition(partitionToUse)
                        .build();
                Message<StartupMessageDto> msg = Message.of(dto).addMetadata(metadata);
                emitter.send(msg);
            } else {
                emitter.send(dto);
            }
        } catch (Exception e) {
            log.error("Zlyhalo odoslanie správy o štarte aplikácie do Kafka: {}", e.getMessage(), e);
        }
    }
}
