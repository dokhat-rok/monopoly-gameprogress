package com.vpr.monopoly.gameprogress.controller;


import com.vpr.monopoly.gameprogress.model.*;
import com.vpr.monopoly.gameprogress.model.enam.ActionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.vpr.monopoly.gameprogress.config.OpenApiConfig.PROGRESS;

@Tag(name = PROGRESS, description = "API для управления ходом игры")
@RestController
@RequestMapping("/v1/progress")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    @Operation(summary = "Запрос начальных данных для начала игровой сессии")
    @PostMapping("/start/{count}")
    public ResponseEntity<StartDataDto> startGame(
            @PathVariable @Min(2) @Parameter(description = "Количество игроков", example = "2") Long count,
            @RequestBody String[] playerFigures
    ){
        log.info("Create {} players with figures {}", count, playerFigures);
        ArrayList<PlayerDto> players = new ArrayList<>();
        players.add(PlayerDto.builder()
                .lastRoll(new int[2])
                .money(10000L)
                .realtyList(new ArrayList<>())
                .playerFigure("Car")
                .build()
        );
        players.add(PlayerDto.builder()
                .lastRoll(new int[2])
                .money(10000L)
                .realtyList(new ArrayList<>())
                .playerFigure("Ship")
                .build()
        );
        ArrayList<RealtyCardDto> realty = new ArrayList<>();
        realty.add(RealtyCardDto.builder()
                .position(4)
                .streetName("Пушкинская")
                .owner("Boot")
                .priceMap(Map.of(
                        0L, 10L,
                        1L, 50L,
                        2L, 150L,
                        3L, 310L,
                        4L, 500L,
                        5L, 870L
                ))
                .costCard(100L)
                .color("Yellow")
                .build()
        );
        return ResponseEntity.ok(
                new StartDataDto()
                        .toBuilder()
                        .token(String.valueOf(System.currentTimeMillis()))
                        .players(players)
                        .realtyList(realty)
                        .build()
        );
    }

    @Operation(summary = "Запрос на действие игрока в определенной сессии")
    @PutMapping("/action/{token}")
    public ResponseEntity<ActionDto> actionPlayer(@PathVariable(name = "token") String token, @RequestBody ActionDto action){
        log.info("Player action");
        Map<String, Object> actionBody = new HashMap<>();
        actionBody.put(
                "Player",
                PlayerDto.builder()
                    .lastRoll(new int[]{5, 0})
                    .money(10000L)
                    .realtyList(new ArrayList<>())
                    .playerFigure("Ship")
                    .build()
        );
        return ResponseEntity.ok(
                new ActionDto()
                        .toBuilder()
                        .actionBody(actionBody)
                        .actionType(ActionType.DROP_DICE.getLabel())
                        .build()
        );
    }

    @Operation(summary = "Окончание игровой сессии и получение истории хода игры")
    @GetMapping("/endgame/{token}")
    public ResponseEntity<String> endGame(@PathVariable(name = "token") String token){
        log.info("End game");
        return ResponseEntity.ok(
                "History"
        );
    }
}
