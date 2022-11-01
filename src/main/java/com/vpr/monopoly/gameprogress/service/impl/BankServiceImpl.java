package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

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
}
