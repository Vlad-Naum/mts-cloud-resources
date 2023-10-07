package com.mts.cloud.service.price;

import com.mts.cloud.configuration.ResourceType;

public record Price(int id,
                    String name,
                    ResourceType type,
                    int cost,
                    int cpu,
                    int ram) {
}
