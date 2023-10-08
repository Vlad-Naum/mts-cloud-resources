package com.mts.cloud.service.resource;

import com.mts.cloud.CloudApplicationConst;
import com.mts.cloud.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ResourceClient {

    private static final Logger log = LoggerFactory.getLogger(ResourceClient.class);
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

    public void post(PostResource postResource) {
        ResponseEntity<Void> bodilessEntity = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE_PATH)
                        .queryParam(CloudApplicationConst.TOKEN, applicationProperties.token())
                        .build())
                .bodyValue(postResource)
                .retrieve()
                .toBodilessEntity()
                .block();
        HttpStatusCode statusCode = bodilessEntity.getStatusCode();
        log.debug("post resource type: {}, statusCode: {}", postResource.type(), statusCode);
    }

    public void delete(long id) {
        ResponseEntity<Void> block = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE_PATH + "/{id}")
                        .queryParam(CloudApplicationConst.TOKEN, applicationProperties.token())
                        .build(id))
                .retrieve()
                .toBodilessEntity()
                .block();
        log.debug("delete resource statusCode: {}", block.getStatusCode());
    }
}
