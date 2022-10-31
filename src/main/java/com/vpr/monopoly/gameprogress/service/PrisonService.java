package com.vpr.monopoly.gameprogress.service;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;

public interface PrisonService {

    PlayerDto imprisonPlayer(PlayerDto player);

    ActionDto waiting(ActionDto action);

    boolean isWaiting(ActionDto action);
}
