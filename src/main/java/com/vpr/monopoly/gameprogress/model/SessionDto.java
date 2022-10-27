package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;

@Schema(description = "Активная сессия с текущими данными этой игры")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("SESSION")
public class SessionDto implements Serializable {

    @Schema(description = "Список игроков на поле")
    private List<Object> players;

    @Schema(description = "Список карточек имущества")
    private List<Object> realty;

    @Schema(description = "Текущая колода карточек шанс")
    private List<Object> chanceCards;

    @Schema(description = "Текущая колода карточек городской казны")
    private List<Object> communityChestCards;

    @Schema(description = "Список игроков в тюрьме")
    private List<Object> playersInPrison;
}
