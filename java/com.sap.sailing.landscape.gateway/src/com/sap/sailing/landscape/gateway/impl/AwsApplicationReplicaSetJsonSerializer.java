package com.sap.sailing.landscape.gateway.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.shared.json.JsonSerializer;

public class AwsApplicationReplicaSetJsonSerializer implements JsonSerializer<AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>>> {
    public static String NAME = "name";
    public static String VERSION = "version";
    private final String releaseName;
    
    public AwsApplicationReplicaSetJsonSerializer(String releaseName) {
        this.releaseName = releaseName;
    }
    
    @Override
    public JSONObject serialize(
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> replicaSet) {
        final JSONObject result = new JSONObject();
        result.put(NAME, replicaSet.getName());
        result.put(VERSION, releaseName);
        return result;
    }
}
