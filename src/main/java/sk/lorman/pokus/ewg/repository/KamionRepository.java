package sk.lorman.pokus.ewg.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import sk.lorman.pokus.ewg.domain.Kamion;

@ApplicationScoped
@Slf4j
public class KamionRepository implements PanacheRepository<Kamion> {
    // Extra dotazy/operácie podľa potreby (napr. findBySpz) môžu byť doplnené neskôr.
}
