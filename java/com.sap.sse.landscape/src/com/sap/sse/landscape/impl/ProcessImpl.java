package com.sap.sse.landscape.impl;

import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Log;
import com.sap.sse.landscape.Process;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;

public abstract class ProcessImpl<LogT extends Log, MetricsT extends ApplicationProcessMetrics> implements Process<LogT, MetricsT> {
    private final int port;
    private final Host host;
    
    public ProcessImpl(int port, Host host) {
        super();
        this.port = port;
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Host getHost() {
        return host;
    }

    /**
     * By default the process doesn't have a log.
     */
    @Override
    public LogT getLog() {
        return null;
    }

    /**
     * By default, the process doesn't expose any metrics.
     */
    @Override
    public MetricsT getMetrics() {
        return null;
    }
}
