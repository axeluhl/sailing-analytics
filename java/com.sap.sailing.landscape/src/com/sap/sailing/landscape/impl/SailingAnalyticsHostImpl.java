package com.sap.sailing.landscape.impl;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.aws.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;

public class SailingAnalyticsHostImpl<ShardingKey> extends AwsInstanceImpl<ShardingKey, SailingAnalyticsMetrics> implements SailingAnalyticsHost<ShardingKey> {
    public SailingAnalyticsHostImpl(String instanceId, AwsAvailabilityZone availabilityZone,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, ?, ?> landscape) {
        super(instanceId, availabilityZone, landscape);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ReverseProxy<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>, RotatingFileBasedLog> getReverseProxy() {
        // TODO Implement SailingAnalyticsHost<ShardingKey>.getReverseProxy(...)
        return null;
    }

    @Override
    public Iterable<SailingAnalyticsProcess<ShardingKey>> getSailingAnalyticsProcesses() {
        // TODO Implement SailingAnalyticsHost<ShardingKey>.getSailingAnalyticsProcesses(...)
        return null;
    }
}
