package com.vpr.monopoly.gameprogress.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpr.monopoly.gameprogress.data.MonopolyMap;
import com.vpr.monopoly.gameprogress.data.enam.MapType;
import com.vpr.monopoly.gameprogress.model.*;
import com.vpr.monopoly.gameprogress.model.enam.ActionType;
import com.vpr.monopoly.gameprogress.repository.SessionRepository;
import com.vpr.monopoly.gameprogress.service.ProgressService;
import com.vpr.monopoly.gameprogress.service.ServicesManager;
import com.vpr.monopoly.gameprogress.utils.ServicesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.*;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final SessionRepository sessionRepository;

    private final ServicesManager servicesManager;

    private final ObjectMapper objectMapper;

    public ProgressServiceImpl(
            SessionRepository sessionRepository,
            ServicesUtils servicesUtils
    ){
      this.sessionRepository = sessionRepository;
      this.objectMapper = new ObjectMapper();
      this.servicesManager = ServicesUtils.INSTANCE;
    }

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
        List<String> resultActions = new ArrayList<>();
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
                            .actionBody(Map.of(
                                    "playerList", List.of(player),
                                    "money", salary
                            ))
                            .build();
                    bankAction = servicesManager.getBankService().playerToBankInteraction(bankAction);

                    List<?> playersList = objectMapper.convertValue(bankAction.getActionBody().get("playerList"), List.class);
                    player = (PlayerDto) playersList.get(0);
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
                            .actionBody(Map.of(
                                    "player", player
                            ))
                            .build();
                    prisonAction = servicesManager.getPrisonService().waiting(prisonAction);
                    player = (PlayerDto) prisonAction.getActionBody().get("player");
                }

                MapType mapType = MonopolyMap.getTypeByCellNumber(player.getPosition());
                generationPossibleActions(mapType, player, players, resultActions, session);
                break;
            case EndTurn:
                players.add(players.remove(0));
                resultActions.add(DropDice.name());
                break;
            case BuyRealty:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                RealtyCardDto card = objectMapper
                        .convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);

                List<RealtyCardDto> updateList = player.getRealtyList();
                updateList.add(card);
                player.setRealtyList(updateList);
                for (RealtyCardDto realty : new HashSet<>(player.getRealtyList())) {
                    if (!realty.getColor().equals(utility) && !realty.getColor().equals(station)) {
                        long countColor = player.getRealtyList()
                                .stream()
                                .filter(color -> color.getColor().equals(realty.getColor()))
                                .count();
                        if (countColor == session.getRealtyColors().get(realty.getColor())) {
                            List<String> monopolies = player.getMonopolies();
                            monopolies.add(realty.getColor());
                            player.setMonopolies(monopolies);
                        }
                    }
                }

                action = servicesManager.getRealtyManagerService().playerToBankInteraction(action);
                player = (PlayerDto) action.getActionBody().get("player");
                session.getRealty().remove(card);
                card = player.getRealtyList().get(player.getRealtyList().size() - 1);
                session.getRealty().add(card);
                session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));

                actionSellRealty(resultActions, player);
                actionSwap(players, resultActions);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.name());
                break;
            case BuyHouse:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                card = objectMapper
                        .convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action)) {
                    updateList = player.getRealtyList();
                    updateList.add(card);
                    player.setRealtyList(updateList);

                    action = servicesManager.getRealtyManagerService().playerToBankInteraction(action);
                    player = (PlayerDto) action.getActionBody().get("player");
                    session.getRealty().remove(card);
                    card = player.getRealtyList().get(player.getRealtyList().size() - 1);
                    session.getRealty().add(card);
                    session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
                }
                actionSellRealty(resultActions, player);
                actionSwap(players, resultActions);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.name());
                break;
            case LeavePrisonByCard:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                player.setPrisonOutCard(player.getPrisonOutCard() - 1);
                action = servicesManager.getPrisonService().waiting(action);
                player = (PlayerDto) action.getActionBody().get("player");
                servicesManager.getCardsManagerService().comebackPrisonCard();
                resultActions.add(EndTurn.name());
                break;
            case LeavePrisonByMoney:
                action = servicesManager.getPrisonService().waiting(action);
                player = (PlayerDto) action.getActionBody().get("player");
                resultActions.add(EndTurn.name());
                break;
            case SellHouse:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                card = objectMapper
                        .convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
                player.getRealtyList().remove(card);

                action = servicesManager.getRealtyManagerService().playerToBankInteraction(action);
                player = (PlayerDto) action.getActionBody().get("player");
                session.getRealty().remove(card);
                card.setOwner("");
                session.getRealty().add(card);
                session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));

                actionSellRealty(resultActions, player);
                actionSwap(players, resultActions);
                actionBuyHouse(resultActions, player);
                resultActions.add(EndTurn.name());
                break;
            case SellRealty:
                player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
                servicesManager.getRealtyManagerService().playerToBankInteraction(action);

                actionSwap(players, resultActions);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.name());
                break;
            case MoneyOperation:
                List<?> playersList = objectMapper.convertValue(action.getActionBody().get("playerList"), List.class);
                if (playersList.size() == 1) {
                    action = servicesManager.getBankService().playerToBankInteraction(action);
                }
                else{
                    action = servicesManager.getBankService().playerToPlayerInteraction(action);
                }

                playersList = objectMapper.convertValue(action.getActionBody().get("playerList"), List.class);
                player = (PlayerDto) playersList.get(0);
                if (playersList.size() > 1) {
                    PlayerDto secondPlayer = (PlayerDto) playersList.get(1);
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
                actionSwap(players, resultActions);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(EndTurn.name());
                break;
            case Swap:
                List<?> offer1 = objectMapper.convertValue(action.getActionBody().get("offerOnPlayer1"), List.class);
                List<?> offer2 = objectMapper.convertValue(action.getActionBody().get("offerOnPlayer2"), List.class);

                action = servicesManager.getRealtyManagerService().playerToPlayerInteraction(action);
                PlayerDto player1 = (PlayerDto) action.getActionBody().get("player1");
                int size = player1.getRealtyList().size();
                swapPlayersAction(session, offer2, player1, size);
                PlayerDto player2 = (PlayerDto) action.getActionBody().get("player2");
                size = player2.getRealtyList().size();
                swapPlayersAction(session, offer1, player2, size);

                player = player1;
                session.getRealty().sort(Comparator.comparing(RealtyCardDto::getPosition));
                resultBody.putAll(Map.of(
                        "player1", player1,
                        "player2", player2
                ));

                actionSellRealty(resultActions, player);
                actionSwap(players, resultActions);
                actionBuyHouse(resultActions, player);
                actionSellHouse(resultActions, player);
                resultActions.add(ActionType.EndTurn.name());
                break;
        }

        players.remove(0);
        players.add(0, player);
        session.setPlayers(players);
        resultBody.put("player", player);

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
                int position = player.getPosition();
                RealtyCardDto card = session.getRealty()
                        .stream()
                        .filter(realty -> realty.getPosition() == position)
                        .findFirst()
                        .orElse(null);
                //TODO когда заплатить аренду
                if (card != null && card.getCountHouse() > 0L) {
                    resultAction.add(MoneyOperation.name());
                }
                actionBuyRealty(card, resultAction, player);
                actionSellRealty(resultAction, player);
                actionSwap(players, resultAction);
                actionBuyHouse(resultAction, player);
                actionSellHouse(resultAction, player);
                break;
            case PAY_CELL:
                resultAction.add(MoneyOperation.name());
                actionSellRealty(resultAction, player);
                actionSwap(players, resultAction);
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
                actionSwap(players, resultAction);
                break;
            case VISITING_PRISON_CELL:
                if (player.getInPrison() > 0) {
                    getOutOfPrison(player, resultAction);
                }
                actionSellRealty(resultAction, player);
                actionBuyHouse(resultAction, player);
                actionSellHouse(resultAction, player);
                actionSwap(players, resultAction);
                break;
            case TO_PRISON_CELL:
                player = servicesManager.getPrisonService().imprisonPlayer(player);
                player.setPosition(10);
                break;
        }
    }

    private void actionBuyRealty(RealtyCardDto card, List<String> resultAction, PlayerDto player) {
        ActionDto action = ActionDto.builder()
                .actionType(BuyRealty.name())
                .actionBody(Map.of(
                        "player", player,
                        "realtyCard", card
                ))
                .build();

        if (servicesManager.getRealtyManagerService().isPlayerToBankInteraction(action)){
            resultAction.add(BuyRealty.name());
        }
        else if(!card.getOwner().equals(player.getPlayerFigure())){
            resultAction.add(MoneyOperation.name());
        }
    }

    private void actionSellRealty(List<String> resultAction, PlayerDto player){
        if (player.getRealtyList()
                .stream()
                .filter(realty -> realty.getCountHouse() == 0)
                .findFirst()
                .orElse(null) != null
        ) {
            resultAction.add(ActionType.SellRealty.name());
        }
    }

    private void actionBuyHouse(List<String> resultAction, PlayerDto player) {
        if (player.getMonopolies() == null) return;

        resultAction.add(BuyHouse.name());
    }

    private void actionSellHouse(List<String> resultAction, PlayerDto player) {
        List<RealtyCardDto> realty = player.getRealtyList();
        for (RealtyCardDto realtyCard: realty) {
            if (realtyCard.getCountHouse() > 0) {
                resultAction.add(SellHouse.name());
                break;
            }
        }
    }

    private void actionSwap(List<PlayerDto> players, List<String> resultAction) {
        if (players.get(0).getRealtyList().isEmpty()) return;

        List<PlayerDto> playersBySwap = players.subList(1, players.size());
        for (PlayerDto player: playersBySwap) {
            if (!player.getRealtyList().isEmpty()) {
                resultAction.add(Swap.name());
                return;
            }
        }
    }

    private void getOutOfPrison(PlayerDto player, List<String> resultAction) {
        ActionDto action = ActionDto.builder()
                .actionType(LeavePrisonByCard.toString())
                .actionBody(Map.of(
                        "player", player
                ))
                .build();
        if(servicesManager.getPrisonService().isWaiting(action)) resultAction.add(LeavePrisonByCard.toString());

        action.setActionType(LeavePrisonByMoney.name());
        if(servicesManager.getPrisonService().isWaiting(action)) resultAction.add(LeavePrisonByMoney.toString());
    }
}
