package com.mts.cloud.service.statistic;

import com.mts.cloud.CloudApplicationConst;
import com.mts.cloud.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static com.mts.cloud.service.statistic.Statistic.*;


@Service
public class StatisticClient {

    public static final String STATISTIC_PATH = "/statistic";

    @Autowired
    public WebClient webClient;
    @Autowired
    public ApplicationProperties applicationProperties;

    public Statistic get() {
        Map mapStatistic = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(STATISTIC_PATH)
                        .queryParam(CloudApplicationConst.TOKEN, applicationProperties.token())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (mapStatistic == null || mapStatistic.isEmpty()) {
            return null;
        }
        return new Statistic((Integer) mapStatistic.get(DB_CPU),
                (Double) mapStatistic.get(DB_CPU_LOAD),
                (Integer) mapStatistic.get(DB_RAM),
                (Double) mapStatistic.get(DB_RAM_LOAD),
                (Integer) mapStatistic.get(RESPONSE_TIME),
                (Integer) mapStatistic.get(VM_CPU),
                (Double) mapStatistic.get(VM_CPU_LOAD),
                (Integer) mapStatistic.get(VM_RAM),
                (Double) mapStatistic.get(VM_RAM_LOAD),
                (Integer) mapStatistic.get(REQUESTS));
    }
}
