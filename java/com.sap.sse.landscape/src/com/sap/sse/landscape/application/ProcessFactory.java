package com.sap.sse.landscape.application;

import java.util.Map;

import com.sap.sse.landscape.Host;

@FunctionalInterface
public interface ProcessFactory<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends Host> {
    ProcessT createProcess(HostT host, int port, String serverDirectory, int telnetPort, String serverName,
            Map<String, Object> additionalProperties);
}
