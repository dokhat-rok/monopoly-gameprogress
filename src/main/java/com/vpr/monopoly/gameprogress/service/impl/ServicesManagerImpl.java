package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ServicesManagerImpl implements ServicesManager {

    @Override
    public BankService getBankService() {
        return null;
    }

    @Override
    public CardsManagerService getCardsManagerService() {
        return null;
    }

    @Override
    public PrisonService getPrisonService() {
        return null;
    }

    @Override
    public RealtyManagerService getRealtyManagerService() {
        return null;
    }

    @Override
    public void checkConnect() {

    }
}
