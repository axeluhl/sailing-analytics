package com.sap.sailing.landscape.gateway.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sse.landscape.aws.AwsApplicationReplicaSet;
import com.sap.sse.shared.json.JsonSerializer;

public class AwsApplicationReplicaSetJsonSerializer implements JsonSerializer<AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>>> {
    @Override
    public JSONObject serialize(
            AwsApplicationReplicaSet<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> object) {
        // TODO Auto-generated method stub
        return null;
    }
}
