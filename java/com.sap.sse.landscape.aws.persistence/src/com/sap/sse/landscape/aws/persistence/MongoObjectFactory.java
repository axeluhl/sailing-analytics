package com.sap.sse.landscape.aws.persistence;

import com.sap.sse.landscape.ssh.SSHKeyPair;

public interface MongoObjectFactory {
    void storeSSHKeyPair(SSHKeyPair keyPair);

    void removeSSHKeyPair(String regionId, String keyName);
    
    void clear();
}
