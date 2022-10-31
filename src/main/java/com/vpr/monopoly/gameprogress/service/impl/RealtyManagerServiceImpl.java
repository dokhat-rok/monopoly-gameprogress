package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;
import com.vpr.monopoly.gameprogress.service.RealtyManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RealtyManagerServiceImpl implements RealtyManagerService {

    @Override
    public ActionDto playerToBankInteraction(ActionDto action) {
        return null;
    }

    @Override
    public boolean isPlayerToBankInteraction(ActionDto action) {
        return false;
    }

    @Override
    public ActionDto playerToPlayerInteraction(ActionDto action) {
        return null;
    }

    @Override
    public boolean isPlayerToPlayerInteraction(ActionDto action) {
        return false;
    }

    @Override
    public List<RealtyCardDto> getAllRealtyCards() {
        return null;
    }
}
