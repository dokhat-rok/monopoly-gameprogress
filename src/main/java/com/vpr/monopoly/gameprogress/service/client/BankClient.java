package com.vpr.monopoly.gameprogress.service.client;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankClient implements BankService {

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
    public boolean usPlayerToPlayerInteraction(ActionDto action) {
        return false;
    }
}
