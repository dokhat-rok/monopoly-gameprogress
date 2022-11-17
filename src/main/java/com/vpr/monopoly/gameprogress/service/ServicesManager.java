package com.vpr.monopoly.gameprogress.service;

import com.vpr.monopoly.gameprogress.service.monopoly.BankService;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import com.vpr.monopoly.gameprogress.service.monopoly.PrisonService;
import com.vpr.monopoly.gameprogress.service.monopoly.RealtyManagerService;

public interface ServicesManager {

    BankService getBankService();

    CardsManagerService getCardsManagerService();

    PrisonService getPrisonService();

    RealtyManagerService getRealtyManagerService();

    void checkConnect();
}
