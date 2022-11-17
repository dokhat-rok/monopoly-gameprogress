package com.vpr.monopoly.gameprogress.service.monopoly.impl;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;
import com.vpr.monopoly.gameprogress.service.monopoly.RealtyManagerService;
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
    public Boolean isPlayerToBankInteraction(ActionDto action) {
        return false;
    }

    @Override
    public ActionDto playerToPlayerInteraction(ActionDto action) {
        return null;
    }

    @Override
    public Boolean isPlayerToPlayerInteraction(ActionDto action) {
        return false;
    }

    @Override
    public List<RealtyCardDto> getAllRealtyCards() {
        return null;
    }
}
