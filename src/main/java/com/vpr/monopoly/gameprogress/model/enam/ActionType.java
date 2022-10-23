package com.vpr.monopoly.gameprogress.model.enam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Тип действия")
@Getter
public enum ActionType {

    DROP_DICE("DropDice"),

    END_TURN("EndTurn"),

    BUY_REALTY("BuyRealty"),

    BUY_HOUSE("BuyHouse"),

    lEAVE_PRISON_BY_CARD("leavePrisonByCard"),

    lEAVE_PRISON_BY_MONEY("leavePrisonByMoney"),

    SELL_HOUSE("SellHouse"),

    SELL_REALTY("SellRealty"),

    MONEY_OPERATION("MoneyOperation"),

    SWAP("Swap"),

    WAITING("Waiting");

    private final String label;

    ActionType(String label) {
        this.label = label;
    }
}
