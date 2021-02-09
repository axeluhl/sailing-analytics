package com.sap.sse.landscape.aws.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.landscape.aws.AwsLandscapeOperation;
import com.sap.sse.landscape.aws.ReplicableAwsLandscapeState;
import com.sap.sse.landscape.aws.impl.SSHKeyPairListenersImpl.SSHKeyPairListener;
import com.sap.sse.landscape.aws.persistence.DomainObjectFactory;
import com.sap.sse.landscape.aws.persistence.MongoObjectFactory;
import com.sap.sse.landscape.aws.persistence.PersistenceFactory;
import com.sap.sse.landscape.ssh.SSHKeyPair;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.replication.impl.AbstractReplicableWithObjectInputStream;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

/**
 * Replicable state that we want to keep about an AWS landscape. In particular, the set of SSH keys is managed here and
 * can be {@link #addSSHKeyPairListeners(Iterable) observed} using {@link SSHKeyPairListener} objects. The set of SSH
 * keys managed here is stored persistently. When creating this object, the keys are read from the persistent store.
 * Adding and deleting SSH keys is replicated, and the effects are stored persistently.
 * <p>
 * 
 * This object does not offer services that actually manipulate or read from the AWS landscape itself. See
 * {@link AwsLandscape} for this type of service.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AwsLandscapeStateImpl extends AbstractReplicableWithObjectInputStream<ReplicableAwsLandscapeState, AwsLandscapeOperation<?>> implements ReplicableAwsLandscapeState {
    private final MongoObjectFactory mongoObjectFactory;
    private ConcurrentMap<Pair<String, String>, SSHKeyPair> sshKeyPairs;
    private final Set<SSHKeyPairListener> sshKeyPairListeners;
    
    public AwsLandscapeStateImpl() {
        this(// by using MongoDBService.INSTANCE the default test configuration will be used if nothing else is configured
             PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE),
             PersistenceFactory.INSTANCE.getMongoObjectFactory(MongoDBService.INSTANCE));
    }
    
    public AwsLandscapeStateImpl(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory) {
        this.mongoObjectFactory = mongoObjectFactory;
        this.sshKeyPairs = new ConcurrentHashMap<Util.Pair<String,String>, SSHKeyPair>();
        this.sshKeyPairListeners = new HashSet<>();
        for (final SSHKeyPair keyPair : domainObjectFactory.loadSSHKeyPairs()) {
            addKeyPairToMap(keyPair);
        }
    }
    
    /**
     * No persistence, no replication.
     */
    private void addKeyPairToMap(SSHKeyPair keyPair) {
        sshKeyPairs.put(new Pair<>(keyPair.getRegionId(), keyPair.getName()), keyPair);
    }
    
    
    @Override
    public void addSSHKeyPair(final SSHKeyPair result) {
        apply(s->s.internalAddSSHKeyPair(result));
    }
    
    @Override
    public Void internalAddSSHKeyPair(SSHKeyPair keyPair) {
        for (final SSHKeyPairListener sshKeyPairListener : sshKeyPairListeners) {
            sshKeyPairListener.sshKeyPairAdded(keyPair);
        }
        addKeyPairToMap(keyPair);
        mongoObjectFactory.storeSSHKeyPair(keyPair);
        return null;
    }
    
    @Override
    public void deleteKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        final String regionId = region.getId();
        apply(s->s.internalDeleteKeyPair(regionId, keyName));
    }

    @Override
    public Void internalDeleteKeyPair(String regionId, String keyName) {
        final SSHKeyPair removedKeyPair = sshKeyPairs.remove(new Pair<>(regionId, keyName));
        for (final SSHKeyPairListener sshKeyPairListener : sshKeyPairListeners) {
            sshKeyPairListener.sshKeyPairRemoved(removedKeyPair);
        }
        mongoObjectFactory.removeSSHKeyPair(regionId, keyName);
        return null;
    }

    @Override
    public void addSSHKeyPairListeners(Iterable<SSHKeyPairListener> listeners) {
        Util.addAll(listeners, this.sshKeyPairListeners);
    }

    @Override
    public SSHKeyPair getSSHKeyPair(com.sap.sse.landscape.Region region, String keyName) {
        return sshKeyPairs.get(new Pair<>(region.getId(), keyName));
    }
    
    @Override
    public Iterable<SSHKeyPair> getSSHKeyPairs() {
        return Collections.unmodifiableCollection(sshKeyPairs.values());
    }

    @Override
    public byte[] getDecryptedPrivateKey(SSHKeyPair keyPair, byte[] privateKeyEncryptionPassphrase) throws JSchException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        keyPair.getKeyPair(new JSch(), privateKeyEncryptionPassphrase).writePrivateKey(bos);
        return bos.toByteArray();
    }

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        sshKeyPairs.clear();
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStreamResolvingAgainstCache<Object>(is, /* dummy "cache" */ new Object(), /* resolve listener */ null) {
        }; // use anonymous inner class in this class loader to see all that this class sees
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        @SuppressWarnings("unchecked")
        final Map<Pair<String, String>, SSHKeyPair> sshKeyPairsReadFromStream = (Map<Pair<String, String>, SSHKeyPair>) is.readObject();
        sshKeyPairs.putAll(sshKeyPairsReadFromStream);
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(sshKeyPairs);
    }
}
