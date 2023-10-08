package com.mts.cloud.service.statistic;

public record Statistic(double dbCpuLoad,
                        double dbRamLoad,
                        int responseTime,
                        double vmCpuLoad,
                        double vmRamLoad) {

    public static final String DB_CPU_LOAD = "db_cpu_load";
    public static final String DB_RAM_LOAD = "db_ram_load";
    public static final String VM_CPU_LOAD = "vm_cpu_load";
    public static final String VM_RAM_LOAD = "vm_ram_load";
    public static final String RESPONSE_TIME = "response_time";
}
