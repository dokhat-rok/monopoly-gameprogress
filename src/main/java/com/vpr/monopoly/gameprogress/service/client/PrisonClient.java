package com.vpr.monopoly.gameprogress.service.client;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.service.PrisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrisonClient implements PrisonService {

    @Override
    public PlayerDto imprisonPlayer(PlayerDto player) {
        return null;
    }

    @Override
    public ActionDto waiting(ActionDto action) {
        return null;
    }

    @Override
    public boolean isWaiting(ActionDto action) {
        return false;
    }
}
