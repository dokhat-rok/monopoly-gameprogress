package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Schema(description = "Активная сессия с текущими данными этой игры")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("SESSION")
public class SessionDto implements Serializable {

    @Schema(description = "Список игроков на поле")
    private List<PlayerDto> players;

    @Schema(description = "Список карточек имущества")
    private List<RealtyCardDto> realty;

    @Schema(description = "Текущая колода карточек шанс")
    private List<CardDto> chanceCards;

    @Schema(description = "Колоды")
    private Map<String, List<CardDto>> decks;

    @Schema(description = "Список игроков в тюрьме")
    private List<PlayerDto> playersInPrison;

    @Schema(description = "История хода игры")
    private List<String> history;
}
