package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.ssh.SSHKeyPair;

public interface ReplicableAwsLandscapeState extends AwsLandscapeState {

    Void internalDeleteKeyPair(String regionId, String keyName);

    Void internalAddSSHKeyPair(SSHKeyPair keyPair);

}
