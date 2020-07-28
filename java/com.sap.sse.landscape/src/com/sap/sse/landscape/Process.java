package com.sap.sse.landscape;

public interface Process<LogT extends Log, MetricsT extends Metrics> {
    /**
     * The TCP port through which this process is typically accessed. For example, a MongoDB
     * would default this to 27017; an HTTP server would be using 80.
     */
    int getPort();
    
    /**
     * The host that this process is running on
     */
    Host getHost();
    
    /**
     * Grants access to the log that this process produces
     */
    LogT getLog();
    
    MetricsT getMetrics();
    
    boolean isAvailable();
}
