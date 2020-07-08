package com.sap.sse.landscape.aws.reverseproxy;

import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaSet;

public interface ApacheReverseProxy {
    <ShardingKey> void setHomeRedirect(String hostname, ApplicationReplicaSet<ShardingKey, ApplicationProcessMetrics> applicationReplicaSet);
}