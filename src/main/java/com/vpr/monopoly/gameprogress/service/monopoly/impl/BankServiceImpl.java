package com.vpr.monopoly.gameprogress.service.monopoly.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.model.enam.ServiceType;
import com.vpr.monopoly.gameprogress.service.monopoly.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.MoneyOperation;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ActionDto playerToBankInteraction(ActionDto action) {
        log.info("Requesting... to {}", ServiceType.BANK.getName());
        ActionDto result = null;

        if(action.getActionType().equals(MoneyOperation.toString())){

            List<PlayerDto> playerList = objectMapper.convertValue(action.getActionBody().get("playerList"), new TypeReference<>() {});
            PlayerDto player = playerList.get(0);
            Long money = (Long) action.getActionBody().get("money");

            player.setMoney(player.getMoney() + money);
            action.getActionBody().put("playerList", playerList);
            action.getActionBody().remove("money");
            result = action;
        }

        log.info("Response {} ==> {}", ServiceType.BANK.getName(), result);
        return result;
    }

    @Override
    public Boolean isPlayerToBankInteraction(ActionDto action) {
        log.info("Requesting... to {}", ServiceType.BANK.getName());
        ActionDto resultAction = this.playerToBankInteraction(action);

        List<PlayerDto> playerList = objectMapper.convertValue(resultAction.getActionBody().get("playerList"), new TypeReference<>() {});
        PlayerDto player = playerList.get(0);
        Boolean result = player.getMoney() >= 0;

        log.info("Response {} ==> {}", ServiceType.BANK.getName(), result);
        return result;
    }

    @Override
    public ActionDto playerToPlayerInteraction(ActionDto action) {
        log.info("Requesting... to {}", ServiceType.BANK.getName());
        ActionDto result = null;

        if(action.getActionType().equals(MoneyOperation.toString())){

            List<PlayerDto> playerList = objectMapper.convertValue(action.getActionBody().get("playerList"), new TypeReference<>() {});
            PlayerDto player1 = playerList.get(0);
            PlayerDto player2 = playerList.get(1);
            Long money = (Long) action.getActionBody().get("money");

            player1.setMoney(player1.getMoney() + money);
            player2.setMoney(player2.getMoney() - money);
            action.getActionBody().put("playerList", playerList);
            action.getActionBody().remove("money");
            result = action;
        }
        log.info("Response {} ==> {}", ServiceType.BANK.getName(), result);
        return result;
    }

    @Override
    public Boolean isPlayerToPlayerInteraction(ActionDto action) {
        log.info("Requesting... to {}", ServiceType.BANK.getName());
        ActionDto resultAction = this.playerToPlayerInteraction(action);

        List<PlayerDto> playerList = objectMapper.convertValue(resultAction.getActionBody().get("playerList"), new TypeReference<>() {});
        PlayerDto player1 = playerList.get(0);
        PlayerDto player2 = playerList.get(1);
        Boolean result = player1.getMoney() >= 0 && player2.getMoney() >= 0;

        log.info("Response {} ==> {}", ServiceType.BANK.getName(), result);
        return result;
    }
}
