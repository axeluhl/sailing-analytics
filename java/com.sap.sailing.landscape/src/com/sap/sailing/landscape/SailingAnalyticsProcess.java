package com.sap.sailing.landscape;

import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationProcess;

public interface SailingAnalyticsProcess<ShardingKey> extends ApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    static Logger logger = Logger.getLogger(SailingAnalyticsProcess.class.getName());
    static String HEALTH_CHECK_PATH = "/gwt/status";

    default int getExpeditionUdpPort(Optional<Duration> optionalTimeout, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        return Integer.parseInt(getEnvShValueFor(SailingAnalyticsProcessConfigurationVariable.EXPEDITION_PORT.name(),
                optionalTimeout, privateKeyEncryptionPassphrase));
    }
}
