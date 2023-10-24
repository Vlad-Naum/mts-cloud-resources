package com.mts.cloud.service;

import com.mts.cloud.configuration.ApplicationProperties;
import com.mts.cloud.configuration.ResourceType;
import com.mts.cloud.service.price.Price;
import com.mts.cloud.service.price.PriceClient;
import com.mts.cloud.service.resource.GetResource;
import com.mts.cloud.service.resource.ResourceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class PreInstalledService implements InitializingBean {

    @Autowired
    public ApplicationProperties applicationProperties;
    @Autowired
    public ResourceService resourceService;
    @Autowired
    public PriceClient priceClient;

    @Override
    public void afterPropertiesSet() throws Exception {
//        List<GetResource> resources = resourceService.get();
//        resources.forEach(resource -> resourceService.delete(resource.id()));
//
//        int minVm = applicationProperties.minVm();
//        Price priceVm = priceClient.getMax(ResourceType.VM);
//        IntStream.range(0, minVm).forEach(value -> resourceService.add(priceVm));
//
//        int minDb = applicationProperties.minDb();
//        Price priceDb = priceClient.getMax(ResourceType.DB);
//        IntStream.range(0, minDb).forEach(value -> resourceService.add(priceDb));
    }
}
