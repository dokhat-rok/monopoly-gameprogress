package com.vpr.monopoly.gameprogress.model.enam;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип действия")
public enum ActionType {

    DropDice,

    EndTurn,

    BuyRealty,

    BuyHouse,

    LeavePrisonByCard,

    LeavePrisonByMoney,

    SellHouse,

    SellRealty,

    MoneyOperation,

    Swap
}
