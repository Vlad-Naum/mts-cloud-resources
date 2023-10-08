package com.mts.cloud.service.resource;

import com.mts.cloud.configuration.ResourceType;
import com.mts.cloud.service.price.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    @Autowired
    public ResourceClient resourceClient;


    public List<GetResource> get() {
        return resourceClient.get();
    }

    public void add(Price price) {
        resourceClient.post(new PostResource(price.cpu(), price.ram(), price.type()));
    }

    public void update(long id, Price price) {
        resourceClient.put(id, new PutResource(price.cpu(), price.ram(), price.type()));
    }

    public void delete(ResourceType type) {
        List<GetResource> getResources = resourceClient.get();
        GetResource getResource = getResources.stream()
                .filter(res -> res.type().equals(type))
                .findFirst()
                .orElse(null);
        if (getResource == null) {
            return;
        }
        resourceClient.delete(getResource.id());
    }

    public void delete(long id) {
        resourceClient.delete(id);
    }
}
