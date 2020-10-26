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
extends AbstractProcedureImpl<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>
implements Procedure<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> {
    private final MachineImage machineImage;
    
    public StartHost(MachineImage machineImage,
            Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> landscape) {
        super(landscape);
        this.machineImage = machineImage;
    }

    protected MachineImage getMachineImage() {
        return machineImage;
    }
}
