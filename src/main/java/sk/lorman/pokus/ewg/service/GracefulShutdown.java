package sk.lorman.pokus.ewg.service;

import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class GracefulShutdown {

    void onStop(@Observes ShutdownEvent ev) {
        // Place for any custom cleanup logic if needed in the future
        log.info("Application is shutting down. Starting graceful cleanup.");
        // If you manage external resources manually, release them here.
    }
}
