package com.sap.sailing.landscape;

import java.util.Optional;
import java.util.logging.Logger;

import com.sap.sse.common.Duration;
import com.sap.sse.landscape.application.ApplicationProcess;

public interface SailingAnalyticsProcess<ShardingKey> extends ApplicationProcess<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> {
    static Logger logger = Logger.getLogger(SailingAnalyticsProcess.class.getName());
    static String HEALTH_CHECK_PATH = "/gwt/status";

    int getExpeditionUdpPort(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception;
}
