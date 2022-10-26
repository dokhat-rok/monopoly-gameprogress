package com.vpr.monopoly.gameprogress.controller;


import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;
import com.vpr.monopoly.gameprogress.model.StartDataDto;
import com.vpr.monopoly.gameprogress.model.enam.ActionType;
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

    @PostMapping("/start/{count}")
    public ResponseEntity<StartDataDto> startGame(
            @PathVariable @Min(2) @Parameter(description = "Количество игроков", example = "2") Long count,
            @RequestBody String[] playerFigures
    ){
        log.info("Create {} players with figures {}", count, playerFigures);
        ArrayList<PlayerDto> players = new ArrayList<>();
        players.add(PlayerDto.builder()
                .lastRoll(new int[]{4, 6})
                .countDouble(2)
                .position(0)
                .prisonOutCard(1)
                .money(10000L)
                .realtyList(new ArrayList<>())
                .playerFigure("Car")
                .build()
        );
        players.add(PlayerDto.builder()
                .lastRoll(new int[]{1, 4})
                .countDouble(1)
                .position(0)
                .prisonOutCard(0)
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
                .priceMap(new HashMap<>())
                .costCard(100L)
                .color("Yellow")
                .build()
        );
        return ResponseEntity.ok(
                new StartDataDto()
                        .toBuilder()
                        .players(players)
                        .realtyList(realty)
                        .build()
        );
    }

    @PutMapping("/action")
    public ResponseEntity<ActionDto> actionPlayer(){
        log.info("Player action");
        return ResponseEntity.ok(
                new ActionDto()
                        .toBuilder()
                        .actionBody(new HashMap<>())
                        .actionType("DropDice")
                        .build()
        );
    }

    @GetMapping("/endgame")
    public ResponseEntity<String> startGame(){
        log.info("End game");
        return ResponseEntity.ok(
                "History"
        );
    }
}
