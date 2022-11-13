package com.vpr.monopoly.gameprogress.data.enam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Тип карты")
@Getter
public enum MapType {

    START_CELL("StartCell"),

    PAY_CELL("PayCell"),

    COMMUNITY_CHEST_CELL("CommunityChestCell"),

    CHANCE_CELL("ChanceCell"),

    REALTY_CELL("RealtyCell"),

    PARKING_CELL("ParkingCell"),

    VISITING_PRISON_CELL("VisitingPrisonCell"),

    TO_PRISON_CELL("ToPrisonCell");

    private final String label;

    MapType(String label) {
        this.label = label;
    }
}
