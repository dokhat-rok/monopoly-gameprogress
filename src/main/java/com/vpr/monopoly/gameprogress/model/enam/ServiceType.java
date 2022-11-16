package com.vpr.monopoly.gameprogress.model.enam;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServiceType {
    BANK("Bank Service"),
    CARDS_MANAGER("Cards Manager Service"),
    PRISON("Prison Service"),
    REALTY_MANAGER("Realty Manager Service");

    private final String name;
}
