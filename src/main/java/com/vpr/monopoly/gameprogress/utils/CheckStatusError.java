package com.vpr.monopoly.gameprogress.utils;

import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class CheckStatusError {

    public static boolean isServerError(Throwable throwable){
        if(throwable instanceof WebClientResponseException){
            WebClientResponseException w = (WebClientResponseException) throwable;
            return w.getStatusCode().is5xxServerError();
        }
        return throwable instanceof WebClientRequestException;
    }
}
