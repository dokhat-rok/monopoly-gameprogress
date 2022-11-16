package com.vpr.monopoly.gameprogress.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final SessionRepository sessionRepository;

    private final ServicesManager servicesManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        List<String> resultAction = new ArrayList<>();
        PlayerDto player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
        List<PlayerDto> players = session.getPlayers();

        switch (action.getActionType()) {
            case "DropDice":
                int firstThrow = (int) (Math.random() * 7);
                int secondThrow = (int) (Math.random() * 7);

                player.setLastRoll(new int[] {firstThrow, secondThrow});
                player.setPosition(firstThrow + secondThrow);

                if (firstThrow == secondThrow && player.getCountDouble() != 3) {
                    player.setCountDouble(player.getCountDouble() + 1);
                }
                else if (firstThrow == secondThrow && player.getInPrison() != 0L) {
                    player.setCountDouble(player.getCountDouble() + 1);
                    player.setInPrison(0L);
                }
                else if (firstThrow == secondThrow && player.getCountDouble() == 2) {
                    player.setCountDouble(0);
                    servicesManager.getPrisonService().imprisonPlayer(player);
                    player.setPosition(10);
                }

                MapType mapType = MonopolyMap.getTypeByCellNumber(player.getPosition());
                generationPossibleActions(mapType, player, players, resultAction, session);
                actionSwap(players, resultAction);
                break;
            case "EndTurn":
                players.add(players.remove(0));
                addActionByType(resultAction, "DropDice");
                break;
            case "BuyRealty":
                RealtyCardDto card = objectMapper.convertValue(
                        action.getActionBody().get("realtyCard"), RealtyCardDto.class
                );
                if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action) && card.getOwner().equals("")) {
                    List<RealtyCardDto> updateList = player.getRealtyList();
                    updateList.add(card);
                    player.setRealtyList(updateList);
                    player.setMoney(player.getMoney() - card.getCostCard());
                    addActionByType(resultAction, "EndTurn");
                }
                else if (!card.getOwner().equals("")) {
                    for (PlayerDto owner: players) {
                        if (owner.getPlayerFigure().equals(card.getOwner())) {
                            if (servicesManager.getRealtyManagerService().isPlayerToPlayerInteraction(action)) {
                                owner.setMoney(owner.getMoney() + card.getCostCard());
                                player.setMoney(player.getMoney() - card.getCostCard());
                                addActionByType(resultAction, "EndTurn");
                            }
                            else if (player.getRealtyList() != null) {
                                resultAction.add("SellRealty");
                                //TODO добавить продажу дома
                            }
                        }
                    }
                }
                break;
            case "BuyHouse":
                //TODO Сделать
                break;
            case "leavePrisonByCard":
                player.setPrisonOutCard(0);
                servicesManager.getCardsManagerService().comebackPrisonCard();
                //TODO Получить данные из карточки выхода из тюрьмы для изменения игрока
                break;
            case "leavePrisonByMoney":
                player.setInPrison(0L);
                addActionByType(resultAction, "EndTurn");
                break;
            case "SellHouse":
                //TODO Сделать
                break;
            case "SellRealty":
                //TODO Проверка на цвет собственности перед продажей
                break;
            case "MoneyOperation":
                List<?> playersList = objectMapper.convertValue(action.getActionBody().get("player"), List.class);
                PlayerDto player1 = (PlayerDto) playersList.get(0);
                PlayerDto player2 = (PlayerDto) playersList.get(1);
                player1.setMoney(player1.getMoney() - objectMapper.convertValue(
                        action.getActionBody().get("money"), Long.class
                        )
                );
                player2.setMoney(player1.getMoney() + objectMapper.convertValue(
                                action.getActionBody().get("money"), Long.class
                        )
                );
                addActionByType(resultAction, "EndTurn");
                break;
            case "Swap":
                List<?> offer1 = objectMapper.convertValue(action.getActionBody().get("offerOnPlayer1"), List.class);
                List<?> offer2 = objectMapper.convertValue(action.getActionBody().get("offerOnPlayer2"), List.class);
                if (offer1 != null && offer2 != null) {
                    PlayerDto playerOne = objectMapper.convertValue(
                            action.getActionBody().get("player1"),
                            PlayerDto.class
                    );
                    PlayerDto playerTwo = objectMapper.convertValue(
                            action.getActionBody().get("player2"),
                            PlayerDto.class
                    );
                    List<RealtyCardDto> updateList1 = playerOne.getRealtyList();
                    List<RealtyCardDto> updateList2 = playerTwo.getRealtyList();

                    updateList1.remove((RealtyCardDto) offer2.get(0));
                    updateList1.add((RealtyCardDto) offer2.get(0));
                    updateList2.remove((RealtyCardDto) offer1.get(0));
                    updateList2.add((RealtyCardDto) offer1.get(0));

                    playerOne.setRealtyList(updateList1);
                    playerTwo.setRealtyList(updateList2);
                }
                addActionByType(resultAction, "EndTurn");
                break;
            case "Waiting":
                actionWaiting(action, resultAction);
                addActionByType(resultAction, "DropDice");
                break;
        }

        sessionRepository.set(sessionToken, session);
        return ActionDto.builder()
                .actionType(action.getActionType())
                .actionBody(Map.of("ResultAction", resultAction))
                .build();
    }

    @Override
    public List<String> endGame() {
        return null;
    }

    private void generationPossibleActions(
            MapType mapType,
            PlayerDto player,
            List<PlayerDto> players,
            List<String> resultAction,
            SessionDto session
    ) {
        switch (mapType) {
            case REALTY_CELL:
                RealtyCardDto card = session.getRealty().get(player.getPosition());

                playerBuyAndSellAction(card, "BuyRealty", resultAction, player);
                playerBuyAndSellAction(card, "SellRealty", resultAction, player);
                break;
            case PAY_CELL:
                long money;
                if (player.getPosition() == 4) {
                    money = 200L;
                    ActionDto action = ActionDto.builder()
                            .actionType("MoneyOperation")
                            .build();
                    action.getActionBody().put("player", player);
                    action.getActionBody().put("money", money);
                    if (servicesManager.getBankService().isPlayerToBankInteraction(action)) {
                        resultAction.add("MoneyOperation");
                        resultAction.add("EndTurn");
                    }
                }
                break;
            case COMMUNITY_CHEST_CELL:
                CardDto communityChestCard = servicesManager.getCardsManagerService().getCommunityChestCard();
                //TODO Сделать
                break;
            case CHANCE_CELL:
                CardDto chanceCard = servicesManager.getCardsManagerService().getChanceCard();
                //TODO Сделать
                break;
            case PARKING_CELL:
                actionsForParkingAndVisitingPrison(resultAction, player, players);
                break;
            case VISITING_PRISON_CELL:
                if (player.getInPrison() != 0) {
                    getOutOfPrison(player, resultAction);
                }
                else {
                    actionsForParkingAndVisitingPrison(resultAction, player, players);
                }
                break;
            case TO_PRISON_CELL:
                servicesManager.getPrisonService().imprisonPlayer(player);
                player.setPosition(10);
                getOutOfPrison(player, resultAction);
                break;
        }
    }

    private void playerBuyAndSellAction(
            RealtyCardDto card,
            String actionType,
            List<String> resultAction,
            PlayerDto player
    ) {
        ActionDto action = ActionDto.builder()
                .actionType(actionType)
                .build();
        action.getActionBody().put("player", player);

        if (actionType.equals("BuyRealty")) {
            action.getActionBody().put("realtyCard", card);
            if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action) && card.getOwner().equals("")) {
                resultAction.add(actionType);
            }
            else if (!card.getOwner().equals("")) {
                if (servicesManager.getRealtyManagerService().isPlayerToPlayerInteraction(action)) {
                    resultAction.add("MoneyOperation");
                }
                else if (player.getRealtyList() != null) {
                    resultAction.add("SellRealty");
                    //TODO добавить продажу дома
                }
            }
        }
        else if (actionType.equals("SellRealty") && player.getRealtyList() != null) {
            resultAction.add(actionType);
        }
    }

    private void actionsForParkingAndVisitingPrison(
            List<String> resultAction,
            PlayerDto player,
            List<PlayerDto> players
    ) {
        playerBuyAndSellAction(null, "SellRealty", resultAction, player);
        actionSwap(players, resultAction);
    }

    private void actionSwap(List<PlayerDto> players, List<String> resultAction) {
        List<PlayerDto> playersBySwap = new ArrayList<>();
        for (PlayerDto player: players) {
            if (player.getRealtyList() !=null) {
                playersBySwap.add(player);
            }
        }
        if (playersBySwap.size() != 0) {
            resultAction.add("Swap");
        }
    }

    private void actionWaiting(ActionDto action, List<String> resultAction) {
        if (servicesManager.getPrisonService().isWaiting(action)) {
            servicesManager.getPrisonService().waiting(action);
            resultAction.add("EndTurn");
        }
    }

    private void addActionByType(List<String> resultAction, String actionType) {
        resultAction.add(actionType);
    }

    private void getOutOfPrison(PlayerDto player, List<String> resultAction) {
        if (player.getMoney() >= 50000) {
            addActionByType(resultAction, "leavePrisonByMoney");
            resultAction.add("EndTurn");
        }
        if (player.getPrisonOutCard() == 1) {
            addActionByType(resultAction, "leavePrisonByCard");
            resultAction.add("EndTurn");
        }
    }
}
