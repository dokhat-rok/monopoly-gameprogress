package com.vpr.monopoly.gameprogress.service.monopoly.impl;

import com.vpr.monopoly.gameprogress.model.CardDto;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardsManagerServiceImpl implements CardsManagerService {

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
