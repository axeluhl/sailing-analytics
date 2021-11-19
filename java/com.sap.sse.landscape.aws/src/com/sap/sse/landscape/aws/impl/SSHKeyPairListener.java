package com.sap.sse.landscape.aws.impl;

import com.sap.sse.landscape.ssh.SSHKeyPair;

public interface SSHKeyPairListener {
    void sshKeyPairAdded(SSHKeyPair sshKeyPair);
    void sshKeyPairRemoved(SSHKeyPair sshKeyPair);
}