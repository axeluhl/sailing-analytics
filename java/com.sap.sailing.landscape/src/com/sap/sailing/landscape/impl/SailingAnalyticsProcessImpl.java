package com.sap.sailing.landscape.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingAnalyticsProcessConfigurationVariable;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.ReleaseRepository;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.impl.ReleaseImpl;
import com.sap.sse.util.Wait;

public class SailingAnalyticsProcessImpl<ShardingKey>
extends ApplicationProcessImpl<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
implements SailingAnalyticsProcess<ShardingKey> {
    private static final String STATUS_SERVERNAME_PROPERTY_NAME = "servername";
    private static final String STATUS_RELEASE_PROPERTY_NAME = "release";
    private Integer expeditionUdpPort;
    
    public SailingAnalyticsProcessImpl(int port, Host host, String serverDirectory, Integer expeditionUdpPort) {
        super(port, host, serverDirectory);
        this.expeditionUdpPort = expeditionUdpPort;
    }

    public SailingAnalyticsProcessImpl(int port,
            ApplicationProcessHost<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>> host,
            String serverDirectory, int telnetPort, String serverName, Integer expeditionUdpPort) {
        super(port, host, serverDirectory, telnetPort, serverName);
        this.expeditionUdpPort = expeditionUdpPort;
    }

    @Override
    public String getHealthCheckPath() {
        return HEALTH_CHECK_PATH;
    }
    
    private JSONObject getStatus(Optional<Duration> optionalTimeout) throws TimeoutException, Exception {
        final HttpGet getStatusRequest = new HttpGet(getHealthCheckUrl(optionalTimeout).toString());
        return Wait.wait(()->{
                    final HttpClient client = HttpClientBuilder.create().build();
                    final HttpResponse result = client.execute(getStatusRequest);
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    result.getEntity().writeTo(bos);
                    return (JSONObject) (new JSONParser().parse(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()))));
                }, json->json != null, /* retryOnException */ true, optionalTimeout,
                /* sleepBetweenAttempts */ Duration.ONE_SECOND.times(5), Level.INFO, "getStatus() on "+getHost()+":"+getPort());
    }

    /**
     * Here we assume that {@code /gwt/status} has a "release" field we can query
     */
    @Override
    public Release getRelease(ReleaseRepository releaseRepository, Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        final JSONObject status = getStatus(optionalTimeout);
        final Release result;
        if (status.containsKey(STATUS_RELEASE_PROPERTY_NAME)) {
            result = new ReleaseImpl((String) status.get(STATUS_RELEASE_PROPERTY_NAME), releaseRepository);
        } else {
            // for backward compatibility
            result = super.getRelease(releaseRepository, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
        }
        return result;
    }
    
    /**
     * For a sailing application process we know that there is a {@code /gwt/status} end point from which much
     * information about server name as well as availability and replication status can be obtained.
     */
    @Override
    public String getServerName(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws TimeoutException, Exception {
        if (serverName == null) {
            serverName = Wait.wait(()->getStatus(optionalTimeout).get(STATUS_SERVERNAME_PROPERTY_NAME).toString(),
                    result->result!=null,
                    /* retry on exception */ true,
                    optionalTimeout,
                    /* sleep between attempts */ Duration.ONE_SECOND.times(5),
                    Level.INFO, "Waiting for server name");
        }
        return serverName;
    }

    @Override
    public Release getVersion(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return getRelease(SailingReleaseRepository.INSTANCE, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
    @Override
    public TimePoint getStartTimePoint(Optional<Duration> optionalTimeout) throws TimeoutException, Exception {
        final TimePoint result;
        final JSONObject status = getStatus(optionalTimeout);
        final Number startTimeMillis = (Number) status.get("start_time_millis");
        if (startTimeMillis == null) {
            // try legacy approach: extract from "buildversion" attribute which has the general format "^.* Started: [0-9]+$"
            // where the "Started" value has format yyyyMMddhhmm, usually provided in UTC
            final String buildversion = (String) status.get("buildversion");
            final Pattern buildversionPattern = Pattern.compile("^.* Started: ([0-9]+)$");
            final Matcher matcher = buildversionPattern.matcher(buildversion);
            if (buildversion != null && matcher.matches()) {
                final String timestamp = matcher.group(1);
                result = TimePoint.of(new SimpleDateFormat("yyyyMMddhhmmX").parse(timestamp+"Z"));
            } else {
                result = null;
            }
        } else {
            result = startTimeMillis == null ? null : TimePoint.of(startTimeMillis.longValue());
        }
        return result;
    }

    @Override
    public int getExpeditionUdpPort(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        if (expeditionUdpPort == null) {
            expeditionUdpPort = Integer.parseInt(getEnvShValueFor(SailingAnalyticsProcessConfigurationVariable.EXPEDITION_PORT.name(),
                optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase));
        }
        return expeditionUdpPort;
    }
}
