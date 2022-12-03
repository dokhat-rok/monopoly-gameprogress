package com.vpr.monopoly.gameprogress.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Модель карты общественной казны и шанса
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CardDto implements Serializable {

    /**
     * Тип карты
     */
    private String cardType;

    /**
     * Описание карты
     */
    private String description;

    /**
     * Тип дейсвтия, которое должен выполнить игрок
     */
    private String cardActionType;

    /**
     * Дополнительный параметр карты к дейсвтию
     */
    private Integer parameter;
}
