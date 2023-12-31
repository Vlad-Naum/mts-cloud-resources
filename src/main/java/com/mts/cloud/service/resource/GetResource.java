package com.mts.cloud.service.resource;

import com.mts.cloud.configuration.ResourceType;

public record GetResource(long id,
                          int cost,
                          int cpu,
                          float cpuLoad,
                          boolean failed,
                          int ram,
                          float ramLoad,
                          ResourceType type) {

}

