package com.vpr.monopoly.gameprogress.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Schema(description = "Модель действия и результата действия в ходе игры")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto {

    @Schema(description = "Тип действия")
    private String actionType;

    @Schema(description = "Действие")
    private Map<String, Object> actionBody;
}
