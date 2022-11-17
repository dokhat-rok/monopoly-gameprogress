package com.vpr.monopoly.gameprogress.service.monopoly.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.model.enam.ActionType;
import com.vpr.monopoly.gameprogress.model.enam.ServiceType;
import com.vpr.monopoly.gameprogress.service.monopoly.PrisonService;
import com.vpr.monopoly.gameprogress.service.ServicesManager;
import com.vpr.monopoly.gameprogress.utils.ServicesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.MoneyOperation;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrisonServiceImpl implements PrisonService {

    @Value("${prison.service.start-day-imprison}")
    private Long startDayImprison;

    @Value("${prison.service.outer-cost}")
    private Long outerCost;

    private final ServicesManager servicesManager = ServicesUtils.INSTANCE;

    @Override
    public PlayerDto imprisonPlayer(PlayerDto player) {
        log.info("Requesting... to {}", ServiceType.PRISON.getName());
        player.setInPrison(startDayImprison);
        log.info("Response {} ==> {}", ServiceType.PRISON.getName(), player);
        return player;
    }

    @Override
    public ActionDto waiting(ActionDto action) {
        log.info("Requesting... to {}", ServiceType.PRISON.getName());
        PlayerDto player = (PlayerDto)action.getActionBody().get("player");
        switch (ActionType.valueOf(action.getActionType())){
            case Waiting:
                int[] throwDice = player.getLastRoll();
                if(throwDice[0] == throwDice[1]){
                    player.setInPrison(0L);
                }
                else{
                    player.setInPrison(player.getInPrison() - 1);
                }
                break;
            case LeavePrisonByCard:
                player.setPrisonOutCard(player.getPrisonOutCard() - 1);
                break;
            case LeavePrisonByMoney:
                ActionDto bankAction = ActionDto.builder()
                        .actionType(MoneyOperation.toString())
                        .actionBody(Map.of(
                                "playerList", List.of(player),
                                "money", -outerCost
                        ))
                        .build();
                bankAction = servicesManager.getBankService().playerToBankInteraction(bankAction);
                ObjectMapper objectMapper = new ObjectMapper();
                player = (PlayerDto) objectMapper
                        .convertValue(bankAction.getActionBody().get("playerList"), List.class)
                        .get(0);
                break;
        }
        action.getActionBody().put("player", player);
        log.info("Response {} ==> {}", ServiceType.PRISON.getName(), action);
        return action;
    }

    @Override
    public Boolean isWaiting(ActionDto action) {
        log.info("Requesting... to {}", ServiceType.PRISON.getName());
        PlayerDto player = (PlayerDto)action.getActionBody().get("player");
        Boolean result = false;
        switch (ActionType.valueOf(action.getActionType())){
            case LeavePrisonByCard:
                if(player.getPrisonOutCard() > 0){
                    result = true;
                }
                break;
            case LeavePrisonByMoney:
                ActionDto bankAction = ActionDto.builder()
                        .actionType(MoneyOperation.toString())
                        .actionBody(Map.of(
                                "playerList", List.of(player),
                                "money", -outerCost
                        ))
                        .build();
                result = servicesManager.getBankService().isPlayerToBankInteraction(bankAction);
                break;
        }
        log.info("Response {} ==> {}", ServiceType.PRISON.getName(), result);
        return result;
    }
}
