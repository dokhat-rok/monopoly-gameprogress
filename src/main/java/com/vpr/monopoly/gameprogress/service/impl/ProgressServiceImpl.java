package com.vpr.monopoly.gameprogress.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpr.monopoly.gameprogress.data.MonopolyMap;
import com.vpr.monopoly.gameprogress.data.enam.MapType;
import com.vpr.monopoly.gameprogress.model.*;
import com.vpr.monopoly.gameprogress.model.enam.ActionType;
import com.vpr.monopoly.gameprogress.repository.SessionRepository;
import com.vpr.monopoly.gameprogress.service.ProgressService;
import com.vpr.monopoly.gameprogress.service.ServicesManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.*;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final SessionRepository sessionRepository;

    private final ServicesManager servicesManager;

    private final ObjectMapper objectMapper;

    @Value("${progress.start.player.money}")
    private Long money;

    @Value("${progress.round.salary}")
    private Long salary;

    @Value("${progress.utility.name}")
    private String utility;

    @Value("${progress.station.name}")
    private String station;

    @Value("${progress.income.money}")
    private Long income;

    @Value("${progress.luxury.money}")
    private Long luxury;

    @Override
    public StartDataDto startGame(String[] players) {
        List<PlayerDto> newPlayers = new ArrayList<>();
        for (String player: players) {
            newPlayers.add(PlayerDto.builder()
                    .playerFigure(player)
                    .money(money)
                    .realtyList(new ArrayList<>())
                    .monopolies(new ArrayList<>())
                    .inPrison(0L)
                    .build());
        }

        Collections.shuffle(newPlayers);
        String token = LocalDateTime.now().toString();
        List<RealtyCardDto> realtyCardList = servicesManager.getRealtyManagerService().getAllRealtyCards();
        realtyCardList.sort(Comparator.comparing(RealtyCardDto::getPosition));
        sessionRepository.set(token, SessionDto.builder()
                        .players(newPlayers)
                        .realty(realtyCardList)
                        .realtyColors(MonopolyMap.getColorsRealty(realtyCardList))
                        .decks(servicesManager.getCardsManagerService().initializingDecks())
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
        List<PlayerDto> players = session.getPlayers();
        Map<String, Object> resultBody = new HashMap<>();
        PlayerDto player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
        RealtyCardDto realtyCard;
        Set<String> currentActions = new HashSet<>();
        Set<String> blockedActions = new HashSet<>();

        switch (ActionType.valueOf(action.getActionType())) {
            case DropDice:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                int firstThrow = ThreadLocalRandom.current().nextInt(1, 7);
                int secondThrow = ThreadLocalRandom.current().nextInt(1, 7);

                player.setLastRoll(new int[] {firstThrow, secondThrow});
                if(player.getInPrison() == 0) player.setPosition(player.getPosition() + firstThrow + secondThrow);
                int count = MonopolyMap.data.size();

                if (player.getPosition() / count == 1) {
                    ActionDto bankAction = ActionDto.builder()
                            .actionType(MoneyOperation.toString())
                            .actionBody(new HashMap<>(Map.of(
                                    "playerList", List.of(player),
                                    "money", salary
                            )))
                            .build();
                    bankAction = servicesManager.getBankService().playerToBankInteraction(bankAction);

                    List<PlayerDto> playersList = objectMapper.convertValue(bankAction.getActionBody().get("playerList"), new TypeReference<>() {});
                    player = playersList.get(0);
                    player.setPosition(player.getPosition() % count);
                }

                if(firstThrow == secondThrow) {
                    player.setCountDouble(player.getCountDouble() + 1);
                    if (player.getCredit() != 0) {
                        blockedActions.add(DropDice.toString());
                    }
                    else {
                        currentActions.add(DropDice.toString());
                    }
                }
                else {
                    player.setCountDouble(0);
                }

                if (player.getCountDouble() == 3) {
                    player = servicesManager.getPrisonService().imprisonPlayer(player);
                    player.setCountDouble(0);
                    player.setPosition(10);
                }
                else if (player.getCountDouble() == 1 && player.getInPrison() > 0) {
                    player.setPosition(player.getPosition() + firstThrow + secondThrow);
                }

                if (player.getInPrison() > 0) {
                    ActionDto prisonAction = ActionDto.builder()
                            .actionType(Waiting.toString())
                            .actionBody(new HashMap<>(Map.of(
                                    "player", player
                            )))
                            .build();
                    prisonAction = servicesManager.getPrisonService().waiting(prisonAction);
                    player = objectMapper.convertValue(prisonAction.getActionBody().get("player"), PlayerDto.class);
                }

                MapType mapType = MonopolyMap.getTypeByCellNumber(player.getPosition());
                generationPossibleActions(mapType, player, players, session, currentActions, blockedActions);
                break;
            case BuyRealty:
                action = servicesManager.getRealtyManagerService().playerToBankInteraction(action);
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                updateRealtyInSession(realtyCard, session);
                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                break;
            case BuyHouse:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                buyAndSellPlayersAction(action, player, realtyCard, session);
                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionSellHouse(player, currentActions);
                break;
            case SellHouse:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                buyAndSellPlayersAction(action, player, realtyCard, session);
                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                break;
            case SellRealty:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                buyAndSellPlayersAction(action, player, realtyCard, session);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                break;
            case LeavePrisonByCard:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                player.setPrisonOutCard(player.getPrisonOutCard() - 1);
                action = servicesManager.getPrisonService().waiting(action);
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                servicesManager.getCardsManagerService().comebackPrisonCard();
                break;
            case LeavePrisonByMoney:
                action = servicesManager.getPrisonService().waiting(action);
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                break;
            case MoneyOperation:
                List<PlayerDto> playersList = objectMapper.convertValue(action.getActionBody().get("playerList"), new TypeReference<>() {});

                action = playersList.size() != 1 ?
                        servicesManager.getBankService().playerToPlayerInteraction(action) :
                        servicesManager.getBankService().playerToBankInteraction(action);

                currentActions.remove(MoneyOperation.toString());
                player.setCredit(0L);

                playersList = objectMapper.convertValue(action.getActionBody().get("playerList"), new TypeReference<>() {});
                player = playersList.get(0);
                if (playersList.size() > 1) {
                    PlayerDto secondPlayer = playersList.get(1);
                    for (int i = 1; i < players.size(); i++) {
                        if (players.get(i).getPlayerFigure().equals(secondPlayer.getPlayerFigure())) {
                            players = new ArrayList<>(players.subList(0, i));
                            players.add(secondPlayer);
                            players.addAll(players.subList(i + 1, players.size()));
                            break;
                        }
                    }
                }
                resultBody.put("playerList", playersList);

                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                break;
            case Swap:
                //TODO проверить обновление игроков
                List<PlayerDto> offer1 = objectMapper.convertValue(action.getActionBody().get("offerOnPlayer1"), new TypeReference<>() {});
                List<PlayerDto> offer2 = objectMapper.convertValue(action.getActionBody().get("offerOnPlayer2"), new TypeReference<>() {});

                action = servicesManager.getRealtyManagerService().playerToPlayerInteraction(action);
                PlayerDto player1 = objectMapper.convertValue(action.getActionBody().get("player1"), PlayerDto.class);
                int size = player1.getRealtyList().size();
                swapPlayersAction(session, offer2, player1, size);
                PlayerDto player2 = objectMapper.convertValue(action.getActionBody().get("player2"), PlayerDto.class);
                size = player2.getRealtyList().size();
                swapPlayersAction(session, offer1, player2, size);

                player = player1;
                session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
                resultBody.putAll(new HashMap<>(Map.of(
                        "player1", player1,
                        "player2", player2
                )));

                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                break;
            case EndTurn:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                players.add(players.remove(0));
                resultBody.put("nextPlayer", players.get(0));

                players.get(0).setCurrentActions(List.of(DropDice.toString()));
                break;
            case GiveUp:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                for(RealtyCardDto realtyCardDto : player.getRealtyList()){
                    session.getRealty().remove(realtyCardDto);
                    realtyCardDto.setOwner(null);
                    session.getRealty().add(realtyCardDto);
                }
                session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
                players.remove(0);
                player.setCurrentActions(List.of(GiveUp.toString()));
        }
        checkCredit(player, currentActions, blockedActions);

        player.setCurrentActions(new ArrayList<>(currentActions));
        player.setBlockedActions(new ArrayList<>(blockedActions));

        this.updatePlayerInSession(player, session);

        session.setPlayers(players);
        resultBody.put("player", player);
        resultBody.put("realtyList", session.getRealty());
        sessionRepository.set(sessionToken, session);
        generateHistory(player, action, session);
        return ActionDto.builder()
                .actionType(action.getActionType())
                .actionBody(resultBody)
                .build();
    }

    private void checkCredit(PlayerDto player, Set<String> currentActions, Set<String> blockedActions) {
        if (blockedActions.contains(MoneyOperation.toString()) && player.getMoney() >= player.getCredit()) {
            if (player.getMoney() >= player.getCredit()) {
                currentActions.add(MoneyOperation.toString());
                currentActions.remove(MoneyOperation.toString());
            }
        }
        else {
            currentActions.addAll(blockedActions);
            blockedActions.clear();
        }

        if (currentActions.contains(MoneyOperation.toString()) || blockedActions.contains(MoneyOperation.toString())) {
            if (currentActions.contains(DropDice.toString())) {
                blockedActions.add(DropDice.toString());
                currentActions.remove(DropDice.toString());
            }
            else if (currentActions.contains(EndTurn.toString())) {
                blockedActions.add(EndTurn.toString());
                currentActions.remove(EndTurn.toString());
            }
        }
    }

    private void swapPlayersAction(SessionDto session, List<?> offer, PlayerDto player, int size) {
        for (RealtyCardDto realty : player.getRealtyList().subList(size - offer.size(), size)) {
            for (RealtyCardDto card1 : session.getRealty()) {
                if (card1.getPosition() == realty.getPosition()) {
                    session.getRealty().remove(card1);
                    session.getRealty().add(realty);
                }
            }
        }
    }

    private void buyAndSellPlayersAction(ActionDto action, PlayerDto player, RealtyCardDto card, SessionDto session){
        if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action)) {
            action = servicesManager.getRealtyManagerService().playerToBankInteraction(action);
            PlayerDto changesPlayer = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
            player.setMoney(changesPlayer.getMoney());
            player.setRealtyList(changesPlayer.getRealtyList());
            RealtyCardDto updateCard = player.getRealtyList()
                    .stream()
                    .filter(r -> r.getPosition() == card.getPosition())
                    .findFirst()
                    .orElse(null);
            updateRealtyInSession(updateCard, session);
        }
    }

    @Override
    public List<String> endGame(String sessionToken) {
        SessionDto session = sessionRepository.get(sessionToken);
        return session.getHistory();
    }

    private void generateHistory(PlayerDto player, ActionDto action, SessionDto session) {
        List<String> history = session.getHistory();
        switch (ActionType.valueOf(action.getActionType())) {
            case DropDice:
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и ему выпали кубики " + Arrays.toString(player.getLastRoll())
                );
                break;
            case BuyRealty:
                RealtyCardDto realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и купил имущество " + realtyCard.getCardName() +
                        "за " + realtyCard.getCostCard()
                );
                break;
            case BuyHouse:
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и купил дом за " + realtyCard.getCostHouse()
                );
                break;
            case SellHouse:
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и продал дом за " + realtyCard.getCostHouse()
                );
                break;
            case SellRealty:
                realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и продал имущество" + realtyCard.getCardName() +
                        " за " + realtyCard.getCostCard()
                );
                break;
            case LeavePrisonByCard:
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() + " и вышел из тюрьмы по карточке"
                );
                break;
            case LeavePrisonByMoney:
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и вышел из тюрьмы за деньги. Осталось денег: " + player.getMoney()
                );
                break;
            case MoneyOperation:
                List<PlayerDto> playersList = objectMapper.convertValue(action.getActionBody().get("playerList"), new TypeReference<>() {});
                if (playersList.size() == 1) {
                    history.add(
                            "Игрок " + player.getPlayerFigure() +
                            " выполнил действие " + action.getActionType() +
                            ". Осталось денег: " + player.getMoney()
                    );
                } else {
                    history.add(
                            "Игрок " + player.getPlayerFigure() +
                            " выполнил действие " + action.getActionType() +
                            " с игроком: " + playersList.get(1) +
                            ". У игрока " + player.getPlayerFigure() + " осталось денег: " + player.getMoney() +
                            ". У игрока " + playersList.get(1).getPlayerFigure() + " осталось денег: " + playersList.get(1).getMoney()
                    );
                }
                break;
            case Swap:
                PlayerDto player2 = objectMapper.convertValue(action.getActionBody().get("player2"), PlayerDto.class);
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() +
                        " и обменялся с игроком " + player2
                );
                break;
            case EndTurn:
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() + " и закончил ход"
                );
                break;
            case GiveUp:
                history.add(
                        "Игрок " + player.getPlayerFigure() +
                        " выполнил действие " + action.getActionType() + " и сдался. Долг: " + player.getCredit()
                );
                break;
        }
        session.setHistory(history);
    }

    private void generationPossibleActions(
            MapType mapType,
            PlayerDto player,
            List<PlayerDto> players,
            SessionDto session,
            Set<String> currentActions,
            Set<String> blockedActions
    ) {
        switch (mapType) {
            case REALTY_CELL:
                int position = player.getPosition();
                RealtyCardDto card = session.getRealty()
                        .stream()
                        .filter(realty -> realty.getPosition() == position)
                        .findFirst()
                        .orElse(null);

                if (card != null && card.getCountHouse() >= 0 && card.getOwner() != null) {
                    if (player.getCredit() != 0) {
                        if (player.getMoney() < player.getCredit()) {
                            long costAllRealty = 0;
                            for (RealtyCardDto realtyCard: player.getRealtyList()) {
                                costAllRealty += realtyCard.getCostCard();
                                costAllRealty += realtyCard.getCountHouse() * realtyCard.getCostHouse();
                            }
                            if (costAllRealty < player.getCredit()) {
                                player.setCredit(player.getCredit() + money);
                                currentActions.add(GiveUp.toString());
                            }
                            else {
                                currentActions.add(MoneyOperation.toString());
                            }
                        }
                        else {
                            currentActions.add(MoneyOperation.toString());
                        }
                    }
                }
                actionBuyRealty(card, player, currentActions, blockedActions);
                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                break;
            case PAY_CELL:
                long money = player.getPosition() == 4 ? income : luxury;
                ActionDto action = ActionDto.builder()
                        .actionType(MoneyOperation.toString())
                        .actionBody(new HashMap<>(Map.of(
                                "player", player,
                                "money", money
                        )))
                        .build();

                player.setCredit(player.getCredit() + money);
                if (servicesManager.getBankService().isPlayerToBankInteraction(action)) {
                    currentActions.add(MoneyOperation.toString());
                }
                else {
                    blockedActions.add(MoneyOperation.toString());
                }

                actionSellRealty(player, currentActions);
                actionSwap(players, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
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
                actionSellRealty(player, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                actionSwap(players, currentActions);
                break;
            case VISITING_PRISON_CELL:
                if (player.getInPrison() > 0) {
                    getOutOfPrison(player, currentActions);
                }
                actionSellRealty(player, currentActions);
                actionBuyHouse(player, currentActions);
                actionSellHouse(player, currentActions);
                actionSwap(players, currentActions);
                break;
            case TO_PRISON_CELL:
                player = servicesManager.getPrisonService().imprisonPlayer(player);
                player.setPosition(10);
                break;
        }
    }

    private void actionBuyRealty(RealtyCardDto card, PlayerDto player, Set<String> currentActions, Set<String> blockedActions) {
        ActionDto action = ActionDto.builder()
                .actionType(BuyRealty.toString())
                .actionBody(new HashMap<>(Map.of(
                        "player", player,
                        "realtyCard", card
                )))
                .build();
        ActionDto actionMoney = ActionDto.builder()
                .actionType(MoneyOperation.toString())
                .actionBody(new HashMap<>(Map.of(
                        "player", player,
                        "money", card.getCostCard()
                )))
                .build();

        if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action)){
            currentActions.add(BuyRealty.toString());
        }
        else if(!card.getOwner().equals(player.getPlayerFigure()) && servicesManager.getBankService().isPlayerToPlayerInteraction(actionMoney)){
            player.setCredit(player.getCredit() + card.getCostCard());
            currentActions.add(MoneyOperation.toString());
        }
        else {
            player.setCredit(player.getCredit() + card.getCostCard());
            blockedActions.add(MoneyOperation.toString());
        }
    }

    private void actionSellRealty(PlayerDto player, Set<String> currentActions){
        if (player.getRealtyList()
                .stream()
                .filter(realty -> realty.getCountHouse() == 0)
                .findFirst()
                .orElse(null) != null
        ) {
            currentActions.add(SellRealty.toString());
        }
    }

    private void actionBuyHouse(PlayerDto player, Set<String> currentActions) {
        if (player.getMonopolies().isEmpty()) return;
        currentActions.add(BuyHouse.toString());
    }

    private void actionSellHouse(PlayerDto player, Set<String> currentActions) {
        for (RealtyCardDto realtyCard: player.getRealtyList()) {
            if (realtyCard.getCountHouse() > 0) {
                currentActions.add(SellHouse.toString());
                break;
            }
        }
    }

    private void actionSwap(List<PlayerDto> players, Set<String> currentActions) {
        if (players.get(0).getRealtyList().isEmpty()) return;

        List<PlayerDto> playersBySwap = players.subList(1, players.size());
        for (PlayerDto player: playersBySwap) {
            if (!player.getRealtyList().isEmpty()) {
                currentActions.add(Swap.toString());
                return;
            }
        }
    }

    private void getOutOfPrison(PlayerDto player, Set<String> currentActions) {
        ActionDto action = ActionDto.builder()
                .actionType(LeavePrisonByCard.toString())
                .actionBody(new HashMap<>(Map.of(
                        "player", player
                )))
                .build();
        if (servicesManager.getPrisonService().isWaiting(action)) {
            currentActions.add(LeavePrisonByCard.toString());
        }

        action.setActionType(LeavePrisonByMoney.toString());
        if (servicesManager.getPrisonService().isWaiting(action)) {
            currentActions.add(LeavePrisonByMoney.toString());
        }
    }

    private void updateRealtyInSession(RealtyCardDto realtyCard, SessionDto session){
        for(RealtyCardDto nowRealtyCard : session.getRealty()){
            if(nowRealtyCard.getPosition() == realtyCard.getPosition()){
                session.getRealty().remove(nowRealtyCard);
                break;
            }
        }
        session.getRealty().add(realtyCard);
        session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
    }

    private void updatePlayerInSession(PlayerDto player, SessionDto session){
        int position = -1;
        for(PlayerDto nowPlayer: session.getPlayers()){
            if(nowPlayer.getPlayerFigure().equals(player.getPlayerFigure())){
                position = session.getPlayers().indexOf(nowPlayer);
                session.getPlayers().remove(nowPlayer);
                break;
            }
        }
        session.getPlayers().add(position, player);
    }
}
