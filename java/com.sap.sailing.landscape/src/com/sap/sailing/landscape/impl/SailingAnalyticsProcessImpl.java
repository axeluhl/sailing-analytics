package com.sap.sailing.landscape.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.json.simple.parser.ParseException;

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
    private Integer expeditionUdpPort;
    
    /**
     * Tries to obtain the port from the {@code env.sh} file found in the {@code serverDirectory}
     * 
     * @param optionalKeyName
     *            the name of the SSH key pair to use to log on; must identify a key pair available for the
     *            {@link #getRegion() region} of this instance. If not provided, the the SSH private key for the key
     *            pair that was originally used when the instance was launched will be used.
     */
    public SailingAnalyticsProcessImpl(Host host, String serverDirectory, Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        super(host, serverDirectory, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
    }
    
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
    
    private JSONObject getStatus(Optional<Duration> optionalTimeout) throws IOException, ParseException {
        final HttpGet getStatusRequest = new HttpGet(getHealthCheckUrl(optionalTimeout).toString());
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpResponse result = client.execute(getStatusRequest);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.getEntity().writeTo(bos);
        return (JSONObject) (new JSONParser().parse(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()))));
    }

    /**
     * Here we assume that {@code /gwt/status} has a "release" field we can query
     */
    @Override
    public Release getRelease(ReleaseRepository releaseRepository, Optional<Duration> optionalTimeout,
            Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws Exception {
        final JSONObject status = getStatus(optionalTimeout);
        return new ReleaseImpl((String) status.get("release"), releaseRepository);
    }
    
    /**
     * For a sailing application process we know that there is a {@code /gwt/status} end point from which much
     * information about server name as well as availability and replication status can be obtained.
     */
    @Override
    public String getServerName(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws TimeoutException, Exception {
        if (serverName == null) {
            serverName = Wait.wait(()->getStatus(optionalTimeout).get("servername").toString(),
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
    public TimePoint getStartTimePoint(Optional<Duration> optionalTimeout) throws IOException, ParseException, java.text.ParseException {
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
