package com.vpr.monopoly.gameprogress.service.monopoly;

import com.vpr.monopoly.gameprogress.model.ActionDto;

public interface BankService extends MonopolyService{

    ActionDto playerToBankInteraction(ActionDto action);

    Boolean isPlayerToBankInteraction(ActionDto action);

    ActionDto playerToPlayerInteraction(ActionDto action);

    Boolean isPlayerToPlayerInteraction(ActionDto action);
}
