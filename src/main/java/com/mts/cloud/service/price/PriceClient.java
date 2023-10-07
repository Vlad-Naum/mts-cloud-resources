package com.mts.cloud.service.price;

import com.mts.cloud.configuration.ResourceType;
import com.mts.cloud.service.resource.ResourceClient;
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

    @Autowired
    public WebClient webClient;
    @Autowired
    public ResourceClient resourceClient;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        getPrices();
    }
}
