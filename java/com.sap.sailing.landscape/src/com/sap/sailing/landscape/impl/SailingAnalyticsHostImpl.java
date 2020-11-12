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
import com.sap.sse.landscape.aws.impl.ApacheReverseProxy;
import com.sap.sse.landscape.aws.impl.AwsInstanceImpl;

public class SailingAnalyticsHostImpl<ShardingKey> extends AwsInstanceImpl<ShardingKey, SailingAnalyticsMetrics> implements SailingAnalyticsHost<ShardingKey> {
    public SailingAnalyticsHostImpl(String instanceId, AwsAvailabilityZone availabilityZone,
            AwsLandscape<ShardingKey, SailingAnalyticsMetrics, ?, ?> landscape) {
        super(instanceId, availabilityZone, landscape);
    }
    
    @Override
    protected AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> getLandscape() {
        @SuppressWarnings("unchecked")
        final AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> castLandscape =
            (AwsLandscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>>) super.getLandscape();
        return castLandscape;
    }

    @Override
    public ReverseProxy<ShardingKey, SailingAnalyticsMetrics, RotatingFileBasedLog> getReverseProxy() {
        return new ApacheReverseProxy<>(getLandscape(), this);
    }

    /**
     * The implementation scans the {@link SailingAnalyticsHost#DEFAULT_SERVERS_PATH application server deployment
     * folder} for sub-folders. In those sub-folders, the configuration file is analyzed
     */
    @Override
    public Iterable<SailingAnalyticsProcess<ShardingKey>> getSailingAnalyticsProcesses() {
        // TODO Implement SailingAnalyticsHost<ShardingKey>.getSailingAnalyticsProcesses(...)
        return null;
    }
}
