package com.vpr.monopoly.gameprogress.service.monopoly.client;

import com.vpr.monopoly.gameprogress.model.CardDto;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CardsManagerClient implements CardsManagerService {

    @Override
    public CardDto getChanceCard() {
        return null;
    }

    @Override
    public CardDto getCommunityChestCard() {
        return null;
    }

    @Override
    public void comebackPrisonCard() {

    }

    @Override
    public Map<String, List<CardDto>> initializingDecks() {
        return null;
    }
}
