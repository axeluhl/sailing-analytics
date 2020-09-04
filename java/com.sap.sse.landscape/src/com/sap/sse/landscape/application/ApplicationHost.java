package com.sap.sse.landscape.application;

import com.sap.sse.landscape.Host;

/**
 * A host that is capable of launching {@link ApplicationProcess}es.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface ApplicationHost extends Host {
    /**
     * Launches an application on this host, using the configuration parameters requested. Throws an exception
     * in case the configuration is incompatible with the host, for example, if it requests a port to be used
     * that is already taken.
     */
    <ShardingKey, MetricsT extends ApplicationProcessMetrics> ApplicationProcess<ShardingKey, MetricsT> launchApplication(ApplicationProcessConfiguration<ShardingKey, MetricsT> configuration);
}
