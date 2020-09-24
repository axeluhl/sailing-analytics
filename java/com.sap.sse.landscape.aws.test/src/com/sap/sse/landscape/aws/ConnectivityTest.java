package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.ssh.SSHKeyPair;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.ChangeStatus;

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
    private AwsLandscape<String, ApplicationProcessMetrics> landscape;
    private AwsRegion region;
    private byte[] keyPass;
    
    @Before
    public void setUp() {
        landscape = AwsLandscape.obtain();
        region = new AwsRegion(Region.EU_WEST_2);
        keyPass = "lkayrelakuesyrlasp8caorewyc".getBytes();
    }
    
    @Test
    public void testConnectivity() {
        final AwsInstance<String, ApplicationProcessMetrics> host = landscape.launchHost(landscape.getImage(region, "ami-01b4b27a5699e33e6"),
                InstanceType.T3_SMALL, landscape.getAvailabilityZoneByName(region, "eu-west-2b"), "Axel", Collections.singleton(()->"sg-0b2afd48960251280"));
        try {
            assertNotNull(host);
        } finally {
            landscape.terminate(host);
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
                InstanceType.T3_SMALL, landscape.getAvailabilityZoneByName(region, "eu-west-2b"), keyName, Collections.singleton(()->"sg-0b2afd48960251280"));
        try {
            assertNotNull(host);
            logger.info("Created instance with ID "+host.getInstanceId());
            logger.info("Waiting for public IP address...");
            // wait for public IPv4 address to become available:
            InetAddress address = host.getPublicAddress(Duration.ONE_SECOND.times(20));
            assertNotNull(address);
            logger.info("Obtained public IP address "+address);
            SshCommandChannel shellChannel = null;
            int sshConnectAttempts = 20;
            while (shellChannel == null && sshConnectAttempts-- > 0) {
                try {
                    shellChannel = host.createRootSshChannel();
                } catch (JSchException e) {
                    logger.info(e.getMessage()
                            + " while trying to connect. Probably timeout trying early SSH connection. Retrying "
                            + sshConnectAttempts + " more times...");
                    Thread.sleep(10000);
                }
            }
            assertNotNull(shellChannel);
            logger.info("Shell channel connected. Waiting for it to become responsive...");
            shellChannel.sendCommandLineSynchronously("pwd", System.err);
            assertEquals("/root\n", turnAllLineSeparatorsIntoLineFeed(new String(shellChannel.getStreamContentsAsByteArray())));
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
        final String hostname = "my-test-host-"+new Random().nextInt()+".wiesen-weg.de.";
        final String ipAddress = "1.2.3.4";
        try {
            ChangeInfo changeInfo = landscape.setDNSRecordToValue(landscape.getDefaultDNSHostedZoneId(), hostname, ipAddress);
            int attempts = 10;
            while ((changeInfo=landscape.getUpdatedChangeInfo(changeInfo)).status() != ChangeStatus.INSYNC && --attempts > 0) {
                Thread.sleep(5000);
            };
            assertEquals(ChangeStatus.INSYNC, changeInfo.status());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            landscape.removeDNSRecord(landscape.getDefaultDNSHostedZoneId(), hostname, ipAddress);
        }
    }
    
    @Test
    public void createEmptyLoadBalancerTest() {
        final String albName = "MyAlb"+new Random().nextInt();
        final ApplicationLoadBalancer alb = landscape.createLoadBalancer(albName, region);
        try {
            assertNotNull(alb);
            assertEquals(albName, alb.getName());
        } finally {
            landscape.deleteLoadBalancer(alb);
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
}
