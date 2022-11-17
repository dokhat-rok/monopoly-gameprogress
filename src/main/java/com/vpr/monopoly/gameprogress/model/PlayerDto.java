package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Schema(description = "Модель игрока")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto implements Serializable {

    @Schema(description = "Массив из последних 2-х бросков кубика")
    private int[] lastRoll;

    @Schema(description = "Количество дублей")
    private int countDouble;

    @Schema(description = "Позиция на поле")
    private int position;

    @Schema(description = "Карта выхода из тюрьмы")
    private int prisonOutCard;

    @Schema(description = "Деньги")
    private Long money;

    @Schema(description = "Список имущества")
    private List<RealtyCardDto> realtyList;

    @Schema(description = "Список полного набора карточек одного цвета")
    private List<String> monopolies;

    @Schema(description = "Наименование фигуры")
    private String playerFigure;

    @Schema(description = "Нахождение в тюрьме")
    private Long inPrison;
}
