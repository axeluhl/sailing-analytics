package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;

public abstract class AbstractApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
implements ReverseProxy<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> {
    protected static final String INTERNAL_SERVER_STATUS = "internal-server-status";
    private final AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape;
    
    public AbstractApacheReverseProxy(AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape) {
        this.landscape = landscape;
    }

    protected AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return landscape;
    }

    @Override
    public String getHealthCheckPath() {
        return "/"+INTERNAL_SERVER_STATUS;
    }
}
