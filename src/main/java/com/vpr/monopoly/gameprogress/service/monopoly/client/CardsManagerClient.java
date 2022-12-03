package com.vpr.monopoly.gameprogress.service.monopoly.client;

import com.vpr.monopoly.gameprogress.model.CardDto;
import com.vpr.monopoly.gameprogress.service.monopoly.CardsManagerService;
import com.vpr.monopoly.gameprogress.utils.CheckStatusError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardsManagerClient implements CardsManagerService {

    @Value("${cards.service.base.url}")
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
    public CardDto getChanceCard(String token) {
        String uri = "/chancecard/" + token;
        return this.getConnect(uri);
    }

    @Override
    public CardDto getCommunityChestCard(String token) {
        String uri = "/communitychestcard/" + token;
        return this.getConnect(uri);
    }

    @Override
    public void comebackPrisonCard(String token) {
        String uri = "/backprisoncard" + token;
        log.info("Requesting... to {}{}", baseUrl, uri);

        webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.fixedDelay(retryCount, Duration.ofSeconds(timeout))
                        .filter(CheckStatusError::isServerError))
                .onErrorResume(e -> {
                    log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                    return Mono.empty();
                })
                .blockOptional();
        log.info("Response {}{}", baseUrl, uri);
    }

    @Override
    public Map<String, List<CardDto>> initializingDecks() {
        String uri = "/init";
        log.info("Requesting... to {}{}", baseUrl, uri);

        Optional<Map<String, List<CardDto>>> response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<CardDto>>>(){})
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
    public Boolean checkConnection() {
        return initializingDecks() != null;
    }

    private CardDto getConnect(String uri){
        log.info("Requesting... to {}{}", baseUrl, uri);

        Optional<CardDto> response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .retrieve()
                .bodyToMono(CardDto.class)
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
}
