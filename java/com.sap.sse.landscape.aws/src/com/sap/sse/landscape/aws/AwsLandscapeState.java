package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.aws.impl.AwsLandscapeStateImpl;
import com.sap.sse.landscape.aws.impl.SSHKeyPairListener;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.replication.Replicable;

public interface AwsLandscapeState extends Replicable<ReplicableAwsLandscapeState, AwsLandscapeOperation<?>> {
    String REPLICABLE_FULLY_QUALIFIED_CLASSNAME = AwsLandscapeStateImpl.class.getName();

    void deleteKeyPair(String regionId, String keyName);

    void addSSHKeyPairListener(SSHKeyPairListener listener);

    void removeSSHKeyPairListener(SSHKeyPairListener listener);

    SSHKeyPair getSSHKeyPair(String regionId, String keyName);

    Iterable<SSHKeyPair> getSSHKeyPairs();

    void addSSHKeyPair(SSHKeyPair result);

}
