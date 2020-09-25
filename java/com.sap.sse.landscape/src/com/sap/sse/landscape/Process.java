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
    
    /**
     * Tells whether this process is still alive and will at some point (again)
     * become {@link #isReady() ready} to accept requests. An example of a process {@link #isAlive() alive}
     * but not {@link #isReady() ready} would be a replica process that has started to receive the initial
     * load from its master. It can answer in a well-defined way to health check / status requests, but you
     * shouldn't route regular traffic to it yet. 
     */
    boolean isAlive();
    
    /**
     * Tells whether this process is ready to accept requests. Use this for a health check in a target group
     * that decides whether traffic will be sent to this process. {@link #isReady()} implies {@link #isAlive()}.
     */
    boolean isReady();
}
