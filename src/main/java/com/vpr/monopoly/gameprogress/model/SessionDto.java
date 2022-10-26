package com.vpr.monopoly.gameprogress.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("Session")
public class SessionDto implements Serializable {

    private List<Object> players;

    private List<Object> realty;

    private List<Object> chanceCards;

    private List<Object> communityChestCards;

    private List<Object> playersInPrison;
}
