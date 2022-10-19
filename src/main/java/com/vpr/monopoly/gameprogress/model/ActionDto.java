package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Schema(description = "Модель действия в ходе игры")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto {
    @Schema(description = "Тип действия")
    private enum actionType {
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
        private String label;

        actionType(String label) {
            this.label = label;
        }
    }
    @Schema(description = "Действие")
    private Map<String, String> actionBody;
}
