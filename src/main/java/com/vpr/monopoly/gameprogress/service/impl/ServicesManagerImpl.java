package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.model.enam.ServiceType;
import com.vpr.monopoly.gameprogress.service.*;
import com.vpr.monopoly.gameprogress.service.client.BankClient;
import com.vpr.monopoly.gameprogress.service.client.CardsManagerClient;
import com.vpr.monopoly.gameprogress.service.client.PrisonClient;
import com.vpr.monopoly.gameprogress.service.client.RealtyManagerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ServicesManagerImpl implements ServicesManager {

    private Map<ServiceType, MonopolyService> services;

    private ServicesManagerImpl(){
        this.checkConnect();
    }

    @Override
    public BankService getBankService() {
        return (BankService) services.get(ServiceType.BANK);
    }

    @Override
    public CardsManagerService getCardsManagerService() {
        return (CardsManagerService) services.get(ServiceType.CARDS_MANAGER);
    }

    @Override
    public PrisonService getPrisonService() {
        return (PrisonService) services.get(ServiceType.PRISON);
    }

    @Override
    public RealtyManagerService getRealtyManagerService() {
        return (RealtyManagerService) services.get(ServiceType.REALTY_MANAGER);
    }

    @Override
    public void checkConnect() {
        Map<ServiceType, MonopolyService> checkedServices = new HashMap<>(Map.of(
                ServiceType.BANK, new BankClient(),
                ServiceType.CARDS_MANAGER, new CardsManagerClient(),
                ServiceType.REALTY_MANAGER, new RealtyManagerClient(),
                ServiceType.PRISON, new PrisonClient()
        ));

        ServiceType type = ServiceType.BANK;
        BankService bankService = (BankService) checkedServices.get(type);
        if(!bankService.checkConnection()){
            checkedServices.put(type, new BankServiceImpl());
        }

        type = ServiceType.CARDS_MANAGER;
        CardsManagerService cardsManagerService = (CardsManagerService) checkedServices.get(type);
        if (!getBankService().checkConnection()){
            checkedServices.put(type, new CardsManagerServiceImpl());
        }

        type = ServiceType.PRISON;
        PrisonService prisonService = (PrisonService) checkedServices.get(type);
        if(!prisonService.checkConnection()){
            checkedServices.put(type, new PrisonServiceImpl());
        }

        type = ServiceType.REALTY_MANAGER;
        RealtyManagerService realtyManagerService = (RealtyManagerService) checkedServices.get(type);
        if(!realtyManagerService.checkConnection()){
            checkedServices.put(type, new RealtyManagerServiceImpl());
        }

        services = checkedServices;
    }
}
