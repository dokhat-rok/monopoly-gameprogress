package com.vpr.monopoly.gameprogress.utils;

import com.vpr.monopoly.gameprogress.service.ServicesManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class ServicesUtils {

    public static ServicesManager INSTANCE;

    private final ServicesManager servicesManager;

    @PostConstruct
    private void init(){
        INSTANCE = servicesManager;
    }
}
