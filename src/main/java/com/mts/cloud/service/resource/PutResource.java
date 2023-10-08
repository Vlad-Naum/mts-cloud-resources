package com.mts.cloud.service.resource;

import com.mts.cloud.configuration.ResourceType;

public record PutResource(int cpu, int ram, ResourceType type) {
}
