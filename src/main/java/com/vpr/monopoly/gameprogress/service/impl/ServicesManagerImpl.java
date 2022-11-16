package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.service.*;
import com.vpr.monopoly.gameprogress.service.client.BankClient;
import com.vpr.monopoly.gameprogress.service.client.CardsManagerClient;
import com.vpr.monopoly.gameprogress.service.client.PrisonClient;
import com.vpr.monopoly.gameprogress.service.client.RealtyManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//TODO сделать класс паттерном - синглтон
@Service
@Slf4j
public class ServicesManagerImpl implements ServicesManager {

    private BankService bankService;

    private CardsManagerService cardsManagerService;

    private PrisonService prisonService;

    private RealtyManagerService realtyManagerService;

    public ServicesManagerImpl(){
        this.checkConnect();
    }

    @Override
    public BankService getBankService() {
        return bankService;
    }

    @Override
    public CardsManagerService getCardsManagerService() {
        return cardsManagerService;
    }

    @Override
    public PrisonService getPrisonService() {
        return prisonService;
    }

    @Override
    public RealtyManagerService getRealtyManagerService() {
        return realtyManagerService;
    }

    @Override
    public void checkConnect() {
        BankService bankService = new BankClient();
        if(!bankService.checkConnection()){
            bankService = new BankServiceImpl();
        }
        this.bankService = bankService;

        CardsManagerService cardsManagerService = new CardsManagerClient();
        if (!cardsManagerService.checkConnection()){
            cardsManagerService = new CardsManagerServiceImpl();
        }
        this.cardsManagerService = cardsManagerService;

        PrisonService prisonService = new PrisonClient(this);
        if(!prisonService.checkConnection()){
            prisonService = new PrisonServiceImpl();
        }
        this.prisonService = prisonService;

        RealtyManagerService realtyManagerService = new RealtyManagerClient();
        if(!realtyManagerService.checkConnection()){
            realtyManagerService = new RealtyManagerServiceImpl();
        }
        this.realtyManagerService = realtyManagerService;
    }
}
