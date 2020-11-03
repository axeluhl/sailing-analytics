package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMaster;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsRegion;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.Tag;

/**
 * Tests for the AWS SDK landscape wrapper in bundle {@code com.sap.sse.landscape.aws}. To run these tests
 * successfully it is necessary to have valid AWS credentials for region {@code EU_WEST_2} that allow the
 * AWS user account to create keys and launch instances, etc. These are to be provided as explained
 * in the documentation of {@link AwsLandscape#obtain()}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TestProcedures {
    private static final Optional<Duration> optionalTimeout = Optional.of(Duration.ONE_MINUTE.times(10));
    private AwsLandscape<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> landscape;
    private AwsRegion region;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
        region = new AwsRegion(Region.EU_WEST_2);
    }
    
    @Test
    public void testConnectivity() throws Exception {
        final String serverName = "test"+new Random().nextInt();
        final String keyName = "MyKey-"+UUID.randomUUID();
        landscape.createKeyPair(region, keyName);
        final StartSailingAnalyticsMaster.Builder<String> builder = StartSailingAnalyticsMaster.builder();
        final StartSailingAnalyticsMaster<String> startSailingAnalyticsMaster = builder
                .setServerName(serverName)
                .setLandscape(landscape)
                .setRegion(region)
                .setInstanceType(InstanceType.T3_SMALL)
                .setKeyName(keyName)
                .setCommaSeparatedEmailAddressesToNotifyOfStartup("axel.uhl@sap.com")
                .setTags(Optional.of(Tags.with("Hello", "World")))
                .setOptionalTimeout(optionalTimeout)
                .build();
        startSailingAnalyticsMaster.run();
        final SailingAnalyticsHost<String> host = startSailingAnalyticsMaster.getHost();
        try {
            assertNotNull(host);
            final Instance instance = landscape.getInstance(host.getInstanceId(), region);
            boolean foundName = false;
            boolean foundHello = false;
            for (final Tag tag : instance.tags()) {
                if (tag.key().equals("Name") && tag.value().equals("SL "+serverName+" (Master)")) {
                    foundName = true;
                }
                if (tag.key().equals("Hello") && tag.value().equals("World")) {
                    foundHello = true;
                }
            }
            assertTrue(foundName);
            assertTrue(foundHello);
            // check env.sh access
            final SailingAnalyticsMaster<String> process = startSailingAnalyticsMaster.getSailingAnalyticsProcess();
            // TODO The problem here: the /etc/init.d/sailing script takes a while to do its job with downloading, installing
            // the release, updating git, and running the httpd reverse proxy server. Only then will the env.sh be patched, just before
            // the instance is launched. We may want to wait for the process to become available on port 8888 before continuing...
            final TimePoint startingToPollForReady = TimePoint.now();
            while (!process.isReady(optionalTimeout) && (!optionalTimeout.isPresent() || startingToPollForReady.until(TimePoint.now()).compareTo(optionalTimeout.get()) <= 0)) {
                Thread.sleep(1000);
            }
            assertTrue(process.isReady(optionalTimeout));
            final String envSh = process.getEnvSh(optionalTimeout);
            assertFalse(envSh.isEmpty());
            assertTrue(envSh.contains("SERVER_NAME="+serverName));
            assertEquals(14888, process.getTelnetPortToOSGiConsole(optionalTimeout));
        } finally {
            landscape.terminate(host);
            landscape.deleteKeyPair(region, keyName);
        }
    }
}
