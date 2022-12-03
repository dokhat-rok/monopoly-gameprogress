package com.vpr.monopoly.gameprogress.service.monopoly.client;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.model.RealtyCardDto;
import com.vpr.monopoly.gameprogress.service.monopoly.RealtyManagerService;
import com.vpr.monopoly.gameprogress.utils.CheckStatusError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class RealtyManagerClient implements RealtyManagerService {

    @Value("${realty.service.base.url}")
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
    public ActionDto playerToBankInteraction(ActionDto action) {
        String uri = "/tobank";
        return this.connectByAction(webClient, baseUrl, uri, HttpMethod.POST, action, log, retryCount, timeout);
    }

    @Override
    public Boolean isPlayerToBankInteraction(ActionDto action) {
        String uri = "/istobank";
        return this.connectByIsAction(webClient, baseUrl, uri, HttpMethod.POST, action, log, retryCount, timeout);
    }

    @Override
    public ActionDto playerToPlayerInteraction(ActionDto action) {
        String uri = "/toplayer";
        return this.connectByAction(webClient, baseUrl, uri, HttpMethod.POST, action, log, retryCount, timeout);
    }

    @Override
    public Boolean isPlayerToPlayerInteraction(ActionDto action) {
        String uri = "/istoplayer";
        return this.connectByIsAction(webClient, baseUrl, uri, HttpMethod.POST, action, log, retryCount, timeout);
    }

    @Override
    public List<RealtyCardDto> getAllRealtyCards() {
        String uri = "/allcards";
        List<RealtyCardDto> response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(uri).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RealtyCardDto>>() {
                })
                .retryWhen(Retry.fixedDelay(retryCount, Duration.ofSeconds(timeout))
                        .filter(CheckStatusError::isServerError))
                .onErrorResume(e -> {
                    log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                    return Mono.empty();
                })
                .block();

        log.info("Response {}{} ==> {}", baseUrl, uri, response);
        return response;
    }

    @Override
    public Boolean checkConnection() {
        return this.getAllRealtyCards() != null;
    }
}
