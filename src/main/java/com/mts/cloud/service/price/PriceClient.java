package com.mts.cloud.service.price;

import com.mts.cloud.configuration.ResourceType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceClient implements InitializingBean {

    public static final String PRICE_URL = "/price";

    public Map<ResourceType, List<Price>> prices;

    private Price minDb;
    private Price maxDb;
    private Price minVm;
    private Price maxVm;

    @Autowired
    public WebClient webClient;

    public void getPrices() {
        Price[] prices = webClient.get()
                .uri(PRICE_URL)
                .retrieve()
                .bodyToMono(Price[].class)
                .block();
        if (prices == null) {
            return;
        }
        this.prices = Arrays.stream(prices)
                .collect(Collectors.groupingBy(Price::type));
    }

    public Price getMin(ResourceType type) {
        return switch (type) {
            case DB -> minDb;
            case VM -> minVm;
        };
    }

    public Price getMax(ResourceType type) {
        return switch (type) {
            case DB -> maxDb;
            case VM -> maxVm;
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        getPrices();
        this.minDb = prices.get(ResourceType.DB).stream()
                .min(Comparator.comparingInt(Price::cpu))
                .get();
        this.minVm = prices.get(ResourceType.VM).stream()
                .min(Comparator.comparingInt(Price::cpu))
                .get();
        this.maxDb = prices.get(ResourceType.DB).stream()
                .max(Comparator.comparingInt(Price::cpu))
                .get();
        this.maxVm = prices.get(ResourceType.VM).stream()
                .max(Comparator.comparingInt(Price::cpu))
                .get();
    }
}
