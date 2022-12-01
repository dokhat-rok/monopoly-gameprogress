package com.vpr.monopoly.gameprogress.service.monopoly.client;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.PlayerDto;
import com.vpr.monopoly.gameprogress.service.monopoly.PrisonService;
import com.vpr.monopoly.gameprogress.utils.CheckStatusError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static com.vpr.monopoly.gameprogress.model.enam.ActionType.MoneyOperation;

@Component
@Slf4j
public class PrisonClient implements PrisonService {

    @Value("${prison.service.base.url}")
    private String baseUrl;

    @Value("${services.retry.count}")
    private Integer retryCount;

    @Value("${services.timeout}")
    private Integer timeout;

    private WebClient webClient;

    @PostConstruct
    private void init(){
        webClient = WebClient.create(baseUrl);
    }

    @Override
    public PlayerDto imprisonPlayer(PlayerDto player) {
        String uri = "/imprison";
        Optional<PlayerDto> response = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .body(Mono.just(player), PlayerDto.class)
                .retrieve()
                .bodyToMono(PlayerDto.class)
                .retryWhen(Retry.fixedDelay(retryCount, Duration.ofSeconds(timeout))
                        .filter(CheckStatusError::isServerError))
                .onErrorResume(e -> {
                    log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                    return Mono.empty();
                })
                .blockOptional();

        log.info("Response {}{} ==> {}", baseUrl, uri, response.orElse(null));
        return response.orElse(null);
    }

    @Override
    public ActionDto waiting(String token, ActionDto action) {
        String uri = "/waiting/" + token;
        return this.connectByAction(webClient, baseUrl, uri, HttpMethod.PUT, action, log, retryCount, timeout);
    }

    @Override
    public Boolean isWaiting(ActionDto action) {
        String uri = "/iswaiting";
        return this.connectByIsAction(webClient, baseUrl, uri, HttpMethod.PUT, action, log, retryCount, timeout);
    }

    @Override
    public Boolean checkConnection() {
        ActionDto action = ActionDto.builder()
                .actionType(MoneyOperation.toString())
                .actionBody(Map.of(
                        "player", PlayerDto.builder().money(1000L).build()
                ))
                .build();
        return this.isWaiting(action) != null;
    }
}
