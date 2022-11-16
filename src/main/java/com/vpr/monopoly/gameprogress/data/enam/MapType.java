package com.vpr.monopoly.gameprogress.data.enam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Тип карты")
@Getter
public enum MapType {

    START_CELL,

    PAY_CELL,

    COMMUNITY_CHEST_CELL,

    CHANCE_CELL,

    REALTY_CELL,

    PARKING_CELL,

    VISITING_PRISON_CELL,

    TO_PRISON_CELL

}
