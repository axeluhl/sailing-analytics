package com.sap.sailing.landscape.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

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
            String serverDirectory, int telnetPort, String serverName) {
        super(port, host, serverDirectory, telnetPort, serverName);
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
     * @throws Exception 
     */
    @Override
    public String getServerName(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws TimeoutException, Exception {
        return Wait.wait(()->getStatus(optionalTimeout).get("servername").toString(),
                result->result!=null,
                /* retry on exception */ true,
                optionalTimeout,
                /* sleep between attempts */ Duration.ONE_SECOND.times(5),
                Level.INFO, "Waiting for server name");
    }

    @Override
    public Release getVersion(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        // TODO see getRelease TODO note... we should try to obtain this from the REST API /gwt/status where we need to spread the buildversion info across fine-grained attributes
        return getRelease(SailingReleaseRepository.INSTANCE, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
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
