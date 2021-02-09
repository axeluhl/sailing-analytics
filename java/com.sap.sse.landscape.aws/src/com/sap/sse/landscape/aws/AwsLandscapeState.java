package com.sap.sse.landscape.aws;

import com.jcraft.jsch.JSchException;
import com.sap.sse.landscape.Region;
import com.sap.sse.landscape.aws.impl.SSHKeyPairListenersImpl.SSHKeyPairListener;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.replication.Replicable;

public interface AwsLandscapeState extends Replicable<ReplicableAwsLandscapeState, AwsLandscapeOperation<?>> {
    void deleteKeyPair(Region region, String keyName);

    void addSSHKeyPairListeners(Iterable<SSHKeyPairListener> listeners);

    SSHKeyPair getSSHKeyPair(Region region, String keyName);

    Iterable<SSHKeyPair> getSSHKeyPairs();

    byte[] getDecryptedPrivateKey(SSHKeyPair keyPair, byte[] privateKeyEncryptionPassphrase) throws JSchException;

    void addSSHKeyPair(SSHKeyPair result);

}
