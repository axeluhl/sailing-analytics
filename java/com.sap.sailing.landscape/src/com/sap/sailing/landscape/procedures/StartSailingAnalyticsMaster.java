package com.sap.sailing.landscape.procedures;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;

public class StartSailingAnalyticsMaster<ShardingKey> extends StartSailingAnalyticsHost<ShardingKey, SailingAnalyticsHost<ShardingKey>> {
    public StartSailingAnalyticsMaster(MachineImage<SailingAnalyticsHost<ShardingKey>> machineImage,
            Landscape<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> landscape) {
        super(machineImage, landscape);
    }
}
