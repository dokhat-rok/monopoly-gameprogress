package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.service.PrisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrisonServiceImpl implements PrisonService {

    @Override
    public PlayerDto imprisonPlayer(PlayerDto player) {
        /*player.se*/

        return null;
    }

    @Override
    public ActionDto waiting(ActionDto action) {
        return null;
    }

    @Override
    public Boolean isWaiting(ActionDto action) {
        return false;
    }
}
