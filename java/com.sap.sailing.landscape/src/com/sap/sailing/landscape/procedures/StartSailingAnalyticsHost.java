package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;

public abstract class StartSailingAnalyticsHost<ShardingKey, 
                                                HostT extends SailingAnalyticsHost<ShardingKey>>
implements Procedure<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> {
    private final MachineImage<HostT> machineImage;
    private final Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape;
    
    public StartSailingAnalyticsHost(MachineImage<HostT> machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape) {
        super();
        this.machineImage = machineImage;
        this.landscape = landscape;
    }

    protected MachineImage<HostT> getMachineImage() {
        return machineImage;
    }

    @Override
    public Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> getLandscape() {
        return landscape;
    }
}
