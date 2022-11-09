package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Schema(description = "Модель стартовых данных для игры")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StartDataDto implements Serializable {

    @Schema(description = "Токен созданной сессии")
    private String token;

    @Schema(description = "Список игроков")
    private List<PlayerDto> players;

    @Schema(description = "Список всех карт имущества на поле")
    private List<RealtyCardDto> realtyList;
}
