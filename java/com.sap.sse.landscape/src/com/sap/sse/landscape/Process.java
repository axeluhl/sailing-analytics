package com.sap.sse.landscape;

public interface Process<LogT extends Log, MetricsT extends Metrics> {
    int getPort();
    Host getHost();
    LogT getLog();
    MetricsT getMetrics();
    boolean isAvailable();
}
