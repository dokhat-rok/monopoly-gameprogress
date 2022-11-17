package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.service.*;
import com.vpr.monopoly.gameprogress.service.monopoly.client.BankClient;
import com.vpr.monopoly.gameprogress.service.monopoly.client.CardsManagerClient;
import com.vpr.monopoly.gameprogress.service.monopoly.client.PrisonClient;
import com.vpr.monopoly.gameprogress.service.monopoly.client.RealtyManagerClient;
import com.vpr.monopoly.gameprogress.service.monopoly.BankService;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import com.vpr.monopoly.gameprogress.service.monopoly.PrisonService;
import com.vpr.monopoly.gameprogress.service.monopoly.RealtyManagerService;
import com.vpr.monopoly.gameprogress.service.monopoly.impl.BankServiceImpl;
import com.vpr.monopoly.gameprogress.service.monopoly.impl.CardsManagerServiceImpl;
import com.vpr.monopoly.gameprogress.service.monopoly.impl.PrisonServiceImpl;
import com.vpr.monopoly.gameprogress.service.monopoly.impl.RealtyManagerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicesManagerImpl implements ServicesManager {

    private final BankService bankClient;

    private CardsManagerService cardsManagerService;

    private final PrisonService prisonClient;

    private RealtyManagerService realtyManagerService;

    /*public ServicesManagerImpl(){
        this.checkConnect();
    }*/

    @PostConstruct
    private void init(){
        this.checkConnect();
    }

    @Override
    public BankService getBankService() {
        return bankClient;
    }

    @Override
    public CardsManagerService getCardsManagerService() {
        return cardsManagerService;
    }

    @Override
    public PrisonService getPrisonService() {
        return prisonClient;
    }

    @Override
    public RealtyManagerService getRealtyManagerService() {
        return realtyManagerService;
    }

    @Override
    public void checkConnect() {
        /*BankService bankService = new BankClient(bankBaseUrl);
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
            prisonService = new PrisonServiceImpl(this);
        }
        this.prisonService = prisonService;

        RealtyManagerService realtyManagerService = new RealtyManagerClient();
        if(!realtyManagerService.checkConnection()){
            realtyManagerService = new RealtyManagerServiceImpl();
        }
        this.realtyManagerService = realtyManagerService;*/
        bankClient.checkConnection();
        prisonClient.checkConnection();
    }
}
