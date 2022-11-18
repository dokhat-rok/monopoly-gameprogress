package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.service.*;
import com.vpr.monopoly.gameprogress.service.monopoly.BankService;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import com.vpr.monopoly.gameprogress.service.monopoly.PrisonService;
import com.vpr.monopoly.gameprogress.service.monopoly.RealtyManagerService;
import com.vpr.monopoly.gameprogress.service.monopoly.client.PrisonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicesManagerImpl implements ServicesManager {

    private boolean bankEnabled = true;

    private boolean prisonEnabled = true;

    private boolean realtyEnabled = true;

    private boolean cardsEnabled = true;

    private final BankService bankClient;

    private final PrisonClient prisonClient;

    private final RealtyManagerService realtyManagerClient;

    private final CardsManagerService cardsManagerClient;

    private final BankService bankServiceImpl;

    private final PrisonService prisonServiceImpl;

    private final RealtyManagerService realtyManagerServiceImpl;

    private final CardsManagerService cardsManagerServiceImpl;

    @PostConstruct
    private void init(){
        this.checkConnect();
    }

    @Override
    public BankService getBankService() {
        return bankEnabled ? bankClient : bankServiceImpl;
    }

    @Override
    public CardsManagerService getCardsManagerService() {
        return cardsEnabled ? cardsManagerClient : cardsManagerServiceImpl;
    }

    @Override
    public PrisonService getPrisonService() {
        return prisonEnabled ? prisonClient : prisonServiceImpl;
    }

    @Override
    public RealtyManagerService getRealtyManagerService() {
        return realtyEnabled ? realtyManagerClient : realtyManagerServiceImpl;
    }

    @Override
    public void checkConnect() {
        boolean bankEnabled = true;
        boolean prisonEnabled = true;
        boolean realtyEnabled = true;
        boolean cardsEnabled = true;

        if(!bankClient.checkConnection()){
            bankEnabled = false;
        }

        if(!prisonClient.checkConnection()){
            prisonEnabled = false;
        }

        if(!realtyManagerClient.checkConnection()){
            realtyEnabled = false;
        }

        if(!cardsManagerClient.checkConnection()){
            cardsEnabled = false;
        }

        this.bankEnabled = bankEnabled;
        this.prisonEnabled = prisonEnabled;
        this.realtyEnabled = realtyEnabled;
        this.cardsEnabled = cardsEnabled;
    }
}
