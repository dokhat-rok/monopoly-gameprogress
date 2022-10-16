package com.vpr.monopoly.gameprogress.controller;


import com.vpr.monopoly.gameprogress.model.StartDataDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/start/{count}")
    public StartDataDto startGame(
            @PathVariable @Min(2) @Parameter(description = "Количество игроков", example = "2") Long count,
            @RequestBody String[] playerFigures
    ){
        log.info("Create {} players with figures {}", count, playerFigures);
        return new StartDataDto();
    }
}
