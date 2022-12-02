package com.vpr.monopoly.gameprogress.service.monopoly.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpr.monopoly.gameprogress.data.MonopolyMap;
import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;
import com.vpr.monopoly.gameprogress.model.enam.ActionType;
import com.vpr.monopoly.gameprogress.service.ServicesManager;
import com.vpr.monopoly.gameprogress.service.monopoly.RealtyManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.MoneyOperation;
import static com.vpr.monopoly.gameprogress.model.enam.ActionType.Swap;
import static com.vpr.monopoly.gameprogress.model.enam.ServiceType.REALTY_MANAGER;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtyManagerServiceImpl implements RealtyManagerService {

    private final ObjectMapper objectMapper;

    private ServicesManager servicesManager;

    @Override
    public ActionDto playerToBankInteraction(ActionDto action) {
        log.info("Requesting... to {}", REALTY_MANAGER.getName());
        PlayerDto player = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
        RealtyCardDto realtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
        switch (ActionType.valueOf(action.getActionType())){
            case BuyRealty:
                Long money = realtyCard.getCostCard();
                if(player.getPlayerFigure().equals(realtyCard.getOwner())){
                    money /= 2;
                }
                else if(realtyCard.getOwner() != null){
                    return action;
                }
                ActionDto bankAction = servicesManager.getBankService().playerToBankInteraction(
                        ActionDto.builder()
                                .actionType(MoneyOperation.toString())
                                .actionBody(new HashMap<>(Map.of(
                                        "playerList", List.of(player),
                                        "money", -money
                                )))
                                .build()
                );
                List<PlayerDto> playerList = objectMapper.convertValue(bankAction.getActionBody().get("playerList"), new TypeReference<>() {});
                player = playerList.get(0);
                realtyCard.setOwner(player.getPlayerFigure());
                realtyCard.setCountHouse(0L);
                if(money.longValue() != realtyCard.getCostCard().longValue()){
                    player.getRealtyList().remove(player.getRealtyList().stream()
                            .filter(r -> r.getPosition() == realtyCard.getPosition())
                            .findFirst()
                            .orElse(null));
                }
                player.getRealtyList().add(realtyCard);
                break;
            case SellRealty:
                if(!player.getPlayerFigure().equals(realtyCard.getOwner())){
                    return action;
                }
                bankAction = servicesManager.getBankService().playerToBankInteraction(
                        ActionDto.builder()
                                .actionType(MoneyOperation.toString())
                                .actionBody(new HashMap<>(Map.of(
                                        "playerList", List.of(player),
                                        "money", realtyCard.getCostCard() / 2
                                )))
                                .build()
                );
                playerList = objectMapper.convertValue(bankAction.getActionBody().get("playerList"), new TypeReference<>() {});
                player = playerList.get(0);
                realtyCard.setCountHouse(-1L);
                player.getRealtyList().remove(player.getRealtyList().stream()
                        .filter(r -> r.getPosition() == realtyCard.getPosition())
                        .findFirst()
                        .orElse(null));
                player.getRealtyList().add(realtyCard);
                break;
            case BuyHouse:
                if(!player.getMonopolies().contains(realtyCard.getColor()) || realtyCard.getCountHouse() >= 5){
                    return action;
                }
                for(RealtyCardDto realty : player.getRealtyList()){
                    if(realty.getPosition() == realtyCard.getPosition()){
                        continue;
                    }
                    if(Math.abs(realty.getCountHouse() - realtyCard.getCountHouse()) > 1 &&
                            realty.getColor().equals(realtyCard.getColor())){
                        return action;
                    }
                }
                bankAction = servicesManager.getBankService().playerToBankInteraction(
                        ActionDto.builder()
                                .actionType(MoneyOperation.toString())
                                .actionBody(new HashMap<>(Map.of(
                                        "playerList", List.of(player),
                                        "money", -realtyCard.getCostHouse()
                                )))
                                .build()
                );
                playerList = objectMapper.convertValue(bankAction.getActionBody().get("playerList"), new TypeReference<>() {});
                player = playerList.get(0);
                player.getRealtyList().remove(realtyCard);
                realtyCard.setCountHouse(realtyCard.getCountHouse() + 1);
                player.getRealtyList().add(realtyCard);
            case SellHouse:
                if(realtyCard.getCountHouse() <= 0){
                    return action;
                }
                bankAction = servicesManager.getBankService().playerToBankInteraction(
                        ActionDto.builder()
                                .actionType(MoneyOperation.toString())
                                .actionBody(new HashMap<>(Map.of(
                                        "playerList", List.of(player),
                                        "money", realtyCard.getCostHouse() / 2
                                )))
                                .build()
                );
                playerList = objectMapper.convertValue(bankAction.getActionBody().get("playerList"), new TypeReference<>() {});
                player = playerList.get(0);
                player.getRealtyList().remove(realtyCard);
                realtyCard.setCountHouse(realtyCard.getCountHouse() - 1);
                player.getRealtyList().add(realtyCard);
                break;
        }
        this.checkMonopolies(player);
        ActionDto result = ActionDto.builder()
                .actionType(action.getActionType())
                .actionBody(new HashMap<>(Map.of(
                        "player", player,
                        "realtyCard", realtyCard
                )))
                .build();
        log.info("Response {} ==> {}", REALTY_MANAGER.getName(), result);
        return result;
    }

    @Override
    public Boolean isPlayerToBankInteraction(ActionDto action) {
        log.info("Requesting... to {}", REALTY_MANAGER.getName());
        RealtyCardDto oldRealtyCard = objectMapper.convertValue(action.getActionBody().get("realtyCard"), RealtyCardDto.class);
        action = this.playerToBankInteraction(action);
        PlayerDto changesPlayer = objectMapper.convertValue(action.getActionBody().get("player"), PlayerDto.class);
        RealtyCardDto realtyCard = changesPlayer.getRealtyList().stream()
                .filter(r -> r.getPosition() == oldRealtyCard.getPosition())
                .findFirst()
                .orElse(null);
        Boolean result = changesPlayer.getRealtyList().remove(realtyCard);
        log.info("Response {} ==> {}", REALTY_MANAGER.getName(), result);
        return result;
    }

    @Override
    public ActionDto playerToPlayerInteraction(ActionDto action) {
        log.info("Requesting... to {}", REALTY_MANAGER.getName());
        if(!action.getActionType().equals(Swap.toString())){
            return action;
        }
        PlayerDto player1 = objectMapper.convertValue(action.getActionBody().get("player1"), PlayerDto.class);
        PlayerDto player2 = objectMapper.convertValue(action.getActionBody().get("player2"), PlayerDto.class);
        List<RealtyCardDto> offer1 = objectMapper.convertValue(action.getActionBody().get("offerOfPlayer1"), new TypeReference<>() {});
        List<RealtyCardDto> offer2 = objectMapper.convertValue(action.getActionBody().get("offerOfPlayer2"), new TypeReference<>() {});
        BigInteger money = new BigInteger(action.getActionBody().get("money").toString());
        for(RealtyCardDto realtyCard : offer1){
            player1.getRealtyList().remove(realtyCard);
            player2.getRealtyList().add(realtyCard);
        }
        for(RealtyCardDto realtyCard : offer2){
            player2.getRealtyList().remove(realtyCard);
            player1.getRealtyList().add(realtyCard);
        }
        ActionDto bankAction = servicesManager.getBankService().playerToPlayerInteraction(ActionDto.builder()
                .actionType(MoneyOperation.toString())
                .actionBody(new HashMap<>(Map.of(
                        "playerList", List.of(player1, player2),
                        "money", money.longValue()
                )))
                .build()
        );
        List<PlayerDto> players = objectMapper
                .convertValue(bankAction.getActionBody().get("playerList"), new TypeReference<>(){});
        ActionDto result = ActionDto.builder()
                .actionType(Swap.toString())
                .actionBody(new HashMap<>(Map.of(
                        "player1", players.get(0),
                        "player2", players.get(1)
                )))
                .build();
        log.info("Response {} ==> {}", REALTY_MANAGER.getName(), result);
        return result;
    }

    @Override
    public Boolean isPlayerToPlayerInteraction(ActionDto action) {
        log.info("Requesting... to {}", REALTY_MANAGER.getName());
        PlayerDto player1 = objectMapper.convertValue(action.getActionBody().get("player1"), PlayerDto.class);
        PlayerDto player2 = objectMapper.convertValue(action.getActionBody().get("player2"), PlayerDto.class);
        List<RealtyCardDto> offer1 = objectMapper.convertValue(action.getActionBody().get("offerOfPlayer1"), new TypeReference<>() {});
        List<RealtyCardDto> offer2 = objectMapper.convertValue(action.getActionBody().get("offerOfPlayer2"), new TypeReference<>() {});
        Long money = (Long) action.getActionBody().get("money");

        action = this.playerToPlayerInteraction(action);

        PlayerDto changesPlayer1 = objectMapper.convertValue(action.getActionBody().get("player1"), PlayerDto.class);
        PlayerDto changesPlayer2 = objectMapper.convertValue(action.getActionBody().get("player2"), PlayerDto.class);

        if(!action.getActionType().equals(Swap.toString())){
            return false;
        }

        Boolean result = (Math.abs(player1.getRealtyList().size() - changesPlayer1.getRealtyList().size()) == offer2.size() &&
                Math.abs(player2.getRealtyList().size() - changesPlayer2.getRealtyList().size()) == offer1.size() ||
                offer1.size() == offer2.size()) &&
                player1.getMoney() - money == changesPlayer1.getMoney() &&
                player2.getMoney() + money == changesPlayer2.getMoney();
        log.info("Response {} ==> {}", REALTY_MANAGER.getName(), result);
        return result;
    }

    @Override
    public List<RealtyCardDto> getAllRealtyCards() {
        log.info("Requesting... to {}", REALTY_MANAGER.getName());
        List<RealtyCardDto> realtyCardsList =  new ArrayList<>();
        realtyCardsList.add(RealtyCardDto.builder()
                .position(1)
                .cardName("Старая Дорога")
                .priceMap(Map.of(
                        -1L, 0L,
                        0L, 2L,
                        1L, 10L,
                        2L, 30L,
                        3L, 90L,
                        4L, 160L,
                        5L, 250L)
                )
                .costCard(60L)
                .costHouse(50L)
                .countHouse(0L)
                .color("yellow")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(3)
                .cardName("Главное шоссе")
                .priceMap(Map.of(
                        -1L, 0L,
                        0L, 4L,
                        1L, 20L,
                        2L, 60L,
                        3L, 180L,
                        4L, 320L,
                        5L, 450L)
                )
                .costCard(60L)
                .costHouse(50L)
                .countHouse(0L)
                .color("yellow")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(5)
                .cardName("Восточный морской порт")
                .priceMap(Map.of(
                        -1L, 0L,
                        1L, 25L,
                        2L, 50L,
                        3L, 100L,
                        4L, 200L)
                )
                .costCard(200L)
                .costHouse(0L)
                .countHouse(0L)
                .color("port")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(6)
                .cardName("Аквапарк")
                .priceMap(Map.of(
                        -1L, 0L,
                        0L, 6L,
                        1L, 30L,
                        2L, 90L,
                        3L, 270L,
                        4L, 400L,
                        5L, 550L)
                )
                .costCard(100L)
                .costHouse(100L)
                .countHouse(0L)
                .color("orange")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(8)
                .cardName("Городской парк")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 6L,
                    1L, 30L,
                    2L, 90L,
                    3L, 270L,
                    4L, 400L,
                    5L, 550L)
                )
                .costCard(100L)
                .costHouse(100L)
                .countHouse(0L)
                .color("orange")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(9)
                .cardName("Горнолыжный курорт")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 8L,
                    1L, 40L,
                    2L, 100L,
                    3L, 300L,
                    4L, 450L,
                    5L, 600L)
                )
                .costCard(120L)
                .costHouse(100L)
                .countHouse(0L)
                .color("orange")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(11)
                .cardName("Спальный район")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 10L,
                    1L, 50L,
                    2L, 150L,
                    3L, 450L,
                    4L, 625L,
                    5L, 750L)
                )
                .costCard(140L)
                .costHouse(100L)
                .countHouse(0L)
                .color("green")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(12)
                .cardName("Электрическая компания")
                .priceMap(Map.of(
                    -1L, 0L,
                    1L, 4L,
                    2L, 10L)
                )
                .costCard(150L)
                .costHouse(0L)
                .countHouse(0L)
                .color("utilities")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(13)
                .cardName("Деловой квартал")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 10L,
                    1L, 50L,
                    2L, 150L,
                    3L, 450L,
                    4L, 625L,
                    5L, 750L)
                )
                .costCard(140L)
                .costHouse(100L)
                .countHouse(0L)
                .color("green")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(14)
                .cardName("Торговая площадь")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 12L,
                    1L, 60L,
                    2L, 180L,
                    3L, 500L,
                    4L, 700L,
                    5L, 900L)
                )
                .costCard(160L)
                .costHouse(100L)
                .countHouse(0L)
                .color("green")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(15)
                .cardName("Южный морской порт")
                .priceMap(Map.of(
                        -1L, 0L,
                        1L, 25L,
                        2L, 50L,
                        3L, 100L,
                        4L, 200L)
                )
                .costCard(200L)
                .costHouse(0L)
                .countHouse(0L)
                .color("port")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(16)
                .cardName("Улица Пушкина")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 14L,
                    1L, 70L,
                    2L, 200L,
                    3L, 550L,
                    4L, 750L,
                    5L, 950L)
                )

                .costCard(180L)
                .costHouse(100L)
                .countHouse(0L)
                .color("lightgreen")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(18)
                .cardName("Проспект Мира")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 14L,
                    1L, 70L,
                    2L, 200L,
                    3L, 550L,
                    4L, 750L,
                    5L, 950L)
                )
                .costCard(180L)
                .costHouse(100L)
                .countHouse(0L)
                .color("lightgreen")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(19)
                .cardName("Проспект Победы")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 16L,
                    1L, 80L,
                    2L, 220L,
                    3L, 600L,
                    4L, 800L,
                    5L, 1000L)
                )
                .costCard(200L)
                .costHouse(100L)
                .countHouse(0L)
                .color("lightgreen")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(21)
                .cardName("Бар")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 18L,
                    1L, 90L,
                    2L, 250L,
                    3L, 700L,
                    4L, 875L,
                    5L, 1050L)
                )
                .costCard(220L)
                .costHouse(150L)
                .countHouse(0L)
                .color("red")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(23)
                .cardName("Ночной клуб")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 18L,
                    1L, 90L,
                    2L, 250L,
                    3L, 700L,
                    4L, 875L,
                    5L, 1050L)
                )
                .costCard(220L)
                .costHouse(150L)
                .countHouse(0L)
                .color("red")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(24)
                .cardName("Ресторан")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 20L,
                    1L, 100L,
                    2L, 300L,
                    3L, 750L,
                    4L, 925L,
                    5L, 1100L)
                )
                .costCard(240L)
                .costHouse(150L)
                .countHouse(0L)
                .color("red")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(25)
                .cardName("Северный морской порт")
                .priceMap(Map.of(
                        -1L, 0L,
                        1L, 25L,
                        2L, 50L,
                        3L, 100L,
                        4L, 200L)
                )
                .costCard(200L)
                .costHouse(0L)
                .countHouse(0L)
                .color("port")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(26)
                .cardName("Компьютеры")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 22L,
                    1L, 110L,
                    2L, 330L,
                    3L, 800L,
                    4L, 975L,
                    5L, 1150L)
                )
                .costCard(260L)
                .costHouse(150L)
                .countHouse(0L)
                .color("pink")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(27)
                .cardName("Интернет")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 22L,
                    1L, 110L,
                    2L, 330L,
                    3L, 800L,
                    4L, 975L,
                    5L, 1150L)
                )
                .costCard(260L)
                .costHouse(150L)
                .countHouse(0L)
                .color("pink")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(28)
                .cardName("Водопроводная компания")
                .priceMap(Map.of(
                    -1L, 0L,
                    1L, 4L,
                    2L, 10L)
                )
                .costCard(150L)
                .costHouse(0L)
                .countHouse(0L)
                .color("utilities")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(29)
                .cardName("Сотовая связь")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 24L,
                    1L, 120L,
                    2L, 360L,
                    3L, 850L,
                    4L, 1025L,
                    5L, 1200L)
                )
                .costCard(280L)
                .costHouse(150L)
                .countHouse(0L)
                .color("pink")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(31)
                .cardName("Морские перевозки")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 26L,
                    1L, 130L,
                    2L, 390L,
                    3L, 900L,
                    4L, 1100L,
                    5L, 1275L)
                )
                .costCard(300L)
                .costHouse(200L)
                .countHouse(0L)
                .color("blue")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(32)
                .cardName("Железная дорога")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 26L,
                    1L, 130L,
                    2L, 390L,
                    3L, 900L,
                    4L, 1100L,
                    5L, 1275L)
                )
                .costCard(300L)
                .costHouse(200L)
                .countHouse(0L)
                .color("blue")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(34)
                .cardName("Авиакомпания")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 28L,
                    1L, 150L,
                    2L, 450L,
                    3L, 1000L,
                    4L, 1200L,
                    5L, 1400L)
                )
                .costCard(320L)
                .costHouse(200L)
                .countHouse(0L)
                .color("blue")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(35)
                .cardName("Западный морской порт")
                .priceMap(Map.of(
                        -1L, 0L,
                        1L, 25L,
                        2L, 50L,
                        3L, 100L,
                        4L, 200L)
                )
                .costCard(200L)
                .costHouse(0L)
                .countHouse(0L)
                .color("port")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(37)
                .cardName("Курортная зона")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 35L,
                    1L, 175L,
                    2L, 500L,
                    3L, 1100L,
                    4L, 1300L,
                    5L, 1500L)
                )
                .costCard(350L)
                .costHouse(200L)
                .countHouse(0L)
                .color("lightblue")
                .build()
        );
        realtyCardsList.add(RealtyCardDto.builder()
                .position(39)
                .cardName("Гостиничный комплекс")
                .priceMap(Map.of(
                    -1L, 0L,
                    0L, 50L,
                    1L, 200L,
                    2L, 600L,
                    3L, 1400L,
                    4L, 1700L,
                    5L, 2000L)
                )
                .costCard(400L)
                .costHouse(200L)
                .countHouse(0L)
                .color("lightblue")
                .build()
        );
        log.info("Response {} ==> {}", REALTY_MANAGER.getName(), realtyCardsList);
        return realtyCardsList;
    }

    private void checkMonopolies(PlayerDto player){
        Map<String, Integer> allColors = MonopolyMap.getColorsRealty(getAllRealtyCards());
        Map<String, Integer> colorMap = MonopolyMap.getColorsRealty(player.getRealtyList());
        List<String> monopolies = new ArrayList<>();
        for(String color : colorMap.keySet()){
            if(allColors.get(color).equals(colorMap.get(color))){
                monopolies.add(color);
            }
        }
        player.setMonopolies(monopolies);
    }

    public void setServicesManager(ServicesManager servicesManager){
        this.servicesManager = servicesManager;
    }
}
