package com.sap.sailing.landscape.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsProcessConfigurationVariable;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;

public class SailingAnalyticsProcessImpl<ShardingKey> extends ApplicationProcessImpl<ShardingKey, SailingAnalyticsMetrics,
SailingAnalyticsMaster<ShardingKey>, SailingAnalyticsReplica<ShardingKey>> implements SailingAnalyticsProcess<ShardingKey> {
    private static final String HEALTH_CHECK_PATH = "/gwt/status";

    public SailingAnalyticsProcessImpl(int port, Host host) {
        super(port, host, "/home/sailing/servers/server");
    }
    
    @Override
    public String getHealthCheckPath() {
        return HEALTH_CHECK_PATH;
    }
    
    private URL getHealthCheckUrl(Optional<Duration> optionalTimeout) throws MalformedURLException {
        return new URL("http", getHost().getPublicAddress(optionalTimeout).getCanonicalHostName(), getPort(), getHealthCheckPath());
    }

    @Override
    public boolean isReady(Optional<Duration> optionalTimeout) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) getHealthCheckUrl(optionalTimeout).openConnection();
        return connection.getResponseCode() == 200;
    }

    @Override
    public int getExpeditionUdpPort(Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, InterruptedException, SftpException {
        return Integer.parseInt(getEnvShValueFor(getEnvSh(optionalTimeout), SailingAnalyticsProcessConfigurationVariable.EXPEDITION_PORT.name(), optionalTimeout));
    }
}
