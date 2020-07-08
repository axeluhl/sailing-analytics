package com.sap.sse.landscape.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.sap.sse.common.TimePoint;
import com.sap.sse.landscape.application.ApplicationProcessMetrics;
import com.sap.sse.landscape.aws.impl.AmazonMachineImage;
import com.sap.sse.landscape.aws.impl.AwsAvailabilityZone;
import com.sap.sse.landscape.aws.impl.AwsInstance;
import com.sap.sse.landscape.aws.impl.AwsRegion;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;

public class ConnectivityTest {
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
                new AwsAvailabilityZone("eu-west-2b", region), Collections.singleton(()->"sg-0b2afd48960251280"));
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
        keyPair.writePrivateKey("test_key", keyPass);
        keyPair.writePublicKey("test_key.pub", publicKeyComment);
        final KeyPair keyPairReadFromFile = KeyPair.load(jsch, "test_key", "test_key.pub");
        assertEquals(publicKeyComment, keyPairReadFromFile.getPublicKeyComment());
    }
    
    @Test
    public void testImportKey() throws JSchException {
        final String testKeyName = "My Test Key";
        final JSch jsch = new JSch();
        final KeyPair keyPairReadFromFile = KeyPair.load(jsch, "test_key", "test_key.pub");
        final byte[] pubKeyBytes = getPublicKeyBytes(keyPairReadFromFile);
        final String keyId = landscape.importKeyPair(region, pubKeyBytes, testKeyName);
        assertTrue(keyId.startsWith("key-"));
        final KeyPairInfo awsKeyPairInfo = landscape.getKeyPair(region, testKeyName);
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
        final JSch jsch = new JSch();
        JSch.setLogger(new JCraftLogAdapter());
        final KeyPair keyPairReadFromFile = KeyPair.load(jsch, "test_key", "test_key.pub");
        jsch.addIdentity("Test Key", getPrivateKeyBytes(keyPairReadFromFile, keyPass), getPublicKeyBytes(keyPairReadFromFile), keyPass);
        final Session session = jsch.getSession("vishal", "homemp3.dyndns.org");
        assertNotNull(session);
        assertEquals(22, session.getPort());
        session.setUserInfo(new UserInfo() {
            @Override public void showMessage(String message) {}
            @Override public boolean promptYesNo(String message) { return true; } // accept host key
            @Override public boolean promptPassword(String message) { return false; } // we're using public key
            @Override public boolean promptPassphrase(String message) { return false; } // passphrase is provided programmatically
            @Override public String getPassword() { return null; }
            @Override public String getPassphrase() { return null; }
        });
        session.connect(/* timeout in millis */ 5000);
        final Channel shellChannel = session.openChannel("shell");
        final ByteArrayOutputStream shellOutput = new ByteArrayOutputStream();
        final ByteArrayInputStream shellInput = new ByteArrayInputStream("pwd\nexit\n".getBytes());
        shellChannel.setOutputStream(shellOutput);
        shellChannel.setInputStream(shellInput);
        shellChannel.connect(/* timeout in millis */ 5000);
        int attempts = 0;
        boolean foundPwdOutput = false;
        while (!foundPwdOutput && attempts < 10) {
            Thread.sleep(100);
            // (?s) means . also matches line separators
            // (?m) means that we'd like to match a multi-line string
            foundPwdOutput = new String(shellOutput.toByteArray()).matches("(?s)(?m).*^/home/vishal$.*");
            attempts++;
        }
        shellChannel.disconnect();
        assertTrue(foundPwdOutput);
    }
    
    @Test
    public void testImageDate() throws ParseException {
        final AmazonMachineImage image = landscape.getImage(region, "ami-01b4b27a5699e33e6");
        assertEquals(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse("2020-07-08T12:41:06+0200")),
                image.getCreatedAt());
    }
}
