package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMaster;
import com.sap.sse.common.Duration;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.CreateDNSBasedLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.StartAwsHost;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.route53.model.RRType;

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
    private static final Optional<Duration> optionalTimeout = Optional.of(Duration.ONE_MINUTE.times(5));
    private AwsLandscape<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> landscape;
    private AwsRegion region;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
        region = new AwsRegion(Region.EU_WEST_2);
    }
    
    @Test
    public void testConnectivity() throws JSchException, IOException, SftpException, NumberFormatException, InterruptedException, URISyntaxException {
        final String TARGET_GROUP_NAME_PREFIX = "S-test-";
        final String hostedZoneName = "wiesen-weg.de";
        final String hostname = "S-test-"+new Random().nextInt()+"."+hostedZoneName;
        final String keyName = "MyKey-"+UUID.randomUUID();
        final Release release = SailingReleaseRepository.INSTANCE.getLatestMasterRelease();
        final StartAwsHost<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>, SailingAnalyticsHost<String>> startSailingAnalyticsMaster = new StartSailingAnalyticsMaster<String>(
                hostname, region, landscape, InstanceType.T2_SMALL, keyName, Optional.of(release), /* databaseConfiguration */ null,
                /* rabbitConfiguration */ null, /* replicationConfiguration */ null, "axel.uhl@sap.com", Optional.empty());
        startSailingAnalyticsMaster.run();
        final AwsInstance<String, SailingAnalyticsMetrics> host = landscape.launchHost(landscape
                .getImage(region, "ami-01b4b27a5699e33e6"),
                InstanceType.T3_SMALL, landscape.getAvailabilityZoneByName(region, "eu-west-2b"), keyName, Collections.singleton(()->"sg-0b2afd48960251280"),
                Optional.of(Tags.with("Name", "MyHost").and("Hello", "World")));
        try {
            assertNotNull(host);
            final Instance instance = landscape.getInstance(host.getInstanceId(), region);
            boolean foundName = false;
            boolean foundHello = false;
            for (final Tag tag : instance.tags()) {
                if (tag.key().equals("Name") && tag.value().equals("MyHost")) {
                    foundName = true;
                }
                if (tag.key().equals("Hello") && tag.value().equals("World")) {
                    foundHello = true;
                }
            }
            assertTrue(foundName);
            assertTrue(foundHello);
            // check env.sh access
            final ApplicationProcess<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> process = new ApplicationProcessImpl<>(8888, host, "/home/sailing/servers/server");
            final String envSh = process.getEnvSh(optionalTimeout);
            assertFalse(envSh.isEmpty());
            assertTrue(envSh.contains("SERVER_NAME="));
            assertEquals(14888, process.getTelnetPortToOSGiConsole(optionalTimeout));
            final AwsLandscape<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> castLandscape = (AwsLandscape<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>>) landscape;
            final CreateDNSBasedLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>, AwsInstance<String, SailingAnalyticsMetrics>> createDNSBasedLoadBalancerMappingProcedure =
                    new CreateDNSBasedLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>, AwsInstance<String, SailingAnalyticsMetrics>>(
                            process, hostname, TARGET_GROUP_NAME_PREFIX, castLandscape, optionalTimeout);
            final String wiesenWegId = landscape.getDNSHostedZoneId(hostedZoneName);
            try {
                createDNSBasedLoadBalancerMappingProcedure.run();
                assertNotNull(createDNSBasedLoadBalancerMappingProcedure.getLoadBalancerUsed());
                assertNotNull(createDNSBasedLoadBalancerMappingProcedure.getMasterTargetGroupCreated());
                assertEquals(TARGET_GROUP_NAME_PREFIX+process.getServerName(optionalTimeout), createDNSBasedLoadBalancerMappingProcedure.getPublicTargetGroupCreated().getName());
            } finally {
                if (createDNSBasedLoadBalancerMappingProcedure.getLoadBalancerUsed() != null) {
                    createDNSBasedLoadBalancerMappingProcedure.getLoadBalancerUsed().delete();
                    landscape.removeDNSRecord(wiesenWegId, hostname, RRType.CNAME, createDNSBasedLoadBalancerMappingProcedure.getLoadBalancerUsed().getDNSName());
                }
            }
        } finally {
            landscape.terminate(host);
            landscape.deleteKeyPair(region, keyName);
        }
    }
}
