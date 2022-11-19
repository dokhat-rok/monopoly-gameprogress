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

    /*public ProgressServiceImpl(
            SessionRepository sessionRepository,
            ServicesUtils servicesUtils
    ){
      this.sessionRepository = sessionRepository;
      this.objectMapper = new ObjectMapper();
      this.servicesManager = ServicesUtils.INSTANCE;
    }*/

    @Value("${progress.start.player.money}")
    private Long money;

    @Value("${progress.round.salary}")
    private Long salary;

    @Value("${progress.utility.name}")
    private String utility;

    @Value("${progress.station.name}")
    private String station;

    @Override
    public StartDataDto startGame(Long count, String[] players) {
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
        Set<String> resultActions = new HashSet<>();
        List<PlayerDto> players = session.getPlayers();
        Map<String, Object> resultBody = new HashMap<>();
        PlayerDto player = null;

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
                            .actionType(ActionType.MoneyOperation.toString())
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
                }
                else{
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
                generationPossibleActions(mapType, player, players, resultActions, session);
                resultActions.add(EndTurn.toString());
                break;
            case EndTurn:
                players.add(players.remove(0));
                resultBody.put("nextPlayer", players.get(0));
                resultActions.add(DropDice.toString());
                break;
            case BuyRealty:
                RealtyCardDto card;
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                if(servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action)){
                    card = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                    action = servicesManager.getRealtyManagerService().playerToBankInteraction(action);
                    player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                    session.getRealty().remove(card);
                    card = player.getRealtyList().get(player.getRealtyList().size() - 1);
                    session.getRealty().add(card);
                    session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
                }
                actionSellRealty(resultActions, player);
                actionSwap(resultActions, players);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.toString());
                break;
            case BuyHouse:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                card = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                buyAndSellPlayersAction(action, player, card, session);
                actionSellRealty(resultActions, player);
                actionSwap(resultActions, players);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.toString());
                break;
            case LeavePrisonByCard:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                player.setPrisonOutCard(player.getPrisonOutCard() - 1);
                action = servicesManager.getPrisonService().waiting(action);
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                servicesManager.getCardsManagerService().comebackPrisonCard();
                resultActions.add(EndTurn.toString());
                break;
            case LeavePrisonByMoney:
                action = servicesManager.getPrisonService().waiting(action);
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                resultActions.add(EndTurn.toString());
                break;
            case SellHouse:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                card = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                buyAndSellPlayersAction(action, player, card, session);
                actionSellRealty(resultActions, player);
                actionSwap(resultActions, players);
                actionBuyHouse(resultActions, player);
                resultActions.add(EndTurn.toString());
                break;
            case SellRealty:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                card = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                buyAndSellPlayersAction(action, player, card, session);
                actionSwap(resultActions, players);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.toString());
                break;
            case MoneyOperation:
                List<PlayerDto> playersList = objectMapper.convertValue(action.getActionBody().get("playerList"), new TypeReference<>() {});
                if (playersList.size() == 1) {
                    action = servicesManager.getBankService().playerToBankInteraction(action);
                }
                else{
                    action = servicesManager.getBankService().playerToPlayerInteraction(action);
                }

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

                actionSellRealty(resultActions, player);
                actionSwap(resultActions, players);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.toString());
                break;
            case Swap:
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

                actionSellRealty(resultActions, player);
                actionSwap(resultActions, players);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(ActionType.EndTurn.toString());
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
                resultActions.add(DropDice.toString());
        }

        session.setPlayers(players);
        resultBody.put("player", player);
        resultBody.put("resultActions", List.of(resultActions));
        resultBody.put("realtyList", session.getRealty());
        sessionRepository.set(sessionToken, session);
        return ActionDto.builder()
                .actionType(action.getActionType())
                .actionBody(resultBody)
                .build();
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
            for(RealtyCardDto realtyCardDto : player.getRealtyList()){
                if(realtyCardDto.getPosition() == card.getPosition()){
                    session.getRealty().remove(card);
                    card = realtyCardDto;
                    session.getRealty().add(card);
                    session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
                    break;
                }
            }
        }
    }

    @Override
    public List<String> endGame() {
        return null;
    }

    private void generationPossibleActions(
            MapType mapType,
            PlayerDto player,
            List<PlayerDto> players,
            Set<String> resultAction,
            SessionDto session
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
                    //TODO проверка на банкрота
                    resultAction.add(MoneyOperation.toString());
                }
                actionBuyRealty(card, resultAction, player);
                actionSellRealty(resultAction, player);
                actionSwap(resultAction, players);
                actionBuyHouse(resultAction, player);
                actionSellHouse(resultAction, player);
                break;
            case PAY_CELL:
                resultAction.add(MoneyOperation.toString());
                actionSellRealty(resultAction, player);
                actionSwap(resultAction, players);
                actionBuyHouse(resultAction, player);
                actionSellHouse(resultAction, player);
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
                actionSellRealty(resultAction, player);
                actionBuyHouse(resultAction, player);
                actionSellHouse(resultAction, player);
                actionSwap(resultAction, players);
                break;
            case VISITING_PRISON_CELL:
                if (player.getInPrison() > 0) {
                    getOutOfPrison(resultAction, player);
                }
                actionSellRealty(resultAction, player);
                actionBuyHouse(resultAction, player);
                actionSellHouse(resultAction, player);
                actionSwap(resultAction, players);
                break;
            case TO_PRISON_CELL:
                player = servicesManager.getPrisonService().imprisonPlayer(player);
                player.setPosition(10);
                break;
        }
    }

    private void actionBuyRealty(RealtyCardDto card, Set<String> resultAction, PlayerDto player) {
        ActionDto action = ActionDto.builder()
                .actionType(BuyRealty.toString())
                .actionBody(new HashMap<>(Map.of(
                        "player", player,
                        "realtyCard", card
                )))
                .build();

        if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action)){
            resultAction.add(BuyRealty.toString());
        }
        else if(!card.getOwner().equals(player.getPlayerFigure())){
            resultAction.add(MoneyOperation.toString());
        }
    }

    private void actionSellRealty(Set<String> resultAction, PlayerDto player){
        if (player.getRealtyList()
                .stream()
                .filter(realty -> realty.getCountHouse() == 0)
                .findFirst()
                .orElse(null) != null
        ) {
            resultAction.add(ActionType.SellRealty.toString());
        }
    }

    private void actionBuyHouse(Set<String> resultAction, PlayerDto player) {
        if (player.getMonopolies().isEmpty()) return;
        resultAction.add(BuyHouse.toString());
    }

    private void actionSellHouse(Set<String> resultAction, PlayerDto player) {
        for (RealtyCardDto realtyCard: player.getRealtyList()) {
            if (realtyCard.getCountHouse() > 0) {
                resultAction.add(SellHouse.toString());
                break;
            }
        }
    }

    private void actionSwap(Set<String> resultAction, List<PlayerDto> players) {
        if (players.get(0).getRealtyList().isEmpty()) return;

        List<PlayerDto> playersBySwap = players.subList(1, players.size());
        for (PlayerDto player: playersBySwap) {
            if (!player.getRealtyList().isEmpty()) {
                resultAction.add(Swap.toString());
                return;
            }
        }
    }

    private void getOutOfPrison(Set<String> resultAction, PlayerDto player) {
        ActionDto action = ActionDto.builder()
                .actionType(LeavePrisonByCard.toString())
                .actionBody(new HashMap<>(Map.of(
                        "player", player
                )))
                .build();
        if(servicesManager.getPrisonService().isWaiting(action)) resultAction.add(LeavePrisonByCard.toString());

        action.setActionType(LeavePrisonByMoney.toString());
        if(servicesManager.getPrisonService().isWaiting(action)) resultAction.add(LeavePrisonByMoney.toString());
    }
}
