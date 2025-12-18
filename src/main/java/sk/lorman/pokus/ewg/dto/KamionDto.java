package sk.lorman.pokus.ewg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class KamionDto {
    public Long id;

    @NotBlank(message = "SPZ nesmie byť prázdna")
    public String spz;

    @NotBlank(message = "Značka nesmie byť prázdna")
    public String znacka;

    @NotNull(message = "Nosnosť musí byť zadaná")
    @Positive(message = "Nosnosť musí byť kladné číslo")
    public Integer nosnostKg;
}
