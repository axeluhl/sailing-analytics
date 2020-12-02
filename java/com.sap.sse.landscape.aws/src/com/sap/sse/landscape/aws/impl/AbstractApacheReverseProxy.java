package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.RotatingFileBasedLog;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.ReverseProxy;

public abstract class AbstractApacheReverseProxy<ShardingKey, MetricsT extends ApplicationProcessMetrics,
ProcessT extends ApplicationProcess<ShardingKey, MetricsT, ProcessT>>
implements ReverseProxy<ShardingKey, MetricsT, ProcessT, RotatingFileBasedLog> {
    private final AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape;
    
    public AbstractApacheReverseProxy(AwsLandscape<ShardingKey, MetricsT, ProcessT> landscape) {
        this.landscape = landscape;
    }

    protected AwsLandscape<ShardingKey, MetricsT, ProcessT> getLandscape() {
        return landscape;
    }

    @Override
    public String getHealthCheckPath() {
        return "/internal-server-status";
    }

    @Override
    public void createInternalStatusRedirect() {
        // TODO make this a script in java/target, just like defineReverseProxyMappings.sh and call from here and from within sailing init.d script?
//      # Append Apache macro invocation for /internal-server-status based on mod_status and INSTANCE_DNS to "${APACHE_INTERNALS_CONFIG_FILE}"
//      echo "Appending macro usage for $INSTANCE_DNS/internal-server-status URL for mod_status based Apache monitoring to ${APACHE_INTERNALS_CONFIG_FILE}" >>/var/log/sailing.err
//      echo "## SERVER STATUS" >>"${APACHE_INTERNALS_CONFIG_FILE}"
//      echo "Use Status $INSTANCE_DNS internal-server-status" >>"${APACHE_INTERNALS_CONFIG_FILE}"
        
    }
}
