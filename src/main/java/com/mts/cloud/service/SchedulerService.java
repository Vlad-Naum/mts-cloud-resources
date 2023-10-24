package com.mts.cloud.service;

import com.mts.cloud.configuration.ResourceType;
import com.mts.cloud.service.price.Price;
import com.mts.cloud.service.price.PriceService;
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
    public static final int OPTIMAL_LOAD = 80;
    public static final int MIN_OPTIMAL_LOAD = 75;
    public static final int MAX_OPTIMAL_LOAD = 85;

    @Autowired
    public StatisticClient statisticClient;
    @Autowired
    public ResourceService resourceService;
    @Autowired
    public PriceService priceService;

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS, initialDelay = 0)
    public void task() {
        Statistic statistic = statisticClient.get();
        List<GetResource> resources = resourceService.get();
        calculateDb2(getOptimalDb(statistic), statistic, resources);
        calculateVm2(getOptimalVm(statistic), statistic, resources);
    }

    public void calculateDb2(int optimalDb, Statistic statistic, List<GetResource> resources) {
        double dbCpuLoad = statistic.dbCpuLoad();
        double dbRamLoad = statistic.dbRamLoad();
        log.debug("db_cpu_load = {}  db_ram_load = {}", dbCpuLoad, dbRamLoad);
        List<GetResource> listDb = resources.stream()
                .filter(r -> r.type().equals(ResourceType.DB))
                .toList();
        boolean isFailed = listDb.stream()
                .anyMatch(GetResource::failed);
        if (isFailed) {
            log.debug("db is failed");
            return;
        }
        if (listDb.isEmpty()) {
            Price price = priceService.getMaxDb();
            resourceService.add(price);
            return;
        }
        if (dbRamLoad > 90 || dbCpuLoad > 90) {
            Price price = priceService.getMaxDb();
            resourceService.add(price);
            return;
        }
        if ((dbRamLoad < MIN_OPTIMAL_LOAD || dbRamLoad > MAX_OPTIMAL_LOAD) && dbRamLoad != 0) {
            int dbRam = listDb.stream().mapToInt(GetResource::ram).sum();
            if (dbRam < optimalDb) {
                Price pricesForDb = priceService.getPricesForDb(optimalDb, dbRam);
                if (pricesForDb != null) {
                    resourceService.add(pricesForDb);
                }
            } else {
                int count = dbRam - optimalDb;
                if (listDb.size() > 1) {
                    listDb.stream()
                            .filter(getResource -> getResource.ram() <= count)
                            .max(Comparator.comparingInt(GetResource::ram))
                            .ifPresent(resource -> resourceService.delete(resource.id()));
                }
            }
        }
    }

    public void calculateVm2(int optimalVm, Statistic statistic, List<GetResource> resources) {
        double vmCpuLoad = statistic.vmCpuLoad();
        double vmRamLoad = statistic.vmRamLoad();
        log.debug("vm_cpu_load = {}  vm_ram_load = {}", vmCpuLoad, vmRamLoad);
        List<GetResource> listVm = resources.stream()
                .filter(r -> r.type().equals(ResourceType.VM))
                .toList();
        boolean isFailed = listVm.stream()
                .anyMatch(GetResource::failed);
        if (isFailed) {
            log.debug("vm is failed");
            return;
        }
        if (listVm.isEmpty()) {
            Price price = priceService.getMaxVm();
            resourceService.add(price);
            return;
        }
        if (vmRamLoad > 90 || vmCpuLoad > 90) {
            Price price = priceService.getMaxVm();
            resourceService.add(price);
            return;
        }
        if ((vmRamLoad < MIN_OPTIMAL_LOAD || vmRamLoad > MAX_OPTIMAL_LOAD) && vmRamLoad != 0) {
            int vmRam = listVm.stream().mapToInt(GetResource::ram).sum();
            if (vmRam < optimalVm) {
                Price pricesForVm = priceService.getPricesForVm(optimalVm, vmRam);
                if (pricesForVm != null) {
                    resourceService.add(pricesForVm);
                }
            } else {
                int count = vmRam - optimalVm;
                if (listVm.size() > 1) {
                    listVm.stream()
                            .filter(getResource -> getResource.ram() <= count)
                            .max(Comparator.comparingInt(GetResource::ram))
                            .ifPresent(resource -> resourceService.delete(resource.id()));
                }
            }
        }
    }

    public int getOptimalDb(Statistic statistic) {
        int dbRam = statistic.dbRam();
        double dbRamLoad = statistic.dbRamLoad() / 100;
        int requests = statistic.requests();
        double i = ((double) requests / dbRam) * (double) OPTIMAL_LOAD / 100;
        double i2 = i / dbRamLoad;
        return (int) ((double) requests / i2);
    }

    public int getOptimalVm(Statistic statistic) {
        int vmRam = statistic.vmRam();
        double vmRamLoad = statistic.vmRamLoad() / 100;
        int requests = statistic.requests();
        double i = ((double) requests / vmRam) * (double) OPTIMAL_LOAD / 100;
        double i2 = i / vmRamLoad;
        return (int) ((double) requests / i2);
    }
}
