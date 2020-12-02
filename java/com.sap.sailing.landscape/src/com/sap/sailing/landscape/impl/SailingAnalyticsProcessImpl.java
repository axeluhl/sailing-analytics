package com.sap.sailing.landscape.impl;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;

public class SailingAnalyticsProcessImpl<ShardingKey>
extends ApplicationProcessImpl<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
implements SailingAnalyticsProcess<ShardingKey> {
    /**
     * Tries to obtain the port from the {@code env.sh} file found in the {@code serverDirectory}
     */
    public SailingAnalyticsProcessImpl(Host host, String serverDirectory)
            throws NumberFormatException, JSchException, IOException, InterruptedException {
        super(host, serverDirectory);
    }
    
    public SailingAnalyticsProcessImpl(int port, Host host, String serverDirectory) {
        super(port, host, serverDirectory);
    }

    @Override
    public String getHealthCheckPath() {
        return HEALTH_CHECK_PATH;
    }
}
