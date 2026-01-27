package com.lorman.ref.ee.repository;

import com.lorman.ref.ee.domain.Kamion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class KamionRepository implements PanacheRepository<Kamion> {
    // Extra dotazy/operácie podľa potreby (napr. findBySpz) môžu byť doplnené neskôr.
}
