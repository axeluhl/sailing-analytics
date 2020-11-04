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
    
    /**
     * A builder that helps building an instance of type {@link StartHost} or any subclass thereof (then using
     * specialized builders).
     * 
     * @author Axel Uhl (D043530)
     */
    public static interface Builder<T extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey,
    MetricsT extends ApplicationProcessMetrics,
    MasterProcessT extends ApplicationMasterProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    ReplicaProcessT extends ApplicationReplicaProcess<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT>,
    HostT extends Host> {
        T build();

        MachineImage getMachineImage();
        
        Landscape<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT> getLandscape();
    }
    
    protected StartHost(Builder<? extends StartHost<ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT>, ShardingKey, MetricsT, MasterProcessT, ReplicaProcessT, HostT> builder) {
        super(builder.getLandscape());
        this.machineImage = builder.getMachineImage();
    }

    protected MachineImage getMachineImage() {
        return machineImage;
    }
}
