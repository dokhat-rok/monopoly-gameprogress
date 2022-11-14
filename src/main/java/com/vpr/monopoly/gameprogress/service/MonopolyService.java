package com.vpr.monopoly.gameprogress.service;

public interface MonopolyService {
    default boolean checkConnection(){
        return true;
    }
}
