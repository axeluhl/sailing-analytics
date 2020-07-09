package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.impl.AwsRegion;
import com.sap.sse.landscape.ssh.SSHKeyPair;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;

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
        final AwsInstance host = landscape.launchHost(landscape.getImage(region, "ami-01b4b27a5699e33e6"),
                new AwsAvailabilityZone("eu-west-2b", region), "Axel", Collections.singleton(()->"sg-0b2afd48960251280"));
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
        final String keyId = landscape.importKeyPair(region, pubKeyBytes, testKeyName);
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
    public void testSshConnect() throws JSchException, InterruptedException {
        final String keyName = "MyKey-"+UUID.randomUUID();
        final SSHKeyPair sshKeyPair = landscape.createKeyPair(region, keyName);
        assertNotNull(sshKeyPair);
        assertEquals(keyName, sshKeyPair.getName());
        final AwsInstance host = landscape.launchHost(landscape.getImage(region, "ami-01b4b27a5699e33e6"),
                new AwsAvailabilityZone("eu-west-2b", region), keyName, Collections.singleton(()->"sg-0b2afd48960251280"));
        try {
            assertNotNull(host);
            logger.info("Created instance with ID "+host.getInstanceId());
            logger.info("Waiting for public IP address...");
            // wait for public IPv4 address to become available:
            int publicIpAddressWaitAttempts = 10;
            InetAddress address = null;
            while ((address=host.getAddress()) == null && --publicIpAddressWaitAttempts > 0) {
                Thread.sleep(1000);
            };
            assertNotNull(address);
            logger.info("Obtained public IP address "+address);
            Channel shellChannel = null;
            int sshConnectAttempts = 10;
            while (sshConnectAttempts-- > 0) {
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
            logger.info("Shell channel connected. Sending commands...");
            final ByteArrayOutputStream shellOutput = new ByteArrayOutputStream();
            final ByteArrayInputStream shellInput = new ByteArrayInputStream("pwd\nexit\n".getBytes());
            shellChannel.setOutputStream(shellOutput);
            shellChannel.setInputStream(shellInput);
            shellChannel.connect(/* timeout in millis */ 5000);
            int attempts = 0;
            boolean foundPwdOutput = false;
            while (!foundPwdOutput && attempts < 10) {
                Thread.sleep(10000);
                // (?s) means . also matches line separators
                // (?m) means that we'd like to match a multi-line string
                foundPwdOutput = new String(shellOutput.toByteArray()).matches("(?s)(?m).*^/root$.*");
                attempts++;
            }
            shellChannel.disconnect();
            assertTrue(foundPwdOutput);
        } finally {
            landscape.terminate(host);
            landscape.deleteKeyPair(region, keyName);
        }
//        final JSch jsch = new JSch();
//        JSch.setLogger(new JCraftLogAdapter());
//        final KeyPair keyPairReadFromFile = KeyPair.load(jsch, "test_key", "test_key.pub");
//        jsch.addIdentity("Test Key", getPrivateKeyBytes(keyPairReadFromFile, keyPass), getPublicKeyBytes(keyPairReadFromFile), keyPass);
//        final Session session = jsch.getSession("vishal", "homemp3.dyndns.org");
//        assertNotNull(session);
//        assertEquals(22, session.getPort());
//        session.setUserInfo(new YesUserInfo());
//        session.connect(/* timeout in millis */ 5000);
//        final Channel shellChannel = session.openChannel("shell");
    }
    
    @Test
    public void testImageDate() throws ParseException {
        final AmazonMachineImage image = landscape.getImage(region, "ami-01b4b27a5699e33e6");
        assertEquals(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse("2020-07-08T12:41:06+0200")),
                image.getCreatedAt());
    }
}
