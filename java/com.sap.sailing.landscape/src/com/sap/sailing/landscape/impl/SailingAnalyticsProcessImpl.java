package com.sap.sailing.landscape.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.Host;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;

public class SailingAnalyticsProcessImpl<ShardingKey>
extends ApplicationProcessImpl<ShardingKey, SailingAnalyticsMetrics, SailingAnalyticsProcess<ShardingKey>>
implements SailingAnalyticsProcess<ShardingKey> {
    private final static Logger logger = Logger.getLogger(SailingAnalyticsProcessImpl.class.getName());

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
    
    public SailingAnalyticsProcessImpl(int port, Host host, String serverDirectory) {
        super(port, host, serverDirectory);
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
     * For a sailing application process we know that there is a {@code /gwt/status} end point from which much
     * information about server name as well as availability and replication status can be obtained.
     */
    @Override
    public String getServerName(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase)
            throws JSchException, IOException, InterruptedException, SftpException, ParseException {
        String result = null;
        final TimePoint start = TimePoint.now();
        while (result == null && optionalTimeout.map(d->start.until(TimePoint.now()).compareTo(d) < 0).orElse(true)) {
            try {
                result = getStatus(optionalTimeout).get("servername").toString();
            } catch (Exception e) {
                logger.info("Exception waiting for server name."+optionalTimeout.map(d->" Waiting another "+d.minus(start.until(TimePoint.now())).toString()).orElse(""));
                Thread.sleep(Duration.ONE_SECOND.times(5).asMillis());
            }
        }
        return result;
    }

    @Override
    public Release getVersion(Optional<Duration> optionalTimeout, Optional<String> optionalKeyName, byte[] privateKeyEncryptionPassphrase) throws Exception {
        return getRelease(SailingReleaseRepository.INSTANCE, optionalTimeout, optionalKeyName, privateKeyEncryptionPassphrase);
    }
}
