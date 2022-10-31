package com.vpr.monopoly.gameprogress.controller;


import com.vpr.monopoly.gameprogress.model.*;
import com.vpr.monopoly.gameprogress.service.ProgressService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;


import static com.vpr.monopoly.gameprogress.config.OpenApiConfig.PROGRESS;

@Tag(name = PROGRESS, description = "API для управления ходом игры")
@RestController
@RequestMapping("/v1/progress")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/start/{count}")
    public ResponseEntity<StartDataDto> startGame(
            @PathVariable @Min(2) @Parameter(description = "Количество игроков", example = "2") Long count,
            @RequestBody String[] playerFigures
    ){
        StartDataDto startData = progressService.startGame(count, playerFigures);
        log.info("Create {} players with figures {}", count, playerFigures);
        return ResponseEntity.ok(startData);
    }

    @PutMapping("/action/{token}")
    public ResponseEntity<ActionDto> actionPlayer(
            @PathVariable("token") @Parameter(description = "Токен сессии", example = "token") String sessionToken,
            @RequestBody ActionDto action
    ){
        ActionDto resultAction = progressService.actionPlayer(sessionToken, action);
        log.info("Player action {}", action.getActionType());
        return ResponseEntity.ok(resultAction);
    }

    @GetMapping("/endgame")
    public ResponseEntity<String> endGame(){
        String history = progressService.endGame();
        log.info("End game");
        return ResponseEntity.ok(history);
    }
}
