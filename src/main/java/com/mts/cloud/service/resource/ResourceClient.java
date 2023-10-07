package com.mts.cloud.service.resource;

import com.mts.cloud.CloudApplicationConst;
import com.mts.cloud.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ResourceClient {

    public static final String RESOURCE_PATH = "/resource";

    @Autowired
    public WebClient webClient;
    @Autowired
    public ApplicationProperties applicationProperties;

    public List<GetResource> get() {
        GetResource[] getResources = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE_PATH)
                        .queryParam(CloudApplicationConst.TOKEN, applicationProperties.token())
                        .build())
                .retrieve()
                .bodyToMono(GetResource[].class)
                .block();
        if (getResources == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(getResources).toList();
    }

    public PostResource post() {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE_PATH)
                        .queryParam(CloudApplicationConst.TOKEN, applicationProperties.token())
                        .build())
                .retrieve()
                .bodyToMono(PostResource.class)
                .block();

    }

    public void delete(long id) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE_PATH + "/{id}")
                        .queryParam(CloudApplicationConst.TOKEN, applicationProperties.token())
                        .build(id))
                .retrieve()
                .toBodilessEntity();
    }
}
