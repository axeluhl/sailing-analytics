package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.SftpException;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.landscape.Release;
import com.sap.sse.landscape.application.ApplicationMasterProcess;
import com.sap.sse.landscape.application.ApplicationProcess;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.application.ApplicationReplicaProcess;
import com.sap.sse.landscape.application.impl.ApplicationProcessImpl;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.aws.orchestration.CreateDNSBasedLoadBalancerMapping;
import com.sap.sse.landscape.impl.ReleaseRepositoryImpl;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.landscape.ssh.SshCommandChannel;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ActionTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Rule;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.ChangeStatus;
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
public class ConnectivityTest {
    private static final Logger logger = Logger.getLogger(ConnectivityTest.class.getName());
    private static final Optional<Duration> optionalTimeout = Optional.of(Duration.ONE_MINUTE.times(5));
    private AwsLandscape<String, ApplicationProcessMetrics, ?, ?> landscape;
    private AwsRegion region;
    private byte[] keyPass;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
        region = new AwsRegion(Region.EU_WEST_2);
        keyPass = "lkayrelakuesyrlasp8caorewyc".getBytes();
    }
    
    @Test
    public <MasterT extends ApplicationMasterProcess<String, ApplicationProcessMetrics, MasterT, ReplicaT>, ReplicaT extends ApplicationReplicaProcess<String, ApplicationProcessMetrics, MasterT, ReplicaT>>
    void testConnectivity() throws JSchException, IOException, SftpException, NumberFormatException, InterruptedException {
        final String TARGET_GROUP_NAME_PREFIX = "S-test-";
        final String hostedZoneName = "wiesen-weg.de";
        final String hostname = "test-"+new Random().nextInt()+"."+hostedZoneName;
        final String keyName = "MyKey-"+UUID.randomUUID();
        createKeyPair(keyName);
        final AwsInstance<String, ApplicationProcessMetrics> host = landscape.launchHost(landscape.getImage(region, "ami-01b4b27a5699e33e6"),
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
            final ApplicationProcess<String, ApplicationProcessMetrics, MasterT, ReplicaT> process = new ApplicationProcessImpl<>(8888, host, "/home/sailing/servers/server");
            final String envSh = process.getEnvSh(optionalTimeout);
            assertFalse(envSh.isEmpty());
            assertTrue(envSh.contains("SERVER_NAME="));
            final Release release = process.getRelease(new ReleaseRepositoryImpl("http://releases.sapsailing.com", "build"), optionalTimeout);
            assertNotNull(release);
            assertEquals(14888, process.getTelnetPortToOSGiConsole(optionalTimeout));
            @SuppressWarnings("unchecked")
            final AwsLandscape<String, ApplicationProcessMetrics, MasterT, ReplicaT> castLandscape = (AwsLandscape<String, ApplicationProcessMetrics, MasterT, ReplicaT>) landscape;
            final CreateDNSBasedLoadBalancerMapping.Builder<String, ApplicationProcessMetrics, MasterT, ReplicaT, AwsInstance<String, ApplicationProcessMetrics>> builder = CreateDNSBasedLoadBalancerMapping.builder();
            builder
                .setProcess(process)
                .setHostname(hostname)
                .setTargetGroupNamePrefix(TARGET_GROUP_NAME_PREFIX)
                .setLandscape(castLandscape);
            optionalTimeout.ifPresent(builder::setTimeout);
            final CreateDNSBasedLoadBalancerMapping<String, ApplicationProcessMetrics, MasterT, ReplicaT, AwsInstance<String, ApplicationProcessMetrics>> createDNSBasedLoadBalancerMappingProcedure =
                    builder.build();
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
    
    @Test
    public void generateSshKeyPair() throws JSchException, FileNotFoundException, IOException {
        final String publicKeyComment = "Test Key";
        final JSch jsch = new JSch();
        final KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 4096);
        final String keyFileBaseName = "test_key";
        keyPair.writePrivateKey(keyFileBaseName, keyPass);
        keyPair.writePublicKey(keyFileBaseName+".pub", publicKeyComment);
        final KeyPair keyPairReadFromFile = KeyPair.load(jsch, keyFileBaseName, keyFileBaseName+".pub");
        assertEquals(publicKeyComment, keyPairReadFromFile.getPublicKeyComment());
        new File(keyFileBaseName).delete();
        new File(keyFileBaseName+".pub").delete();
    }
    
    @Test
    public void testImportKey() throws JSchException {
        final String testKeyName = "My Test Key";
        final JSch jsch = new JSch();
        final KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 4096);
        final byte[] pubKeyBytes = getPublicKeyBytes(keyPair);
        final byte[] privKeyBytes = getPrivateKeyBytes(keyPair, /* passphrase */ null);
        final String keyId = landscape.importKeyPair(region, pubKeyBytes, privKeyBytes, testKeyName);
        assertTrue(keyId.startsWith("key-"));
        final KeyPairInfo awsKeyPairInfo = landscape.getKeyPairInfo(region, testKeyName);
        assertNotNull(awsKeyPairInfo);
        landscape.deleteKeyPair(region, testKeyName);
    }

    private byte[] getPublicKeyBytes(final KeyPair keyPair) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keyPair.writePublicKey(bos, keyPair.getPublicKeyComment());
        final byte[] pubKeyBytes = bos.toByteArray();
        return pubKeyBytes;
    }
    
    private byte[] getPrivateKeyBytes(final KeyPair keyPair, final byte[] passphrase) {
        if (!keyPair.decrypt(passphrase)) { // need to decrypt before writePrivateKey would work
            throw new IllegalArgumentException("Passphrase didn't unlock private key of key pair "+keyPair);
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keyPair.writePrivateKey(bos, /* passphrase */ null);
        final byte[] privKeyBytes = bos.toByteArray();
        return privKeyBytes;
    }
    
    @Test
    public void testSshConnectWithCreatedKey() throws JSchException, InterruptedException, IOException {
        final String keyName = "MyKey-"+UUID.randomUUID();
        createKeyPair(keyName);
        testSshConnectWithKey(keyName);
    }

    @Test
    public void testSshConnectWithImportedKey() throws JSchException, InterruptedException, IOException {
        final String keyName = "MyKey-"+UUID.randomUUID();
        final KeyPair keyPair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA, 4096);
        landscape.importKeyPair(region, getPublicKeyBytes(keyPair), getPrivateKeyBytes(keyPair, /* passphrase */ null), keyName);
        testSshConnectWithKey(keyName);
    }

    private void testSshConnectWithKey(final String keyName) throws InterruptedException, JSchException, IOException {
        final AwsInstance<String, ApplicationProcessMetrics> host = landscape.launchHost(landscape.getImage(region, "ami-01b4b27a5699e33e6"),
                InstanceType.T3_SMALL, landscape.getAvailabilityZoneByName(region, "eu-west-2b"), keyName, Collections.singleton(()->"sg-0b2afd48960251280"), /* tags */ Optional.empty());
        try {
            assertNotNull(host);
            logger.info("Created instance with ID "+host.getInstanceId());
            logger.info("Waiting for public IP address...");
            // wait for public IPv4 address to become available:
            InetAddress address = host.getPublicAddress(optionalTimeout);
            assertNotNull(address);
            logger.info("Obtained public IP address "+address);
            SshCommandChannel shellChannel = host.createRootSshChannel(optionalTimeout);
            assertNotNull(shellChannel);
            logger.info("Shell channel connected. Waiting for it to become responsive...");
            shellChannel.sendCommandLineSynchronously("pwd", System.err);
            assertEquals("/root\n", turnAllLineSeparatorsIntoLineFeed(new String(shellChannel.getStreamContentsAsByteArray())));
            // now try a simple command, checking for the "init" process to be found
            final SshCommandChannel commandChannel = host.createRootSshChannel(Optional.empty());
            final String processToLookFor = "init";
            commandChannel.sendCommandLineSynchronously("ps axlw | grep "+processToLookFor, new ByteArrayOutputStream());
            final String output = new String(commandChannel.getStreamContentsAsByteArray());
            assertTrue(output.contains(processToLookFor));
            assertEquals(0, commandChannel.getExitStatus());
        } finally {
            landscape.terminate(host);
            landscape.deleteKeyPair(region, keyName);
        }
    }

    private String turnAllLineSeparatorsIntoLineFeed(String string) {
        return string.replaceAll("\n\r", "\n").replaceAll("\r\n", "\n").replaceAll("\r", "\n");
    }

    private void createKeyPair(final String keyName) throws JSchException {
        final SSHKeyPair sshKeyPair = landscape.createKeyPair(region, keyName);
        assertNotNull(sshKeyPair);
        assertEquals(keyName, sshKeyPair.getName());
    }
    
    @Test
    public void testImageDate() throws ParseException {
        final AmazonMachineImage<String, ApplicationProcessMetrics> image = landscape.getImage(region, "ami-01b4b27a5699e33e6");
        assertEquals(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse("2020-07-08T12:41:06+0200")),
                image.getCreatedAt());
    }
    
    @Test
    public void setDNSRecordTest() {
        final String testHostedZoneDnsName = "wiesen-weg.de";
        final String hostname = "my-test-host-"+new Random().nextInt()+"."+testHostedZoneDnsName+".";
        final String ipAddress = "1.2.3.4";
        final String dnsHostedZoneId = landscape.getDNSHostedZoneId(testHostedZoneDnsName);
        try {
            ChangeInfo changeInfo = landscape.setDNSRecordToValue(dnsHostedZoneId, hostname, ipAddress);
            int attempts = 10;
            while ((changeInfo=landscape.getUpdatedChangeInfo(changeInfo)).status() != ChangeStatus.INSYNC && --attempts > 0) {
                Thread.sleep(10000);
            };
            assertEquals(ChangeStatus.INSYNC, changeInfo.status());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            landscape.removeDNSRecord(dnsHostedZoneId, hostname, ipAddress);
        }
    }
    
    @Test
    public void createEmptyLoadBalancerTest() throws InterruptedException {
        final String albName = "MyAlb"+new Random().nextInt();
        final ApplicationLoadBalancer<String, ApplicationProcessMetrics> alb = landscape.createLoadBalancer(albName, region);
        try {
            assertNotNull(alb);
            assertEquals(albName, alb.getName());
            assertTrue(Util.contains(Util.map(landscape.getLoadBalancers(region), ApplicationLoadBalancer::getArn), alb.getArn()));
            // now add two rules to the load balancer and check they arrive:
            final String hostnameCondition = "a.wiesen-weg.de";
            @SuppressWarnings("unchecked")
            final Iterable<Rule> rulesCreated = alb
                    .addRules(Rule.builder()
                            .priority("5")
                            .conditions(r -> r.field("host-header").hostHeaderConfig(hhc -> hhc.values(hostnameCondition)))
                            .actions(a -> a.type(ActionTypeEnum.FIXED_RESPONSE).fixedResponseConfig(frc -> frc.statusCode("200").messageBody("Hello world"))).build());
            assertEquals(1, Util.size(rulesCreated));
            assertTrue(hostnameCondition, rulesCreated.iterator().next().conditions().iterator().next().hostHeaderConfig().values().contains(hostnameCondition));
        } finally {
            alb.delete();
        }
    }
    
    @Test
    public void createAndDeleteTargetGroupTest() {
        final String targetGroupName = "TestTargetGroup-"+new Random().nextInt();
        final TargetGroup<String, ApplicationProcessMetrics> targetGroup = landscape.createTargetGroup(region, targetGroupName, 80, "/gwt/status", 80);
        try {
            final TargetGroup<String, ApplicationProcessMetrics> fetchedTargetGroup = landscape.getTargetGroup(region, targetGroupName, targetGroup.getTargetGroupArn());
            assertEquals(targetGroupName, fetchedTargetGroup.getName());
        } finally {
            landscape.deleteTargetGroup(targetGroup);
        }
    }
    
    @Test
    public void testCentralReverseProxyInEuWest2IsAvailable() throws IOException, InterruptedException, JSchException {
        final ReverseProxyCluster<String, ApplicationProcessMetrics, ?, ?, ?> proxy = landscape.getCentralReverseProxy(new AwsRegion("eu-west-2"));
        assertEquals(1, Util.size(proxy.getHosts()));
        final HttpURLConnection healthCheckConnection = (HttpURLConnection) new URL("http://"+proxy.getHosts().iterator().next().getPublicAddress().getCanonicalHostName()+proxy.getHealthCheckPath()).openConnection();
        assertEquals(200, healthCheckConnection.getResponseCode());
        healthCheckConnection.disconnect();
    }
}
