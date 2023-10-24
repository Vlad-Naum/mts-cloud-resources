package com.mts.cloud.service.price;

import com.mts.cloud.configuration.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PriceService {

    @Autowired
    public PriceClient priceClient;

    public Price getPricesForDb(int optimalDb, int dbRam) {
        Price res = null;
        int count = optimalDb - dbRam;
        for (Price price : priceClient.prices.get(ResourceType.DB)) {
            if (price.ram() >= count) {
                if (res == null) {
                    res = price;
                    continue;
                }
                if (price.ram() > count) {
                    res = price;
                }
            }
        }
        return res;
    }

    public Price getPricesForVm(int optimalVm, int vmRam) {
        Price res = null;
        int count = optimalVm - vmRam;
        for (Price price : priceClient.prices.get(ResourceType.VM)) {
            if (price.ram() >= count) {
                if (res == null) {
                    res = price;
                    continue;
                }
                if (price.ram() > count) {
                    res = price;
                }
            }
        }
        return res;
    }

    public Price getMaxDb() {
        return priceClient.prices.get(ResourceType.DB).stream()
                .filter(price -> price.ram() == 8 && price.cpu() == 8)
                .findFirst()
                .get();
    }

    public Price getMaxVm() {
        return priceClient.prices.get(ResourceType.VM).stream()
                .filter(price -> price.ram() == 8 && price.cpu() == 4)
                .findFirst()
                .get();
    }
}
