package sk.lorman.pokus.ewg.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import sk.lorman.pokus.ewg.domain.Kamion;
import sk.lorman.pokus.ewg.repository.KamionRepository;

import java.util.List;


@ApplicationScoped
@Slf4j
public class KamionService {

    @Inject
    KamionRepository repository;

    @WithSpan
    public List<Kamion> listAll() {
        log.debug("Listing all Kamion entities");
        return repository.listAll();
    }

    public Kamion getById(Long id) {
        log.debug("Get Kamion by id={}", id);
        return repository.findById(id);
    }

    @Transactional
    public Kamion create(Kamion input) {
        if (input == null || input.spz == null || input.znacka == null || input.nosnostKg == null) {
            throw new IllegalArgumentException("Neplatné dáta");
        }
        log.info("Creating Kamion spz={}, znacka={}", input.spz, input.znacka);
        input.id = null; // ensure new
        repository.persist(input);
        return input;
    }

    @Transactional
    public Kamion update(Long id, Kamion input) {
        log.info("Updating Kamion id={}", id);
        Kamion existing = repository.findById(id);
        if (existing == null) {
            log.info("Kamion id={} not found", id);
            return null;
        }
        if (input != null) {
            if (input.spz != null) existing.spz = input.spz;
            if (input.znacka != null) existing.znacka = input.znacka;
            if (input.nosnostKg != null) existing.nosnostKg = input.nosnostKg;
        }
        return existing;
    }

    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting Kamion id={}", id);
        Kamion existing = repository.findById(id);
        if (existing == null) {
            log.info("Kamion id={} not found for delete", id);
            return false;
        }
        repository.delete(existing);
        return true;
    }
}
