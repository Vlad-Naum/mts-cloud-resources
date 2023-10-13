package com.mts.cloud.service.statistic;

public record Statistic(int dbCpu,
                        double dbCpuLoad,
                        int dbRam,
                        double dbRamLoad,
                        int responseTime,
                        int vmCpu,
                        double vmCpuLoad,
                        int vmRam,
                        double vmRamLoad,
                        int requests) {

    public static final String DB_CPU = "db_cpu";
    public static final String DB_CPU_LOAD = "db_cpu_load";
    public static final String DB_RAM = "db_ram";
    public static final String DB_RAM_LOAD = "db_ram_load";
    public static final String VM_CPU = "vm_cpu";
    public static final String VM_CPU_LOAD = "vm_cpu_load";
    public static final String VM_RAM = "vm_ram";
    public static final String VM_RAM_LOAD = "vm_ram_load";
    public static final String RESPONSE_TIME = "response_time";
    public static final String REQUESTS = "requests";
}
