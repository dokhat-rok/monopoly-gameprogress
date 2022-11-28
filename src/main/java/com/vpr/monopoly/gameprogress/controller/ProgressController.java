package com.vpr.monopoly.gameprogress.controller;


import com.vpr.monopoly.gameprogress.model.*;
import com.vpr.monopoly.gameprogress.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;
import java.util.List;

import static com.vpr.monopoly.gameprogress.config.OpenApiConfig.PROGRESS;

@Tag(name = PROGRESS, description = "API для управления ходом игры")
@RestController
@RequestMapping("/v1/progress")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final ProgressService progressService;

    @Operation(summary = "Запрос начальных данных для начала игровой сессии")
    @PostMapping("/start")
    public ResponseEntity<StartDataDto> startGame(
            @Size(min = 2, max = 8) @RequestBody String[] playerFigures
    ){
        StartDataDto startData = progressService.startGame(playerFigures);
        log.info("Create {} players with figures {}",playerFigures.length, playerFigures);
        return ResponseEntity.ok(startData);
    }

    @Operation(summary = "Запрос на действие игрока в определенной сессии")
    @PutMapping("/action/{token}")
    public ResponseEntity<ActionDto> actionPlayer(
            @PathVariable("token") @Parameter(description = "Токен сессии", example = "token") String sessionToken,
            @RequestBody ActionDto action
    ){
        ActionDto resultAction = progressService.actionPlayer(sessionToken, action);
        log.info("Player action {}", action.getActionType());
        return ResponseEntity.ok(resultAction);
    }

    @Operation(summary = "Окончание игровой сессии и получение истории хода игры")
    @GetMapping("/endgame/{token}")
    public ResponseEntity<List<String>> endGame(
            @PathVariable("token") @Parameter(description = "Токен сессии", example = "token") String sessionToken
    ){
        List<String> history = progressService.endGame(sessionToken);
        log.info("End game");
        return ResponseEntity.ok(history);
    }
}
