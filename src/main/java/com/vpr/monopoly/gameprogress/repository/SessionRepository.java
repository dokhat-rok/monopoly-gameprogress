package com.vpr.monopoly.gameprogress.repository;

import com.vpr.monopoly.gameprogress.model.SessionDto;

import java.util.List;

public interface SessionRepository {

    SessionDto get(String key);

    List<SessionDto> getAll();

    boolean set(String key, SessionDto session);

    boolean remove(String key);

    boolean removeAll();

}
