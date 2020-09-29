package com.sap.sailing.landscape;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.ReverseProxy;

public interface SailingAnalyticsHost<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends AwsInstance<ShardingKey, MetricsT> {
    /**
     * Obtains an object through which an Apache reverse proxy running on this sailing analytics host can be configured.
     * It is mainly used to decide how to route based on a URL's hostname or other {@link Scope} identification, and to
     * expand base URLs to, e.g., the URL of a specific event in that scope or an overview page of an event series or
     * simply the home/landing page. It furthermore handles logging in a consistent way.
     */
    ReverseProxy<ShardingKey, MetricsT, SailingAnalyticsMaster<ShardingKey, MetricsT>, SailingAnalyticsReplica<ShardingKey, MetricsT>> getReverseProxy();
    
    /**
     * Obtains the Sailing Analytics processes running on this host. Can be zero or more.
     */
    Iterable<SailingAnalyticsProcess<ShardingKey, MetricsT>> getSailingAnalyticsProcesses();
}
