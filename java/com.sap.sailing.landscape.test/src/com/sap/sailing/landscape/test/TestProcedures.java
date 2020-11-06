package com.sap.sailing.landscape.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.landscape.SailingAnalyticsHost;
import com.sap.sailing.landscape.SailingAnalyticsMaster;
import com.sap.sailing.landscape.SailingAnalyticsMetrics;
import com.sap.sailing.landscape.SailingAnalyticsReplica;
import com.sap.sailing.landscape.impl.BearerTokenReplicationCredentials;
import com.sap.sailing.landscape.procedures.StartSailingAnalyticsMaster;
import com.sap.sailing.landscape.procedures.UpgradeAmi;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.InboundReplicationConfiguration;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.AmazonMachineImage;
import com.sap.sse.landscape.aws.AwsInstance;
import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.Tags;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.CreateDynamicLoadBalancerMapping;
import com.sap.sse.landscape.aws.orchestration.StartMongoDBServer;
import com.sap.sse.landscape.aws.orchestration.StartMongoDBServer.Builder;
import com.sap.sse.landscape.mongodb.MongoEndpoint;

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
    private AwsLandscape<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> landscape;
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
    public void testImageUpgrade() throws Exception {
        final String keyName = "MyKey-"+UUID.randomUUID();
        landscape.createKeyPair(region, keyName);
        final com.sap.sailing.landscape.procedures.UpgradeAmi.Builder<String, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> imageUpgradeProcedureBuilder = UpgradeAmi.builder();
        final UpgradeAmi<String, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>> imageUpgradeProcedure =
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
    public void testMongoReplica() throws Exception {
        final Builder<String, ApplicationProcessMetrics, ?, ?> startMongoDBServerProcedureBuilder = StartMongoDBServer.builder();
        final StartMongoDBServer<String, ApplicationProcessMetrics, ?, ?> startMongoDBServerProcedure = startMongoDBServerProcedureBuilder.build();
        // by default this should add a replica to the only "live" server in the test landscape
        try {
            startMongoDBServerProcedure.run();
            final AwsInstance<String, ApplicationProcessMetrics> host = startMongoDBServerProcedure.getHost();
            final String internalDNSName = landscape.getInstance(host.getInstanceId(), region).privateDnsName();
            // now configure a MongoEndpoint against the replica just launched and try to connect (with a timeout)
            final MongoEndpoint mongoEndpoint = landscape.getDatabaseConfigurationForDefaultReplicaSet(region);
            final MongoClient mongoClient = mongoEndpoint.getClient();
            final MongoDatabase database = mongoClient.getDatabase("admin");
            final Document replicaSetStatus = database.runCommand(new Document("replSetGetStatus", 1));
            @SuppressWarnings("unchecked")
            final List<Document> members = (List<Document>) replicaSetStatus.get("members");
            assertTrue(members.stream().filter(member->member.get("name").equals(internalDNSName+":27017")).findAny().isPresent());
        } finally {
            startMongoDBServerProcedure.getHost().terminate();
        }
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
                .setInstanceType(InstanceType.T3_LARGE)
                .setKeyName(keyName)
                .setCommaSeparatedEmailAddressesToNotifyOfStartup("axel.uhl@sap.com")
                .setTags(Optional.of(Tags.with("Hello", "World")))
                .setOptionalTimeout(optionalTimeout)
                .setReplicationConfiguration(InboundReplicationConfiguration.builder()
                        .setCredentials(new BearerTokenReplicationCredentials(securityServiceReplicationBearerToken))
                        .build())
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
                Thread.sleep(5000);
            }
            assertTrue(process.isReady(optionalTimeout));
            final String envSh = process.getEnvSh(optionalTimeout);
            assertFalse(envSh.isEmpty());
            assertTrue("Couldn't find SERVER_NAME=\""+serverName+"\" in env.sh:\n"+envSh, envSh.contains("SERVER_NAME=\""+serverName+"\""));
            assertEquals(14888, process.getTelnetPortToOSGiConsole(optionalTimeout));
            // Now create an ALB mapping, assuming to create the dynamic ALB:
            final String domain = "wiesen-weg.de";
            final String hostname = serverName+"."+domain;
            CreateDynamicLoadBalancerMapping.Builder<String, SailingAnalyticsMetrics,
            SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>, AwsInstance<String, SailingAnalyticsMetrics>> createAlbProcedureBuilder = CreateDynamicLoadBalancerMapping.builder();
            createAlbProcedureBuilder
                .setProcess(process)
                .setHostname(hostname)
                .setTargetGroupNamePrefix("S-ded-") // TODO when we combine procedures for launching dedicated hosts (StartSailingAnlayticsHost and specializations) then "S-ded-" should be the default; for DeployProcessOnMultiServer, "S-shared-" should be the default
                .setLandscape(landscape);
            optionalTimeout.ifPresent(createAlbProcedureBuilder::setTimeout);
            final CreateDynamicLoadBalancerMapping<String, SailingAnalyticsMetrics, SailingAnalyticsMaster<String>, SailingAnalyticsReplica<String>, AwsInstance<String, SailingAnalyticsMetrics>> createAlbProcedure =
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
