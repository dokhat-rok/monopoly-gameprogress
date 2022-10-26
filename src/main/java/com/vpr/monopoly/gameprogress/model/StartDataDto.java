package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Schema(description = "Модель стартовых данных для игры")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StartDataDto {

    @Schema(description = "Список игроков")
    private ArrayList<PlayerDto> players;

    @Schema(description = "Список всех карт имущества на поле")
    private ArrayList<RealtyCardDto> realtyList;
}
