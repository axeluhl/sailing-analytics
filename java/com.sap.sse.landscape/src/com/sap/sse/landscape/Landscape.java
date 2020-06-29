package com.sap.sse.landscape;

import java.util.Map;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;
import com.sap.sse.landscape.application.Scope;

public interface Landscape<ShardingKey, MetricsT extends ApplicationProcessMetrics> {
    /**
     * Tells which scope currently lives where
     */
    Map<Scope<ShardingKey>, ApplicationReplicaSet<ShardingKey, MetricsT>> getScopes();
    
    /**
     * Launches a new {@link Host} from a given image into the availability zone specified and controls
     * network access to that instance by setting the security groups specified for the resulting host.
     */
    Host launchHost(MachineImage fromImage, AvailabilityZone az, Iterable<SecurityGroup> securityGroups);
}
