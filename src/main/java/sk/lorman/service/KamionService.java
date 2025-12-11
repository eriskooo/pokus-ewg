package sk.lorman.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import sk.lorman.domain.Kamion;
import sk.lorman.repository.KamionRepository;

import java.util.List;

@ApplicationScoped
public class KamionService {

    @Inject
    KamionRepository repository;

    public List<Kamion> listAll() {
        return repository.listAll();
    }

    public Kamion getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Kamion create(Kamion input) {
        if (input == null || input.spz == null || input.znacka == null || input.nosnostKg == null) {
            throw new IllegalArgumentException("Neplatné dáta");
        }
        input.id = null; // ensure new
        repository.persist(input);
        return input;
    }

    @Transactional
    public Kamion update(Long id, Kamion input) {
        Kamion existing = repository.findById(id);
        if (existing == null) {
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
        Kamion existing = repository.findById(id);
        if (existing == null) {
            return false;
        }
        repository.delete(existing);
        return true;
    }
}
