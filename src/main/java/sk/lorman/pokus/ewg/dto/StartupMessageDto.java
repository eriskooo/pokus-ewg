package sk.lorman.pokus.ewg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.lorman.pokus.ewg.util.JsonUtils;

/**
 * DTO reprezentujúce správu o štarte aplikácie posielanú cez Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartupMessageDto {
    private String timestamp;   // ISO-8601 čas vytvorenia správy
    private String message;     // Popisná správa
    private String appRandom;   // Ukážková vlastná hodnota z configu

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
