package sk.lorman.pokus.ewg.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
    @NotBlank(message = "SPZ nesmie byť prázdna")
    public String spz;

    // Značka / výrobca
    @Column(name = "znacka", nullable = false)
    @NotBlank(message = "Značka nesmie byť prázdna")
    public String znacka;

    // Nosnosť v kilogramoch
    @Column(name = "nosnost_kg", nullable = false)
    @NotNull(message = "Nosnosť musí byť zadaná")
    @Positive(message = "Nosnosť musí byť kladné číslo")
    public Integer nosnostKg;
}
