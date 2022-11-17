package com.vpr.monopoly.gameprogress.service.monopoly;

import com.vpr.monopoly.gameprogress.model.ActionDto;
import com.vpr.monopoly.gameprogress.utils.CheckStatusError;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Optional;

public interface MonopolyService {
    default Boolean checkConnection(){
        return true;
    }

    default ActionDto connectByAction(WebClient webClient, String baseUrl, String uri, HttpMethod httpMethod, ActionDto action, Logger log){
        log.info("Requesting... to {}{}", baseUrl, uri);

        Optional<ActionDto> response = Optional.empty();
        switch (httpMethod){
            case POST:
                response = webClient
                        .post()
                        .uri(uriBuilder -> uriBuilder.path(uri).build())
                        .body(action, ActionDto.class)
                        .retrieve()
                        .bodyToMono(ActionDto.class)
                        .retryWhen(Retry.max(4)
                                .filter(CheckStatusError::isServerError))
                        .onErrorResume(e -> {
                            log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                            return Mono.empty();
                        })
                        .blockOptional();
                break;
            case PUT:
                response = webClient
                        .put()
                        .uri(uriBuilder -> uriBuilder.path(uri).build())
                        .body(action, ActionDto.class)
                        .retrieve()
                        .bodyToMono(ActionDto.class)
                        .retryWhen(Retry.max(4)
                                .filter(CheckStatusError::isServerError))
                        .onErrorResume(e -> {
                            log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                            return Mono.empty();
                        })
                        .blockOptional();
                break;
        }

        log.info("Response {}{} ==> {}", baseUrl, uri, response.orElse(null));
        return response.orElse(null);
    }

    default Boolean connectByIsAction(WebClient webClient, String baseUrl, String uri, HttpMethod httpMethod, ActionDto action, Logger log){
        log.info("Requesting... to {}{}", baseUrl, uri);

        Optional<Boolean> response = Optional.empty();
        switch (httpMethod){
            case POST:
                response = webClient
                        .post()
                        .uri(uriBuilder -> uriBuilder.path(uri).build())
                        .body(Mono.just(action), ActionDto.class)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .retryWhen(Retry.max(4)
                                .filter(CheckStatusError::isServerError))
                        .onErrorResume(e -> {
                            log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                            return Mono.empty();
                        })
                        .blockOptional();
                break;
            case PUT:
                response = webClient
                        .put()
                        .uri(uriBuilder -> uriBuilder.path(uri).build())
                        .body(Mono.just(action), ActionDto.class)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .retryWhen(Retry.max(4)
                                .filter(CheckStatusError::isServerError))
                        .onErrorResume(e -> {
                            log.error("Response {}{} ==> {}", baseUrl, uri, e.getMessage());
                            return Mono.empty();
                        })
                        .blockOptional();
                break;
        }

        log.info("Response {}{} ==> {}", baseUrl, uri, response.orElse(null));
        return response.orElse(null);
    }
}
