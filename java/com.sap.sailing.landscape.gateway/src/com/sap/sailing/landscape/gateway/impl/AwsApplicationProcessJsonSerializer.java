package com.sap.sailing.landscape.gateway.impl;

import org.json.simple.JSONObject;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsApplicationProcess;
import com.sap.sse.shared.json.JsonSerializer;

public class AwsApplicationProcessJsonSerializer<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends AwsApplicationProcess<ShardingKey, MetricsT, ProcessT>> implements JsonSerializer<ProcessT> {
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String SERVER_DIRECTORY = "serverDirectory";

    @Override
    public JSONObject serialize(ProcessT object) {
        final JSONObject result = new JSONObject();
        result.put(HOST, new HostJsonSerializer().serialize(object.getHost()));
        result.put(PORT, object.getPort());
        result.put(SERVER_DIRECTORY, object.getServerDirectory());
        return result;
    }
}
