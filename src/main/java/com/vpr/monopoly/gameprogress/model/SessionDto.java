package com.vpr.monopoly.gameprogress.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * Активная сессия с текущими данными этой игры
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("SESSION")
public class SessionDto implements Serializable {

    /**
     * Список игроков на поле
     */
    private List<PlayerDto> players;

    /**
     * Список карточек имущества
     */
    private List<RealtyCardDto> realty;

    /**
     * Количество карт имущества с определенным цветом
     */
    private Map<String, Integer> realtyColors;

    /**
     * Колоды
     */
    private Map<String, List<CardDto>> decks;

    /**
     * Наличие карты выхода из тюрьмы в каждой колоде на данный момент
     */
    private Map<String, Boolean> isDecksHaveOutPrison;

    /**
     * История хода игры
     */
    private List<String> history;
}
