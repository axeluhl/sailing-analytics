package com.sap.sse.landscape.orchestration;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Landscape;
import com.sap.sse.landscape.MachineImage;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;

public abstract class StartHost<ShardingKey,
                       MetricsT extends ApplicationProcessMetrics,
                       MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                       ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
                       HostT extends Host>
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private final MachineImage<HostT> machineImage;
    private final Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape;
    
    public StartHost(MachineImage<HostT> machineImage,
            Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super();
        this.machineImage = machineImage;
        this.landscape = landscape;
    }

    @Override
    public Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape() {
        return landscape;
    }

    protected MachineImage<HostT> getMachineImage() {
        return machineImage;
    }
}
