package com.sap.sse.landscape.aws;

import com.sap.sse.landscape.aws.impl.SSHKeyPairListenersImpl.SSHKeyPairListener;

public interface SSHKeyPairListeners {

    void addSSHKeyPairListener(SSHKeyPairListener listener);

    void removeSSHKeyPairListener(SSHKeyPairListener listener);

    Iterable<SSHKeyPairListener> getSshKeyPairListeners();

}
