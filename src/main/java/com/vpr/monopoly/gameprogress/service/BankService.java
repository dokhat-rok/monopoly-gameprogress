package com.vpr.monopoly.gameprogress.service;

import com.vpr.monopoly.gameprogress.model.ActionDto;

public interface BankService {

    ActionDto playerToBankInteraction(ActionDto action);

    boolean isPlayerToBankInteraction(ActionDto action);

    ActionDto playerToPlayerInteraction(ActionDto action);

    boolean isPlayerToPlayerInteraction(ActionDto action);
}
