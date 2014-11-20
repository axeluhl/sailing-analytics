package com.sap.sse.replication.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;

public interface ReplicableWithObjectInputStream<S, O extends OperationWithResult<S, ?>> extends Replicable<S, O> {
    static final Logger logger = Logger.getLogger(ReplicableWithObjectInputStream.class.getName());
    
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

    @Override
    default void writeOperation(OperationWithResult<?, ?> operation, OutputStream outputStream, boolean closeStream) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(operation);
        oos.flush();
        if (closeStream) {
            oos.close();
        }
    }
    
    default <T> void replicate(O operation) {
        for (OperationExecutionListener<S> listener : getOperationExecutionListeners()) {
            try {
                @SuppressWarnings("unchecked")
                final OperationWithResult<S, T> owr = (OperationWithResult<S, T>) operation;
                listener.executed(owr);
            } catch (Exception e) {
                // don't risk the master's operation only because replication to a listener/replica doesn't work
                logger.severe("Error replicating operation " + operation + " to replication listener " + listener);
                logger.log(Level.SEVERE, "replicate", e);
            }
        }
    }

    Iterable<OperationExecutionListener<S>> getOperationExecutionListeners();

    /**
     * The operation is executed by immediately {@link Operation#internalApplyTo(Object) applying} it to this
     * service object. It is then replicated to all replicas.
     * 
     * @see {@link #replicate(RacingEventServiceOperation)}
     */
    default <T> T apply(OperationWithResult<S, T> operation) {
        OperationWithResult<S, T> reso = (OperationWithResult<S, T>) operation;
        try {
            @SuppressWarnings("unchecked")
            S replicable = (S) this;
            T result = reso.internalApplyTo(replicable);
            @SuppressWarnings("unchecked") // This is necessary because otherwise apply(...) couldn't bind the result type T
            O oo = (O) operation;
            replicate(oo);
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "apply", e);
            throw new RuntimeException(e);
        }
    }
}
