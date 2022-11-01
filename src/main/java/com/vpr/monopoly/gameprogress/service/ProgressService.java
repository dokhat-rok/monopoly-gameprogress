package com.vpr.monopoly.gameprogress.service;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.StartDataDto;

import java.util.List;

public interface ProgressService {

    StartDataDto startGame(Long count, String[] players);

    ActionDto actionPlayer(String sessionToken, ActionDto action);

    List<String> endGame();
}
