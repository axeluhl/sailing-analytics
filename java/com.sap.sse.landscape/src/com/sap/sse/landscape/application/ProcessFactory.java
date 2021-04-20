package com.sap.sse.landscape.application;

import java.util.Map;

import com.sap.sse.landscape.Host;

@FunctionalInterface
public interface ProcessFactory<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>, HostT extends Host> {
    /**
     * @param telnetPort
     *            optional; if not provided, it will be discovered by SSH from the process's environment variables (time
     *            consuming) upon request
     * @param additionalProperties
     *            can be used to pass through values required for calling the {@link ApplicationProcess} subclass
     *            constructor which may require more arguments than the ones that appear as fixed arguments on this
     *            method.
     */
    ProcessT createProcess(HostT host, int port, String serverDirectory, Integer telnetPort, String serverName,
            Map<String, Object> additionalProperties);
}
