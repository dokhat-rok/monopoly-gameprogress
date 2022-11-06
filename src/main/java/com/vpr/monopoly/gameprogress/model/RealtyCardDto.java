package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Schema(description = "Модель карты имущества")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RealtyCardDto {

    @Schema(description = "Позиция карты на поле")
    private int position;

    @Schema(description = "Название улицы")
    private String streetName;

    @Schema(description = "Владелец карты")
    private String owner;

    @Schema(description = "Прайс лист")
    private Map<Long, Long> priceMap;

    @Schema(description = "Стоимость карты")
    private Long costCard;

    @Schema(description = "Стоимость дома")
    private String costHouse;

    @Schema(description = "Текущее количество домов")
    private Long countHouse;

    @Schema(description = "Цвет карты")
    private String color;
}
