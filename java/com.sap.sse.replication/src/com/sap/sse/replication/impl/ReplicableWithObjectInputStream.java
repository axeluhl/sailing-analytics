package com.sap.sse.replication.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;

public interface ReplicableWithObjectInputStream<S, O extends OperationWithResult<S, ?>> extends Replicable<S, O> {
    /**
     * Produces an object input stream that can choose to resolve objects against a cache so that duplicate instances
     * are avoided.
     */
    ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException;

    /**
     * Implementation of {@link #initiallyFillFrom(InputStream)} which receives an {@link ObjectInputStream} instead of
     * an {@link InputStream}. The {@link ObjectInputStream} is expected to have been produced by
     * {@link #createObjectInputStreamResolvingAgainstCache(InputStream)}.
     */
    void initiallyFillFromInternal(ObjectInputStream is) throws IOException, ClassNotFoundException, InterruptedException;

    /**
     * Implementation of {@link #serializeForInitialReplication(OutputStream)}, using an {@link ObjectOutputStream}.
     */
    void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException;

    /**
     * Implementation of {@link #readOperation(InputStream)}, using the {@link ObjectInputStream} created by
     * {@link #createObjectInputStreamResolvingAgainstCache(InputStream)}.
     */
    @SuppressWarnings("unchecked")
    default O readOperationInternal(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return (O) ois.readObject();
    }

    @Override
    default void initiallyFillFrom(InputStream is) throws IOException, ClassNotFoundException, InterruptedException {
        initiallyFillFromInternal(createObjectInputStreamResolvingAgainstCache(is));
    }
    
    /**
     * Wraps <code>os</code> by an {@link ObjectOutputStream} and invokes
     * {@link #serializeForInitialReplicationInternal(ObjectOutputStream)}.
     */
    @Override
    default void serializeForInitialReplication(OutputStream os) throws IOException {
        serializeForInitialReplicationInternal(new ObjectOutputStream(os));
    }
    
    @Override
    default O readOperation(InputStream inputStream) throws IOException, ClassNotFoundException {
        return readOperationInternal(createObjectInputStreamResolvingAgainstCache(inputStream));
    }

}
