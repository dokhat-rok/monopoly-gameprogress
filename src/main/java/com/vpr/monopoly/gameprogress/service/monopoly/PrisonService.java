package com.vpr.monopoly.gameprogress.service.monopoly;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;

public interface PrisonService extends MonopolyService {

    PlayerDto imprisonPlayer(PlayerDto player);

    ActionDto waiting(String token, ActionDto action);

    Boolean isWaiting(ActionDto action);
}
