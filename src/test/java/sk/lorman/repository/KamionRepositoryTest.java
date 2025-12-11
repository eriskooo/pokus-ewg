package sk.lorman.repository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import sk.lorman.domain.Kamion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class KamionRepositoryTest {

    @Inject
    KamionRepository repository;

    @Test
    @Transactional
    void persist_find_list_delete_flow() {
        // create and persist
        Kamion k = new Kamion();
        k.spz = "BA123AB";
        k.znacka = "Volvo";
        k.nosnostKg = 12000;
        repository.persist(k);
        assertNotNull(k.id, "ID must be assigned after persist");

        // findById
        Kamion found = repository.findById(k.id);
        assertNotNull(found);
        assertEquals("BA123AB", found.spz);

        // listAll contains it
        List<Kamion> all = repository.listAll();
        assertTrue(all.stream().anyMatch(it -> it.id.equals(k.id)));

        // delete removes it
        repository.delete(found);
        assertNull(repository.findById(k.id));
    }
}
