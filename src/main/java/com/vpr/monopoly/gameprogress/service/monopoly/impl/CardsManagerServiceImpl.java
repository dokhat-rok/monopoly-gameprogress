package com.vpr.monopoly.gameprogress.service.monopoly.impl;

import com.vpr.monopoly.gameprogress.model.CardDto;
import com.vpr.monopoly.gameprogress.model.SessionDto;
import com.vpr.monopoly.gameprogress.model.enam.CardType;
import com.vpr.monopoly.gameprogress.model.enam.ServiceType;
import com.vpr.monopoly.gameprogress.repository.SessionRepository;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.vpr.monopoly.gameprogress.model.enam.CardActionType.*;
import static com.vpr.monopoly.gameprogress.model.enam.CardType.Chance;
import static com.vpr.monopoly.gameprogress.model.enam.CardType.CommunityChest;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardsManagerServiceImpl implements CardsManagerService {

    private final SessionRepository sessionRepository;

    @Override
    public CardDto getChanceCard(String token) {
        return this.getCardFromDeck(token, Chance);
    }

    @Override
    public CardDto getCommunityChestCard(String token) {
        return this.getCardFromDeck(token, CommunityChest);
    }

    @Override
    public void comebackPrisonCard(String token) {
        log.info("Requesting... to {}", ServiceType.CARDS_MANAGER.getName());
        SessionDto session = sessionRepository.get(token);
        if(!session.getIsDecksHaveOutPrison().get(Chance.toString())){
            session.getIsDecksHaveOutPrison().put(Chance.toString(), true);
        }
        else if(!session.getIsDecksHaveOutPrison().get(CommunityChest.toString())){
            session.getIsDecksHaveOutPrison().put(CommunityChest.toString(), true);
        }
        log.info("Response {}", ServiceType.CARDS_MANAGER.getName());
        sessionRepository.set(token, session);
    }

    @Override
    public Map<String, List<CardDto>> initializingDecks() {
        List<CardDto> communityChestDeck = new ArrayList<>(List.of(
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Банковская ошибка в вашу пользу - получите 200")
                        .cardActionType(Money.toString())
                        .parameter(200)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Выгодная продажа акций - получите 25")
                        .cardActionType(Money.toString())
                        .parameter(25)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Вы получили наследство - получите 100")
                        .cardActionType(Money.toString())
                        .parameter(100)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Вас арестовали - отправляйтесь в тюрьму")
                        .cardActionType(Imprison.toString())
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Отправляйтесь на Старую дорогу")
                        .cardActionType(Move.toString())
                        .parameter(1)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Вы заняли второе место на конкурсе красоты - получите 10")
                        .cardActionType(Money.toString())
                        .parameter(10)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Оплата лечения - заплатите 100")
                        .cardActionType(Money.toString())
                        .parameter(-100)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Оплата услуг доктора - заплатите 50")
                        .cardActionType(Money.toString())
                        .parameter(-50)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Выгодная продажа облигаций - получите 50")
                        .cardActionType(Money.toString())
                        .parameter(50)
                        .build(),
                CardDto.builder()
                        .cardType(CommunityChest.toString())
                        .description("Освобождение из тюрьмы")
                        .cardActionType(OutPrison.toString())
                        .build()
        ));
        List<CardDto> chanceDeck = new ArrayList<>(List.of(
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Отправляйтесь в Гостинничный комплекс")
                        .cardActionType(Place.toString())
                        .parameter(39)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Пройдите на старт")
                        .cardActionType(Place.toString())
                        .parameter(0)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Освобождение из тюрьмы")
                        .cardActionType(OutPrison.toString())
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Отправляйтесь в Ресторан")
                        .cardActionType(Place.toString())
                        .parameter(24)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Вас арестовали - отправляйтесь в тюрьму")
                        .cardActionType(Imprison.toString())
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Вернитесь на три квартала назад")
                        .cardActionType(Move.toString())
                        .parameter(-3)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Пройдите на пять кварталов вперед")
                        .cardActionType(Move.toString())
                        .parameter(5)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Вы выиграли чемпионат по шахматам - получите 100")
                        .cardActionType(Money.toString())
                        .parameter(100)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Оплата курсов водителей - заплатите 150")
                        .cardActionType(Money.toString())
                        .parameter(-150)
                        .build(),
                CardDto.builder()
                        .cardType(Chance.toString())
                        .description("Отправляйтесь ну улицу Пушкина")
                        .cardActionType(Place.toString())
                        .parameter(16)
                        .build()
        ));
        Collections.shuffle(chanceDeck);
        Collections.shuffle(communityChestDeck);
        return new HashMap<>(Map.of(
                Chance.toString(), chanceDeck,
                CommunityChest.toString(), communityChestDeck
        ));
    }

    private CardDto getCardFromDeck(String token, CardType cardType){
        log.info("Requesting... to {}", ServiceType.CARDS_MANAGER.getName());
        SessionDto session = sessionRepository.get(token);
        if(session.getDecks().get(cardType.toString()).isEmpty()){
            session.getDecks().put(cardType.toString(), this.initializingDecks().get(cardType.toString()));
            this.deleteOutCards(session.getDecks().get(cardType.toString()));
        }

        CardDto card = session.getDecks().get(cardType.toString()).remove(0);
        log.info("Response {} ==> {}", ServiceType.CARDS_MANAGER.getName(), card);
        sessionRepository.set(token, session);
        return card;
    }

    private void deleteOutCards(List<CardDto> cards){
        for(CardDto card : cards){
            if(card.getCardActionType().equals(OutPrison.toString())){
                cards.remove(card);
                break;
            }
        }
    }
}
