package sk.lorman.pokus.ewg.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.lorman.pokus.ewg.domain.Kamion;
import sk.lorman.pokus.ewg.repository.KamionRepository;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class KamionServiceTest {

    @Inject
    KamionService service;

    @Inject
    KamionRepository repository;

    @BeforeEach
    @Transactional
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    void create_success() {
        Kamion k = new Kamion();
        k.spz = "TT001AA";
        k.znacka = "MAN";
        k.nosnostKg = 10000;

        Kamion created = service.create(k);
        assertNotNull(created.id);
        Kamion fromDb = repository.findById(created.id);
        assertNotNull(fromDb);
        assertEquals("TT001AA", fromDb.spz);
    }

    @Test
    void create_invalid_throws() {
        Kamion invalid = new Kamion();
        invalid.spz = null;
        invalid.znacka = "X";
        invalid.nosnostKg = 1;
        assertThrows(ConstraintViolationException.class, () -> service.create(invalid));
    }

    @Test
    @Transactional
    void update_existing_returns_updated() {
        Kamion base = new Kamion();
        base.spz = "KE777ZZ";
        base.znacka = "DAF";
        base.nosnostKg = 9000;
        repository.persist(base);

        Kamion patch = new Kamion();
        patch.znacka = "Scania";
        Kamion updated = service.update(base.id, patch);

        assertNotNull(updated);
        assertEquals("Scania", updated.znacka);
        assertEquals("KE777ZZ", updated.spz);
    }

    @Test
    void update_missing_returns_null() {
        Kamion patch = new Kamion();
        patch.znacka = "Iveco";
        Kamion updated = service.update(99999L, patch);
        assertNull(updated);
    }

    @Test
    @Transactional
    void delete_behaviour() {
        Kamion k = new Kamion();
        k.spz = "NR222BB";
        k.znacka = "Volvo";
        k.nosnostKg = 11000;
        repository.persist(k);

        assertTrue(service.delete(k.id));
        assertFalse(service.delete(123456L));
    }
}
