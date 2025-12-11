package sk.lorman.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Kamion extends PanacheEntity {

    // ŠPZ (štátna poznávacia značka)
    @Column(nullable = false, unique = true)
    public String spz;

    // Značka / výrobca
    @Column(nullable = false)
    public String znacka;

    // Nosnosť v kilogramoch
    @Column(nullable = false)
    public Integer nosnostKg;
}
