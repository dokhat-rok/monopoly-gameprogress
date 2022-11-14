package com.vpr.monopoly.gameprogress.service;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;

import java.util.List;

public interface RealtyManagerService extends MonopolyService {
    ActionDto playerToBankInteraction(ActionDto action);

    boolean isPlayerToBankInteraction(ActionDto action);

    ActionDto playerToPlayerInteraction(ActionDto action);

    boolean isPlayerToPlayerInteraction(ActionDto action);

    List<RealtyCardDto> getAllRealtyCards();
}
