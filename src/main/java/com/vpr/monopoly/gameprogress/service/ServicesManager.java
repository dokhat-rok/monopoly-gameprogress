package com.vpr.monopoly.gameprogress.service;

public interface ServicesManager {

    BankService getBankService();

    CardsManagerService getCardsManagerService();

    PrisonService getPrisonService();

    RealtyManagerService getRealtyManagerService();

    void checkConnect();
}
