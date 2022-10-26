package com.vpr.monopoly.gameprogress.repository.impl;

import com.vpr.monopoly.gameprogress.model.SessionDto;
import com.vpr.monopoly.gameprogress.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final RedisTemplate<String, SessionDto> template;

    @Override
    public SessionDto get(String key) {
        return template.opsForValue().get(key);
    }

    @Override
    public List<SessionDto> getAll() {
        List<SessionDto> sessionDtoList = new ArrayList<>();
        Set<String> keys = template.keys("[0-9]*");
        if(keys == null){
            return sessionDtoList;
        }

        for(String key : keys){
            sessionDtoList.add(template.opsForValue().get(key));
        }

        return sessionDtoList;
    }

    @Override
    public boolean set(String key, SessionDto session) {
        template.opsForValue().set(key, session);
        return true;
    }

    @Override
    public boolean remove(String key) {
        return template.opsForValue().getAndDelete(key) != null;
    }

    @Override
    public boolean removeAll() {
        Set<String> keys = template.keys("[0-9]*");
        if(keys == null){
            return false;
        }

        for(String key : keys){
            template.opsForValue().getAndDelete(key);
        }
        return false;
    }
}
