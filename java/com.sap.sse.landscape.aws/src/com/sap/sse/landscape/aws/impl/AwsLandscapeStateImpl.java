package com.sap.sse.landscape.aws.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;

import com.sap.sse.landscape.aws.AwsLandscapeOperation;
import com.sap.sse.landscape.aws.ReplicableAwsLandscapeState;
import com.sap.sse.replication.impl.AbstractReplicableWithObjectInputStream;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public class AwsLandscapeStateImpl extends AbstractReplicableWithObjectInputStream<ReplicableAwsLandscapeState, AwsLandscapeOperation<?>> implements ReplicableAwsLandscapeState {

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        // TODO Implement Replicable<ReplicableAwsLandscapeState,AwsLandscapeOperation<?>>.clearReplicaState(...)
        
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStreamResolvingAgainstCache<Object>(is, /* dummy "cache" */ new Object(), /* resolve listener */ null) {
        }; // use anonymous inner class in this class loader to see all that this class sees
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        // TODO Implement ReplicableWithObjectInputStream<ReplicableAwsLandscapeState,AwsLandscapeOperation<?>>.initiallyFillFromInternal(...)
        
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        // TODO Implement ReplicableWithObjectInputStream<ReplicableAwsLandscapeState,AwsLandscapeOperation<?>>.serializeForInitialReplicationInternal(...)
        
    }

    @Override
    public void clearState() throws Exception {
        // TODO Implement ClearStateTestSupport.clearState(...)
        
    }
}
