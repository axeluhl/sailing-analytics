package com.sap.sailing.landscape;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationProcess;

public interface SailingAnalyticsProcess<ShardingKey> extends ApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    static Logger logger = Logger.getLogger(SailingAnalyticsProcess.class.getName());
    static String HEALTH_CHECK_PATH = "/gwt/status";

    @Override
    default String getHealthCheckPath() {
        return HEALTH_CHECK_PATH;
    }
    
    @Override
    default boolean isReady(Optional<Duration> optionalTimeout) throws IOException {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL("http",
                    getHost().getPublicAddress(optionalTimeout).getCanonicalHostName(), getPort(), getHealthCheckPath())
                            .openConnection();
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            logger.info("Ready-check failed for "+this+": "+e.getMessage());
            return false;
        }
    }

    default int getExpeditionUdpPort(Optional<Duration> optionalTimeout) throws NumberFormatException, JSchException, IOException, InterruptedException, SftpException {
        return Integer.parseInt(getEnvShValueFor(SailingAnalyticsProcessConfigurationVariable.EXPEDITION_PORT.name(), optionalTimeout));
    }
}
