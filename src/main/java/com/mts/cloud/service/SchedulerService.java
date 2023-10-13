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
    public static final int MIN_OPTIMAL_LOAD = 70;
    public static final int MAX_OPTIMAL_LOAD = 80;

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
        calculateDb2(getOptimalDb(statistic), statistic, resources);
        calculateVm2(getOptimalVm(statistic), statistic, resources);
//        calculateDb(statistic, resources);
//        calculateVm(statistic, resources);
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
            Price price = priceClient.getMax(ResourceType.DB);
            resourceService.add(price);
            return;
        }
        if ((dbRamLoad < MIN_OPTIMAL_LOAD || dbRamLoad > MAX_OPTIMAL_LOAD) && dbRamLoad != 0) {
            int dbRam = listDb.stream().mapToInt(GetResource::ram).sum();
            if (dbRam < optimalDb) {
                int count = optimalDb - dbRam;
                int countMax = count / 10;
                int countMin = count % 10;
                for (int i = 0; i < countMax; i++) {
                    Price price = priceClient.getMax(ResourceType.DB);
                    resourceService.add(price);
                }
                for (int i = 0; i < countMin; i++) {
                    Price price = priceClient.getMin(ResourceType.DB);
                    resourceService.add(price);
                }
            } else {
                int count = dbRam - optimalDb;
                int countMax = count / 10;
                int countMin = count % 10;
                Price priceMin = priceClient.getMin(ResourceType.DB);
                List<GetResource> dbMaxResourceList = listDb.stream()
                        .filter(getResource -> getResource.ram() == 10)
                        .limit(countMax)
                        .toList();
                dbMaxResourceList.forEach(getResource -> resourceService.update(getResource.id(), priceMin));
                if (listDb.size() != 1) {
                    List<GetResource> dbMinResourceList = listDb.stream()
                            .filter(getResource -> getResource.ram() == 1)
                            .limit(countMin)
                            .toList();
                    dbMinResourceList.forEach(getResource -> resourceService.delete(getResource.id()));
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
            Price price = priceClient.getMax(ResourceType.VM);
            resourceService.add(price);
            return;
        }
        if ((vmRamLoad < MIN_OPTIMAL_LOAD || vmRamLoad > MAX_OPTIMAL_LOAD) && vmRamLoad != 0) {
            int vmRam = listVm.stream().mapToInt(GetResource::ram).sum();
            if (vmRam < optimalVm) {
                int count = optimalVm - vmRam;
                int countMax = count / 10;
                int countMin = count % 10;
                for (int i = 0; i < countMax; i++) {
                    Price price = priceClient.getMax(ResourceType.VM);
                    resourceService.add(price);
                }
                for (int i = 0; i < countMin; i++) {
                    Price price = priceClient.getMin(ResourceType.VM);
                    resourceService.add(price);
                }
            } else {
                int count = vmRam - optimalVm;
                int countMax = count / 10;
                int countMin = count % 10;
                Price priceMin = priceClient.getMin(ResourceType.VM);
                List<GetResource> vmMaxResourceList = listVm.stream()
                        .filter(getResource -> getResource.ram() == 10)
                        .limit(countMax)
                        .toList();
                vmMaxResourceList.forEach(getResource -> resourceService.update(getResource.id(), priceMin));
                if (listVm.size() != 1) {
                    List<GetResource> vmMinResourceList = listVm.stream()
                            .filter(getResource -> getResource.ram() == 1)
                            .limit(countMin)
                            .toList();
                    vmMinResourceList.forEach(getResource -> resourceService.delete(getResource.id()));
                }
            }
        }
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
        } else if (dbCpuLoad >= MAX_LOAD || dbRamLoad >= MAX_LOAD) {
            Price price = priceClient.getMax(ResourceType.DB);
            GetResource getResource = getMin(listDb);
            if (getResource == null) {
                resourceService.add(price);
            } else {
                resourceService.update(getResource.id(), price);
            }
        } else if (dbRamLoad <= MIN_DB_LOAD && !listDb.isEmpty()) {
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
        } else if (vmCpuLoad >= MAX_LOAD || vmRamLoad >= MAX_LOAD) {
            Price price = priceClient.getMax(ResourceType.VM);
            GetResource getResource = getMin(listVm);
            if (getResource == null) {
                resourceService.add(price);
            } else {
                resourceService.update(getResource.id(), price);
            }
        } else if (vmRamLoad <= MIN_VM_LOAD && !listVm.isEmpty()) {
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
