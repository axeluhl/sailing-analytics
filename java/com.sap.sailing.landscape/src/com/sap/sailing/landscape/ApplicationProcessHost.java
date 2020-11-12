package com.sap.sailing.landscape;

import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.Scope;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.ReverseProxy;

public interface ApplicationProcessHost<ShardingKey, MetricsT extends ApplicationProcessMetrics> extends AwsInstance<ShardingKey, MetricsT> {
    String DEFAULT_SERVERS_PATH = "/home/sailing/servers";
    
    String DEFAULT_SERVER_DIRECTORY_NAME = "server";
    
    String DEFAULT_SERVER_PATH = DEFAULT_SERVERS_PATH+"/"+DEFAULT_SERVER_DIRECTORY_NAME;
    
    /**
     * Obtains an object through which an Apache reverse proxy running on this sailing analytics host can be configured.
     * It is mainly used to decide how to route based on a URL's hostname or other {@link Scope} identification, and to
     * expand base URLs to, e.g., the URL of a specific event in that scope or an overview page of an event series or
     * simply the home/landing page. It furthermore handles logging in a consistent way.
     */
    ReverseProxy<ShardingKey, MetricsT, RotatingFileBasedLog> getReverseProxy();
    
    /**
     * Obtains the Sailing Analytics processes running on this host. Can be zero or more.
     */
    Iterable<ApplicationProcess<ShardingKey, MetricsT>> getApplicationProcesses();
}
