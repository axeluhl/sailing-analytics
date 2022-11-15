package com.sap.sailing.landscape.gateway.impl;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsApplicationProcess;
import com.sap.sse.shared.json.JsonSerializer;

public class AwsApplicationProcessJsonSerializer<ShardingKey, MetricsT extends ApplicationProcessMetrics, ProcessT extends AwsApplicationProcess<ShardingKey, MetricsT, ProcessT>> implements JsonSerializer<ProcessT> {
    private static final Logger logger = Logger.getLogger(AwsApplicationProcessJsonSerializer.class.getName());

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String SERVER_DIRECTORY = "serverDirectory";

    @Override
    public JSONObject serialize(ProcessT object) {
        final JSONObject result = new JSONObject();
        result.put(HOST, new HostJsonSerializer<ShardingKey>().serialize(object.getHost()));
        result.put(PORT, object.getPort());
        try {
            result.put(SERVER_DIRECTORY, object.getServerDirectory(Landscape.WAIT_FOR_PROCESS_TIMEOUT));
        } catch (Exception e) {
            logger.warning("Unable to obtain server directory for host "+result.get(HOST)+":"+result.get(PORT)+": "+e.getMessage());
        }
        return result;
    }
}
