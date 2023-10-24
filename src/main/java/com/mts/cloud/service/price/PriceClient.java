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
        List<Price> listDb = new ArrayList<>(this.prices.get(ResourceType.DB).stream()
                .filter(price -> price.cpu() == price.ram())
                .toList());
        this.prices.put(ResourceType.DB, listDb);

        List<Price> listVm = this.prices.get(ResourceType.VM).stream()
                .filter(price -> price.ram() / price.cpu() == 2)
                .toList();
        this.prices.put(ResourceType.VM, listVm);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        getPrices();
    }
}
