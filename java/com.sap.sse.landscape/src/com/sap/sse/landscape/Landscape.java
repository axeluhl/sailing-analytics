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
}
