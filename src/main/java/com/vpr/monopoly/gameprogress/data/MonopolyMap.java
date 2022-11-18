package com.vpr.monopoly.gameprogress.data;

import com.vpr.monopoly.gameprogress.data.enam.MapType;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "Карта игры")
public class MonopolyMap {

    @Schema(description = "Карты на поле")
    public static final Map<Integer, MapType> data = new HashMap<>(){
        {
            put(0, MapType.START_CELL);
            put(1, MapType.REALTY_CELL);
            put(2, MapType.COMMUNITY_CHEST_CELL);
            put(3, MapType.REALTY_CELL);
            put(4, MapType.PAY_CELL);
            put(5, MapType.REALTY_CELL);
            put(6, MapType.REALTY_CELL);
            put(7, MapType.CHANCE_CELL);
            put(8, MapType.REALTY_CELL);
            put(9, MapType.REALTY_CELL);
            put(10, MapType.VISITING_PRISON_CELL);
            put(11, MapType.REALTY_CELL);
            put(12, MapType.REALTY_CELL);
            put(13, MapType.REALTY_CELL);
            put(14, MapType.REALTY_CELL);
            put(15, MapType.REALTY_CELL);
            put(16, MapType.REALTY_CELL);
            put(17, MapType.COMMUNITY_CHEST_CELL);
            put(18, MapType.REALTY_CELL);
            put(19, MapType.REALTY_CELL);
            put(20, MapType.PARKING_CELL);
            put(21, MapType.REALTY_CELL);
            put(22, MapType.CHANCE_CELL);
            put(23, MapType.REALTY_CELL);
            put(24, MapType.REALTY_CELL);
            put(25, MapType.REALTY_CELL);
            put(26, MapType.REALTY_CELL);
            put(27, MapType.REALTY_CELL);
            put(28, MapType.REALTY_CELL);
            put(29, MapType.REALTY_CELL);
            put(30, MapType.TO_PRISON_CELL);
            put(31, MapType.REALTY_CELL);
            put(32, MapType.REALTY_CELL);
            put(33, MapType.COMMUNITY_CHEST_CELL);
            put(34, MapType.REALTY_CELL);
            put(35, MapType.REALTY_CELL);
            put(36, MapType.CHANCE_CELL);
            put(37, MapType.REALTY_CELL);
            put(38, MapType.PAY_CELL);
            put(39, MapType.REALTY_CELL);
        }};

    public static MapType getTypeByCellNumber(int number) {
        return data.get(number);
    }

    public static Map<String, Integer> getColorsRealty(List<RealtyCardDto> realty) {
        Map<String, Integer> colorsRealty = new HashMap<>();
        for (RealtyCardDto realtyCard: realty) {
            if (!colorsRealty.containsKey(realtyCard.getColor())) {
                colorsRealty.put(realtyCard.getColor(), 1);
            }
            else {
                int count = colorsRealty.get(realtyCard.getColor()) + 1;
                colorsRealty.put(realtyCard.getColor(), count);
            }
        }

        return colorsRealty;
    }
}
