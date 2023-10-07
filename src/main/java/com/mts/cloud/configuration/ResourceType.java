package com.mts.cloud.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ResourceType {
    @JsonProperty("db")
    DB,
    @JsonProperty("vm")
    VM
}
