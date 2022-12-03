package com.vpr.monopoly.gameprogress.service.monopoly;

import com.vpr.monopoly.gameprogress.model.CardDto;

import java.util.List;
import java.util.Map;

public interface CardsManagerService extends MonopolyService {

    CardDto getChanceCard(String token);

    CardDto getCommunityChestCard(String token);

    void comebackPrisonCard(String token);

    Map<String, List<CardDto>> initializingDecks();
}
