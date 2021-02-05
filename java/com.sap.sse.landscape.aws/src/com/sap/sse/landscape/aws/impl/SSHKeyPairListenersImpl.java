package com.sap.sse.landscape.aws.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.landscape.aws.AwsLandscape;
import com.sap.sse.landscape.aws.SSHKeyPairListeners;
import com.sap.sse.landscape.ssh.SSHKeyPair;

/**
 * Manages a set of listeners which are kept as a static (class-level) reference in
 * {@link AwsLandscape} such that each time any of the {@link AwsLandscape#obtain()}
 * variants is called, all these listeners are added to the resulting landscape object
 * so they get informed about any change in SSH key pairs, no matter through which landscape
 * object instance the SSH key pair set is manipulated. This somewhat convoluted design is
 * owned to the fact that landscape objects are initialized with their AWS credentials,
 * and so for different users with different credentials different landscape objects
 * are required.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SSHKeyPairListenersImpl implements SSHKeyPairListeners {
    public static interface SSHKeyPairListener {
        void sshKeyPairAdded(SSHKeyPair sshKeyPair);
        void sshKeyPairRemoved(SSHKeyPair sshKeyPair);
    }
    
    private final Set<SSHKeyPairListener> sshKeyPairListeners;
    
    public SSHKeyPairListenersImpl() {
        sshKeyPairListeners = new HashSet<>();
    }
    
    @Override
    public void addSSHKeyPairListener(SSHKeyPairListener listener) {
        sshKeyPairListeners.add(listener);
    }
    
    @Override
    public void removeSSHKeyPairListener(SSHKeyPairListener listener) {
        sshKeyPairListeners.remove(listener);
    }
    
    @Override
    public Iterable<SSHKeyPairListener> getSshKeyPairListeners() {
        return Collections.unmodifiableCollection(sshKeyPairListeners);
    }
}
