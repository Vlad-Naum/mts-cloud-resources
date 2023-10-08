package com.mts.cloud.service;

import com.mts.cloud.configuration.ResourceType;
import com.mts.cloud.service.price.Price;
import com.mts.cloud.service.price.PriceClient;
import com.mts.cloud.service.resource.ResourceService;
import com.mts.cloud.service.statistic.Statistic;
import com.mts.cloud.service.statistic.StatisticClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    public static final int MAX_LOAD = 85;
    public static final int MIN_LOAD = 20;

    @Autowired
    public StatisticClient statisticClient;
    @Autowired
    public ResourceService resourceService;
    @Autowired
    public PriceClient priceClient;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 0)
    public void task() {
        Statistic statistic = statisticClient.get();
        calculateDb(statistic);
        calculateVm(statistic);
    }

    private void calculateDb(Statistic statistic) {
        double dbCpuLoad = statistic.dbCpuLoad();
        double dbRamLoad = statistic.dbRamLoad();
        log.debug("db_cpu_load = {}  db_ram_load = {}", dbCpuLoad, dbRamLoad);
        if (dbCpuLoad >= MAX_LOAD || dbRamLoad >= MAX_LOAD) {
            Price price = priceClient.prices.get(ResourceType.DB)
                    .stream()
                    .max(Comparator.comparingInt(Price::cpu))
                    .get();
            resourceService.add(price);
        } else if (dbRamLoad <= MIN_LOAD) {
            resourceService.delete(ResourceType.DB);
        }
    }

    private void calculateVm(Statistic statistic) {
        double vmCpuLoad = statistic.vmCpuLoad();
        double vmRamLoad = statistic.vmRamLoad();
        log.debug("vm_cpu_load = {}  vm_ram_load = {}", vmCpuLoad, vmRamLoad);
        if (vmCpuLoad >= MAX_LOAD || vmRamLoad >= MAX_LOAD) {
            Price price = priceClient.prices.get(ResourceType.VM)
                    .stream()
                    .max(Comparator.comparingInt(Price::cpu))
                    .get();
            resourceService.add(price);
        } else if (vmRamLoad <= MIN_LOAD) {
            resourceService.delete(ResourceType.DB);
        }
    }
}
