package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSchException;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsProcess;
import com.sap.sailing.landscape.SailingReleaseRepository;
import com.sap.sailing.landscape.impl.BearerTokenReplicationCredentials;
import com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration;
import com.sap.sailing.landscape.procedures.SailingAnalyticsMasterConfiguration.Builder;
import com.sap.sailing.landscape.procedures.StartMultiServer;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsHost;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMasterHost;
import com.sap.sailing.landscape.procedures.UpgradeAmi;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.ApplicationProcessHost;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.CreateDynamicLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.StartMongoDBServer;
import com.sap.sse.landscape.mongodb.MongoProcess;
import com.sap.sse.landscape.ssh.SshCommandChannel;

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
    private static final Logger logger = Logger.getLogger(TestProcedures.class.getName());
    private static final Optional<Duration> optionalTimeout = Optional.of(Duration.ONE_MINUTE.times(10));
    private AwsLandscape<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> landscape;
    private AwsRegion region;
    private final static String SECURITY_SERVICE_REPLICATION_BEARER_TOKEN = "security.service.replication.bearer.token";
    private String securityServiceReplicationBearerToken;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
        region = new AwsRegion(Region.EU_WEST_2);
        securityServiceReplicationBearerToken = System.getProperty(SECURITY_SERVICE_REPLICATION_BEARER_TOKEN);
    }
    
    @Test
    public void testStartupEmptyMultiServer() throws Exception {
        final String keyName = "MyKey-"+UUID.randomUUID();
        landscape.createKeyPair(region, keyName);
        final StartMultiServer.Builder<?, String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> builder = StartMultiServer.builder();
        final StartMultiServer<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> startEmptyMultiServer = builder
              .setLandscape(landscape)
              .setKeyName(keyName)
              .setOptionalTimeout(optionalTimeout)
              .build();
        try {
            // this is expected to have connected to the default "live" replica set.
            startEmptyMultiServer.run();
            final AwsInstance<String, SailingAnalyticsMetrics> host = startEmptyMultiServer.getHost();
            final SshCommandChannel sshChannel = host.createRootSshChannel(optionalTimeout);
            sshChannel.sendCommandLineSynchronously("ls "+ApplicationProcessHost.DEFAULT_SERVERS_PATH, new ByteArrayOutputStream());
            final String result = sshChannel.getStreamContentsAsString();
            assertTrue(result.isEmpty());
            final HttpURLConnection connection = (HttpURLConnection) new URL("http", host.getPublicAddress().getCanonicalHostName(), 80, "").openConnection();
            assertTrue(connection.getHeaderField("Server").startsWith("Apache"));
            connection.disconnect();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while trying to create a MongoDB replica", e);
            throw e;
        } finally {
            if (startEmptyMultiServer.getHost() != null) {
                startEmptyMultiServer.getHost().terminate();
            }
            landscape.deleteKeyPair(region, keyName);
        }
    }
    
    @Test
    public void testAddMongoReplica() throws Exception {
        final String keyName = "MyKey-"+UUID.randomUUID();
        landscape.createKeyPair(region, keyName);
        final StartMongoDBServer.Builder<?, String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> builder = StartMongoDBServer.builder();
        final StartMongoDBServer<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> startMongoDBServerProcedure = builder
              .setLandscape(landscape)
              .setKeyName(keyName)
              .setOptionalTimeout(optionalTimeout)
              .build();
        try {
            // this is expected to have connected to the default "live" replica set.
            startMongoDBServerProcedure.run();
            final MongoProcess result = startMongoDBServerProcedure.getMongoProcess();
            connectAndWaitForReplicaSet(result, AwsLandscape.MONGO_DEFAULT_REPLICA_SET_NAME);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while trying to create a MongoDB replica", e);
            throw e;
        } finally {
            if (startMongoDBServerProcedure.getHost() != null) {
                startMongoDBServerProcedure.getHost().terminate();
            }
            landscape.deleteKeyPair(region, keyName);
        }
    }
    
    private void connectAndWaitForReplicaSet(MongoProcess mongoProcess, String mongoDefaultReplicaSetName) throws JSchException, IOException, InterruptedException {
        final TimePoint start = TimePoint.now();
        boolean fine = true;
        do {
            try {
                final SshCommandChannel sshChannel = mongoProcess.getHost().createSshChannel("ec2-user", optionalTimeout);
                final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                sshChannel.sendCommandLineSynchronously("i=0; while [ $i -lt $(echo \"rs.status().members.length\" | mongo  2>/dev/null | tail -n +5 | head -n +1) ]; do  echo \"rs.status().members[$i].stateStr\" | mongo  2>/dev/null | tail -n +5 | head -n +1; i=$((i+1)); done", stderr);
                if (stderr.size() > 0) {
                    logger.log(Level.WARNING, "stderr while trying to fetch replica set members", stderr.toString());
                }
                final String stdout = sshChannel.getStreamContentsAsString();
                sshChannel.disconnect();
                fine = stdout.contains("PRIMARY") && stdout.contains("SECONDARY");
            } catch (Exception e) {
                logger.info("No success (yet) finding replica set "+mongoDefaultReplicaSetName);
                fine = false;
            }
        } while (!fine && start.until(TimePoint.now()).compareTo(optionalTimeout.get()) < 0);
    }

    @Test
    public void testImageUpgrade() throws Exception {
        final String keyName = "MyKey-"+UUID.randomUUID();
        landscape.createKeyPair(region, keyName);
        final com.sap.sailing.landscape.procedures.UpgradeAmi.Builder<?, String, SailingAnalyticsProcess<String>> imageUpgradeProcedureBuilder = UpgradeAmi.builder();
        final UpgradeAmi<String> imageUpgradeProcedure =
                imageUpgradeProcedureBuilder
                    .setLandscape(landscape)
                    .setInstanceType(InstanceType.T2_MEDIUM)
                    .setKeyName(keyName)
                    .setOptionalTimeout(optionalTimeout)
                    .build();
        try {
            imageUpgradeProcedure.run();
            final AmazonMachineImage<String, SailingAnalyticsMetrics> upgradedAmi = imageUpgradeProcedure.getUpgradedAmi();
            assertTrue(upgradedAmi.getCreatedAt().until(TimePoint.now()).compareTo(Duration.ONE_MINUTE.times(10)) < 0);
            assertEquals(3, Util.size(upgradedAmi.getBlockDeviceMappings()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during test", e);
        } finally {
            landscape.deleteKeyPair(region, keyName);
            if (imageUpgradeProcedure.getUpgradedAmi() != null) {
                imageUpgradeProcedure.getUpgradedAmi().delete();
            }
        }
    }
    
    @Test
    public <AppConfigBuilderT extends SailingAnalyticsMasterConfiguration.Builder<AppConfigBuilderT, String>>
    void testConnectivity() throws Exception {
        final String serverName = "test"+new Random().nextInt();
        final String keyName = "MyKey-"+UUID.randomUUID();
        landscape.createKeyPair(region, keyName);
        Builder<AppConfigBuilderT, String> applicationConfigurationBuilder = SailingAnalyticsMasterConfiguration.builder();
        applicationConfigurationBuilder
            .setServerName(serverName)
            .setRelease(SailingReleaseRepository.INSTANCE.getLatestRelease("bug4811")) // TODO this is the debug config for the current branch bug4811 and its releases
            .setCommaSeparatedEmailAddressesToNotifyOfStartup("axel.uhl@sap.com")
            .setInboundReplicationConfiguration(InboundReplicationConfiguration.builder()
                    .setCredentials(new BearerTokenReplicationCredentials(securityServiceReplicationBearerToken))
                    .build());
        final StartSailingAnalyticsMasterHost.Builder<?, String> builder = StartSailingAnalyticsMasterHost.builder(applicationConfigurationBuilder);
        final StartSailingAnalyticsHost<String> startSailingAnalyticsMaster = builder
                .setLandscape(landscape)
                .setRegion(region)
                .setInstanceType(InstanceType.T3_LARGE)
                .setKeyName(keyName)
                .setTags(Optional.of(Tags.with("Hello", "World")))
                .setOptionalTimeout(optionalTimeout)
                .build();
        startSailingAnalyticsMaster.run();
        final ApplicationProcessHost<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>> host = startSailingAnalyticsMaster.getHost();
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
            final SailingAnalyticsProcess<String> process = startSailingAnalyticsMaster.getSailingAnalyticsProcess();
            assertTrue(process.waitUntilReady(optionalTimeout));
            final String envSh = process.getEnvSh(optionalTimeout);
            assertFalse(envSh.isEmpty());
            assertTrue("Couldn't find SERVER_NAME=\""+serverName+"\" in env.sh:\n"+envSh, envSh.contains("SERVER_NAME=\""+serverName+"\""));
            assertEquals(14888, process.getTelnetPortToOSGiConsole(optionalTimeout));
            // Now create an ALB mapping, assuming to create the dynamic ALB:
            final String domain = "wiesen-weg.de";
            final String hostname = serverName+"."+domain;
            CreateDynamicLoadBalancerMapping.Builder<?, ?, String, SailingAnalyticsMetrics,
                    SailingAnalyticsProcess<String>, AwsInstance<String, SailingAnalyticsMetrics>> createAlbProcedureBuilder = CreateDynamicLoadBalancerMapping.builder();
            createAlbProcedureBuilder
                .setProcess(process)
                .setHostname(hostname)
                .setTargetGroupNamePrefix("S-ded-") // TODO when we combine procedures for launching dedicated hosts (StartSailingAnlayticsHost and specializations) then "S-ded-" should be the default; for DeployProcessOnMultiServer, "S-shared-" should be the default
                .setLandscape(landscape);
            optionalTimeout.ifPresent(createAlbProcedureBuilder::setTimeout);
            final CreateDynamicLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsProcess<String>, AwsInstance<String, SailingAnalyticsMetrics>> createAlbProcedure =
                    createAlbProcedureBuilder.build();
            try {
                createAlbProcedure.run();
                // A few validations:
                // Is the process's host part of the public and master target groups?
                assertNotNull(createAlbProcedure.getMasterTargetGroupCreated());
                assertNotNull(createAlbProcedure.getPublicTargetGroupCreated());
                assertTrue(createAlbProcedure.getMasterTargetGroupCreated().getRegisteredTargets().keySet().contains(process.getHost()));
                assertTrue(createAlbProcedure.getPublicTargetGroupCreated().getRegisteredTargets().keySet().contains(process.getHost()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during test case", e);
            } finally {
                createAlbProcedure.getLoadBalancerUsed().delete();
                landscape.removeDNSRecord(landscape.getDNSHostedZoneId(domain), "*."+domain,
                        RRType.CNAME, createAlbProcedure.getLoadBalancerUsed().getDNSName());
            }
        } finally {
            landscape.terminate(host);
            landscape.deleteKeyPair(region, keyName);
        }
    }

}
