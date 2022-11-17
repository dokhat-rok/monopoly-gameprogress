package com.vpr.monopoly.gameprogress.service.monopoly.client;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.service.monopoly.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.MoneyOperation;

@Component
@Slf4j
public class BankClient implements BankService {

    @Value("${bank.service.base.url}")
    private String baseUrl;

    private WebClient webClient;

    @PostConstruct
    private void init(){
        webClient = WebClient.create(baseUrl);
    }

    @Override
    public ActionDto playerToBankInteraction(ActionDto action) {
        String uri = "/tobank";
        return this.connectByAction(webClient, baseUrl, uri, HttpMethod.PUT, action, log);
    }

    @Override
    public Boolean isPlayerToBankInteraction(ActionDto action) {
        String uri = "/istobank";
        return this.connectByIsAction(webClient, baseUrl, uri, HttpMethod.PUT, action, log);
    }

    @Override
    public ActionDto playerToPlayerInteraction(ActionDto action) {
        String uri = "/toplayer";
        return this.connectByAction(webClient, baseUrl, uri, HttpMethod.PUT, action, log);
    }

    @Override
    public Boolean isPlayerToPlayerInteraction(ActionDto action) {
        String uri = "/istoplayer";
        return this.connectByIsAction(webClient, baseUrl, uri, HttpMethod.PUT, action, log);
    }

    @Override
    public Boolean checkConnection() {
        List<PlayerDto> playerList = List.of(PlayerDto.builder().build());
        Long money = 100L;
        ActionDto action = ActionDto.builder()
                .actionType(MoneyOperation.toString())
                .actionBody(Map.of(
                        "playerList", playerList,
                        "money", money
                ))
                .build();

        return this.isPlayerToBankInteraction(action) != null;
    }
}
