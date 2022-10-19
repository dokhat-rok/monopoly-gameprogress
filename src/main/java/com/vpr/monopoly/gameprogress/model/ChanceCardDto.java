package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Schema(description = "Модель карты шанса")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChanceCardDto {
    @Schema(description = "Действия")
    private List<String> actions;
    @Schema(description = "Описание карты")
    private String description;
    @Schema(description = "Атрибуты карты")
    private Map<String, String> options;
}
