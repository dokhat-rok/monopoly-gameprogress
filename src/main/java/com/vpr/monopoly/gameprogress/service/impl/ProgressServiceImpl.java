package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.StartDataDto;
import com.vpr.monopoly.gameprogress.repository.SessionRepository;
import com.vpr.monopoly.gameprogress.service.ProgressService;
import com.vpr.monopoly.gameprogress.service.ServicesManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final SessionRepository sessionRepository;

    private final ServicesManager servicesManager;

    @PostConstruct
    private void init(){
        servicesManager.checkConnect();
    }

    @Override
    public StartDataDto startGame(Long count, String[] players) {
        return null;
    }

    @Override
    public ActionDto actionPlayer(String sessionToken, ActionDto action) {
        return null;
    }

    @Override
    public List<String> endGame() {
        return null;
    }
}
