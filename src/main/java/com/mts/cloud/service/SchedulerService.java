package com.mts.cloud.service;

import com.mts.cloud.configuration.ResourceType;
import com.mts.cloud.service.price.Price;
import com.mts.cloud.service.price.PriceClient;
import com.mts.cloud.service.resource.GetResource;
import com.mts.cloud.service.resource.ResourceService;
import com.mts.cloud.service.statistic.Statistic;
import com.mts.cloud.service.statistic.StatisticClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    public static final int MAX_LOAD = 85;
    public static final int MIN_VM_LOAD = 50;
    public static final int MIN_DB_LOAD = 30;
    public static final int OPTIMAL_LOAD = 60;

    @Autowired
    public StatisticClient statisticClient;
    @Autowired
    public ResourceService resourceService;
    @Autowired
    public PriceClient priceClient;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 0)
    public void task() {
        Statistic statistic = statisticClient.get();
        List<GetResource> resources = resourceService.get();
        calculateDb(statistic, resources);
        calculateVm(statistic, resources);
    }

    private void calculateDb(Statistic statistic, List<GetResource> resources) {
        double dbCpuLoad = statistic.dbCpuLoad();
        double dbRamLoad = statistic.dbRamLoad();
        log.debug("db_cpu_load = {}  db_ram_load = {}", dbCpuLoad, dbRamLoad);
        List<GetResource> listDb = resources.stream()
                .filter(r -> r.type().equals(ResourceType.DB))
                .toList();
        boolean isFailed = listDb.stream()
                .anyMatch(GetResource::failed);
        if (isFailed) {
            return;
        }
        if (dbRamLoad == 0) {
            Price price = priceClient.getMax(ResourceType.DB);
            resourceService.add(price);
        }
        if (dbCpuLoad >= MAX_LOAD || dbRamLoad >= MAX_LOAD) {
            Price price = priceClient.getMax(ResourceType.DB);
            GetResource getResource = getMin(listDb);
            if (getResource == null) {
                resourceService.add(price);
            } else {
                resourceService.update(getResource.id(), price);
            }
        } else if (dbRamLoad <= MIN_DB_LOAD) {
            if (listDb.size() == 1) {
                Price price = priceClient.getMax(ResourceType.DB);
                resourceService.update(listDb.get(0).id(), price);
            } else {
                resourceService.delete(ResourceType.DB);
            }
        } else if (dbRamLoad < OPTIMAL_LOAD) {
            GetResource getResource = getMax(listDb);
            if (getResource == null) {
                resourceService.delete(ResourceType.DB);
            } else {
                Price price = priceClient.getMin(ResourceType.DB);
                resourceService.update(getResource.id(), price);
            }
        }
    }

    private void calculateVm(Statistic statistic, List<GetResource> resources) {
        double vmCpuLoad = statistic.vmCpuLoad();
        double vmRamLoad = statistic.vmRamLoad();
        log.debug("vm_cpu_load = {}  vm_ram_load = {}", vmCpuLoad, vmRamLoad);
        List<GetResource> listVm = resources.stream()
                .filter(r -> r.type().equals(ResourceType.VM))
                .toList();
        boolean isFailed = listVm.stream()
                .anyMatch(GetResource::failed);
        if (isFailed) {
            return;
        }
        if (vmRamLoad == 0) {
            Price price = priceClient.getMax(ResourceType.VM);
            resourceService.add(price);
        }
        if (vmCpuLoad >= MAX_LOAD || vmRamLoad >= MAX_LOAD) {
            Price price = priceClient.getMax(ResourceType.VM);
            GetResource getResource = getMin(listVm);
            if (getResource == null) {
                resourceService.add(price);
            } else {
                resourceService.update(getResource.id(), price);
            }
        } else if (vmRamLoad <= MIN_VM_LOAD) {
            if (listVm.size() == 1) {
                Price price = priceClient.getMax(ResourceType.VM);
                resourceService.update(listVm.get(0).id(), price);
            }
            resourceService.delete(ResourceType.VM);
        } else if (vmRamLoad < OPTIMAL_LOAD) {
            GetResource getResource = getMax(listVm);
            if (getResource == null) {
                resourceService.delete(ResourceType.VM);
            } else {
                Price price = priceClient.getMin(ResourceType.VM);
                resourceService.update(getResource.id(), price);
            }
        }
    }

    public GetResource getMin(List<GetResource> list) {
        return list.stream()
                .min(Comparator.comparingInt(GetResource::ram))
                .orElse(null);
    }

    public GetResource getMax(List<GetResource> list) {
        return list.stream()
                .max(Comparator.comparingInt(GetResource::ram))
                .orElse(null);
    }
}
