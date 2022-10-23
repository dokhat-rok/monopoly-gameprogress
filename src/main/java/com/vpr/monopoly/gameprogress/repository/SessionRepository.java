package com.vpr.monopoly.gameprogress.repository;

import com.vpr.monopoly.gameprogress.model.SessionDto;

import java.util.List;

public interface SessionRepository {

    SessionDto find(String key);

    List<SessionDto> findAll();

    boolean add(String key, SessionDto session);

    boolean update(String key, SessionDto session);

    boolean delete(String key);

    boolean addPlayer(String key, Object player);

    boolean deletePlayer(String key, String figure);

    boolean updatePlayer(String key, Object player);

    boolean updatePlayers(String key, List<Object> players);

}
