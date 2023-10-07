package com.mts.cloud.service.resource;

import com.mts.cloud.configuration.ResourceType;

public record PostResource(int cpu,
                           int ram,
                           ResourceType type) {
}
