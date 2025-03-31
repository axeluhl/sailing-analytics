package com.sap.sse.landscape.aws.persistence;

import com.sap.sse.landscape.ssh.SSHKeyPair;

public interface DomainObjectFactory {

    Iterable<SSHKeyPair> loadSSHKeyPairs();

}
