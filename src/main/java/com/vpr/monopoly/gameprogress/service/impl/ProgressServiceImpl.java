package com.vpr.monopoly.gameprogress.service.impl;

import com.vpr.monopoly.gameprogress.data.MonopolyMap;
import com.vpr.monopoly.gameprogress.data.enam.MapType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    .realtyList(new ArrayList<>())
                    .build());
        }

        String token = LocalDateTime.now().toString();
        List<RealtyCardDto> realtyCardList = servicesManager.getRealtyManagerService().getAllRealtyCards();
        sessionRepository.set(token, SessionDto.builder()
                        .players(newPlayers)
                        .realty(realtyCardList)
                        .decks(servicesManager.getCardsManagerService().initializingDecks())
                        .playersInPrison(new ArrayList<>())
                        .history(new ArrayList<>())
                        .build()
                );

        return StartDataDto.builder()
                .token(token)
                .players(newPlayers)
                .realtyList(realtyCardList)
                .build();
    }

    @Override
    public ActionDto actionPlayer(String sessionToken, ActionDto action) {
        SessionDto session = sessionRepository.get(sessionToken);
        List<ActionDto> resultAction = new ArrayList<>();
        PlayerDto currentPlayer = session.getPlayers().get(0);

        switch (action.getActionType()) {
            case "DropDice":
                int firstThrow = (int) (Math.random() * 7);
                int secondThrow = (int) (Math.random() * 7);

                currentPlayer.setLastRoll(new int[] {firstThrow, secondThrow});
                currentPlayer.setPosition(firstThrow + secondThrow);

                MapType mapType = MonopolyMap.getTypeByCellNumber(currentPlayer.getPosition());

                this.generationPossibleActions(mapType, currentPlayer, resultAction, session, sessionToken);

                break;
            case "EndTurn":
                List<PlayerDto> players = session.getPlayers();
                players.add(players.remove(0));
                break;
            case "BuyRealty":
                if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(ActionDto.builder()
                        .actionType("BuyRealty")
                        .actionBody(Map.of(sessionToken, currentPlayer))
                        .build())
                ) {
                    RealtyCardDto card = session.getRealty().get(currentPlayer.getPosition());
                    currentPlayer.getRealtyList().add(card);
                    currentPlayer.setMoney(currentPlayer.getMoney() - card.getCostCard());
                }
                break;
            case "BuyHouse":

                break;
            case "leavePrisonByCard":
                if (currentPlayer.getPrisonOutCard() == 1) {
                    currentPlayer.setPrisonOutCard(0);
                    currentPlayer.setInPrison(0L);
                }
                break;
            case "SellHouse":

                break;
            case "SellRealty":

                break;
            case "MoneyOperation":
                //когда встал на карточку заплатить деньги и все?
                break;
            case "Swap":
                //в какой момент происходит?
                break;
            case "Waiting":
                if (currentPlayer.getInPrison() != 0L) {
                    currentPlayer.setInPrison(currentPlayer.getInPrison() - 1L);
                    resultAction.add(ActionDto.builder()
                            .actionType("DropDice")
                            .actionBody(Map.of(sessionToken, currentPlayer))
                            .build()
                    );
                }
                break;
        }

        sessionRepository.set(sessionToken, session);

        return ActionDto.builder()
                .actionType("ResultAction")
                .actionBody(Map.of(sessionToken, resultAction))
                .build();
    }

    @Override
    public List<String> endGame() {
        return null;
    }

    private void generationPossibleActions(
            MapType mapType,
            PlayerDto player,
            List<ActionDto> resultAction,
            SessionDto session,
            String sessionToken
    ) {
        switch (mapType) {
            case REALTY_CELL:
                RealtyCardDto card = session.getRealty().get(player.getPosition());
                if (!card.getOwner().equals("")) {
                    playerToPlayerInteraction("BuyRealty", resultAction, player, sessionToken);
                }
                else if (!card.getOwner().equals(player.getPlayerFigure())) {
                    playerToBankInteraction("BuyRealty", resultAction, player, sessionToken);
                }
                else {
                    playerToPlayerInteraction("SellRealty", resultAction, player, sessionToken);
                }
                break;
            case PAY_CELL:
                playerToBankInteraction("MoneyOperation", resultAction, player, sessionToken);
                break;
            case COMMUNITY_CHEST_CELL:
                CardDto communityChestCard = servicesManager.getCardsManagerService().getCommunityChestCard();

                break;
            case CHANCE_CELL:
                CardDto chanceCard = servicesManager.getCardsManagerService().getChanceCard();

                break;
            case PARKING_CELL:
                //куда отсюда деваться?
                break;
            case VISITING_PRISON_CELL:
                //куда отсюда деваться?
                break;
            case TO_PRISON_CELL:
                if (player.getInPrison() == 0L) {
                    player.setInPrison(3L);
                }
                else {
                    int[] lastRoll = player.getLastRoll();
                    if (lastRoll[0] == lastRoll[1]) {
                        player.setInPrison(0L);
                        player.setPosition(lastRoll[0] + lastRoll[1]);
                    }
                }
                break;
        }
    }

    private void playerToBankInteraction(
            String action,
            List<ActionDto> resultAction,
            PlayerDto player,
            String sessionToken
    ) {
        if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(ActionDto.builder()
                .actionType(action)
                .actionBody(Map.of(sessionToken, player))
                .build())
        ) {
            resultAction.add(ActionDto.builder()
                    .actionType(action)
                    .actionBody(Map.of(sessionToken, player))
                    .build()
            );
        }
    }

    private void playerToPlayerInteraction(
            String action,
            List<ActionDto> resultAction,
            PlayerDto player,
            String sessionToken
    ) {
        if (servicesManager.getRealtyManagerService().isPlayerToPlayerInteraction(ActionDto.builder()
                .actionType(action)
                .actionBody(Map.of(sessionToken, player))
                .build())
        ) {
            resultAction.add(ActionDto.builder()
                    .actionType(action)
                    .actionBody(Map.of(sessionToken, player))
                    .build()
            );
        }
    }
}
