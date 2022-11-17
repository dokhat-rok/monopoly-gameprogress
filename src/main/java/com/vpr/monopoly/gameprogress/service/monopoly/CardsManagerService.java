package com.vpr.monopoly.gameprogress.service.monopoly;

import com.vpr.monopoly.gameprogress.model.CardDto;

import java.util.List;
import java.util.Map;

public interface CardsManagerService extends MonopolyService {

    CardDto getChanceCard();

    CardDto getCommunityChestCard();

    void comebackPrisonCard();

    Map<String, List<CardDto>> initializingDecks();
}
