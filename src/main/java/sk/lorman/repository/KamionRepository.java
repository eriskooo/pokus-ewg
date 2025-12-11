package sk.lorman.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import sk.lorman.domain.Kamion;

@ApplicationScoped
public class KamionRepository implements PanacheRepository<Kamion> {
    // Extra dotazy/operácie podľa potreby (napr. findBySpz) môžu byť doplnené neskôr.
}
