package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.model.*;
import com.vpr.monopoly.gameprogress.repository.SessionRepository;
import com.vpr.monopoly.gameprogress.service.ProgressService;
import com.vpr.monopoly.gameprogress.service.ServicesManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final SessionRepository sessionRepository;

    private final ServicesManager servicesManager;

    @Value("${start.player.money}")
    private Long money;

    @PostConstruct
    private void init(){
        servicesManager.checkConnect();
    }

    @Override
    public StartDataDto startGame(Long count, String[] players) {
        List<PlayerDto> newPlayers = new ArrayList<>();

        for (String player: players) {
            newPlayers.add(PlayerDto.builder()
                    .playerFigure(player)
                    .money(money)
                    .realtyList(servicesManager.getRealtyManagerService().getAllRealtyCards())
                    .build());
        }

        List<PlayerDto> playersInPrison = new ArrayList<>();
        playersInPrison.add(PlayerDto.builder().build());

        sessionRepository.set(
                LocalDateTime.now().toString(),
                SessionDto.builder()
                        .players(newPlayers)
                        .realty(servicesManager.getRealtyManagerService().getAllRealtyCards())
                        .decks(servicesManager.getCardsManagerService().initializingDecks())
                        .playersInPrison(playersInPrison)
                        .build()
                );

        return StartDataDto.builder()
                .token(LocalDateTime.now().toString())
                .players(newPlayers)
                .realtyList(servicesManager.getRealtyManagerService().getAllRealtyCards())
                .build();
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
