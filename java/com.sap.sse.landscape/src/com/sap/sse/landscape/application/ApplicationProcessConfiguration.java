package com.sap.sse.landscape.application;

import java.io.Serializable;
import java.util.Map;

import com.sap.sse.common.WithID;
import com.sap.sse.landscape.mongodb.Database;

/**
 * The configuration parameters required when launching an {@link ApplicationProcess}. Some configuration
 * properties may be left unspecified which allows the {@link ApplicationHost} to choose its own defaults.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationProcessConfiguration<ShardingKey, MetricsT extends ApplicationProcessMetrics> {
    ApplicationVersion getVersion();
    
    /**
     * The database that the process connects to is specific to the process instance. Each process in a replica set must
     * connect to a different database.
     */
    Database getDatabase();
    
    int getTelnetPortToOSGiConsole();
    
    int getUdpPortForSensorInput();

    long getHeapSizeInBytes();
    
    /**
     * The name of the RabbitMQ "Exchange" to which the {@link ApplicationProcess} configured by this object will send its
     * outbound replication operations as soon as one or more replicas have registered.
     */
    String getOutboundReplicationExchangeName();
    
    /**
     * Specifies which replicable services shall be replicated from which master server when launching an application
     * process from this configuration. The keys are the {@link WithID#getId() IDs} of the {@code Replicable} objects,
     * the values tell the master server process from which to replicate and with it (in the
     * {@link ApplicationMasterProcess#getEffectiveConfiguration() effective configuration parameters) the
     * {@link ApplicationProcessConfiguration#getOutboundReplicationExchangeName() RabbitMQ Exchange name} to connect
     * to. When a non-{@code null}, non-empty result is returned by this method, the application process created from
     * this configuration will be an {@link ApplicationReplicaProcess} whereas otherwise it will produce an
     * {@link ApplicationMasterProcess}.
     */
    Map<Serializable, ApplicationMasterProcess<ShardingKey, MetricsT>> getMasterToReplicateServiceFrom();
}
