package sk.lorman.pokus.ewg.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "kamion")
public class Kamion extends PanacheEntityBase {


    @Id
    @SequenceGenerator(
            name = "kamionSeq",
            sequenceName = "kamion_SEQ",
            allocationSize = 1 // musí sedieť s INCREMENT BY v DB
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kamionSeq")
    public Long id;

    // ŠPZ (štátna poznávacia značka)
    @Column(name = "spz", nullable = false, unique = true)
    public String spz;

    // Značka / výrobca
    @Column(name = "znacka", nullable = false)
    public String znacka;

    // Nosnosť v kilogramoch
    @Column(name = "nosnost_kg", nullable = false)
    public Integer nosnostKg;
}
