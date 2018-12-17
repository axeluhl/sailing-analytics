package com.sap.sse.replication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.WithID;
import com.sap.sse.operationaltransformation.Operation;

public interface ReplicableWithObjectInputStream<S, O extends OperationWithResult<S, ?>> extends Replicable<S, O> {
    static final Logger logger = Logger.getLogger(ReplicableWithObjectInputStream.class.getName());
    
    /**
     * When an operation is applied that is an {@link OperationWithResultWithIdWrapper} then its ID is stored
     * in this thread local and removed when the local execution of that operation has finished. When during the
     * execution of the operation another operation is fired to the replicas registered, it will be wrapped
     * by an {@link OperationWithResultWithIdWrapper} that has this ID. This will let the replica that originally
     * passed this operation to this master recognize that it is receiving the operation it triggered itself, so
     * it can safely be ignored on that replica. This procedure is important for operations that have
     * {@link OperationWithResult#isRequiresExplicitTransitiveReplication()} return <code>false</code> because they
     * implicitly replicate their effects while being executed.
     */
    static final ThreadLocal<Serializable> idOfOperationBeingExecuted = new ThreadLocal<Serializable>();
    
    static final AtomicInteger operationCounter = new AtomicInteger(0);
    
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
        assert !isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(); // no nested receiving of initial load
        setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(true);
        try {
            final ObjectInputStream objectInputStream = createObjectInputStreamResolvingAgainstCache(is);
            ClassLoader oldContextClassloader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getDeserializationClassLoader());
            try {
                initiallyFillFromInternal(objectInputStream);
            } finally {
                Thread.currentThread().setContextClassLoader(oldContextClassloader);
            }
        } finally {
            setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(false);
        }
    }
    
    /**
     * The class loader to use for de-serializing objects. By default, this object's class's class loader is used.
     */
    default ClassLoader getDeserializationClassLoader() {
        return getClass().getClassLoader();
    }
    
    /**
     * Wraps <code>os</code> by an {@link ObjectOutputStream} and invokes
     * {@link #serializeForInitialReplicationInternal(ObjectOutputStream)}.
     */
    @Override
    default void serializeForInitialReplication(OutputStream os) throws IOException {
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        serializeForInitialReplicationInternal(objectOutputStream);
        objectOutputStream.flush();
    }
    
    @Override
    default O readOperation(InputStream inputStream) throws IOException, ClassNotFoundException {
        ClassLoader oldContextClassloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getDeserializationClassLoader());
        try {
            return readOperationInternal(createObjectInputStreamResolvingAgainstCache(inputStream));
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassloader);
        }
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
        @SuppressWarnings("unchecked")
        final OperationWithResult<S, T> castOperation = (OperationWithResult<S, T>) operation;
        // if this is a replica and this replicable is not currently in the process of handling replication data (either an operation
        // or the initial load) coming from the master, send the operation back to the master
        if (getMasterDescriptor() != null && !isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster()) {
            try {
                sendReplicaInitiatedOperationToMaster(castOperation);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception trying to send operation "+operation+" to master for replication", e);
            }
        }
        replicateReplicated(operation); // this anticipates receiving the operation back from master; then, the operation will be ignored;
        // see also addOperationSentToMasterForReplication
    }

    /**
     * Replicates <code>operation</code> to any replica registered. This is different from {@link #replicate(OperationWithResult)}
     * which would also send the operation to a master if this is a replica. If on the current call stack there is an operation
     * being {@link #apply(OperationWithResult) applied} that was received from a replica (indicated by the operation
     * being a {@link WithID} instance) and the operation to be sent to the replicas does not {@link WithID carry an ID},
     * a new {@link OperationWithResultWithIdWrapper ID decorator} is wrapped around the <code>operation</code> with the ID
     * of the call stack's operation currently being {@link #apply(OperationWithResult) applied}. This will tell the replicas
     * that <code>operation</code> is just replicating the effects of another operation, and the replica where the original
     * operation was initiated can therefore safely ignore <code>operation</code> when receiving it.
     */
    default <T> void replicateReplicated(O operation) {
        @SuppressWarnings("unchecked")
        final OperationWithResult<S, T> owr = (OperationWithResult<S, T>) operation;
        final Serializable idOfCurrentlyExecutingOperation = idOfOperationBeingExecuted.get();
        final OperationWithResult<S, T> operationToNotify;
        if (!(owr instanceof OperationWithResultWithIdWrapper<?, ?>) && idOfCurrentlyExecutingOperation != null) {
            operationToNotify = new OperationWithResultWithIdWrapper<S, T>(owr, idOfCurrentlyExecutingOperation);
        } else {
            operationToNotify = owr;
        }
        for (OperationExecutionListener<S> listener : getOperationExecutionListeners()) {
            final int operationCount = operationCounter.incrementAndGet();
            logger.fine(()->""+operationCount+": Replicating "+operation);
            try {
                listener.executed(operationToNotify);
            } catch (Exception e) {
                // don't risk the master's operation only because replication to a listener/replica doesn't work
                logger.severe("Error replicating operation " + operationToNotify + " to replication listener " + listener);
                logger.log(Level.SEVERE, "replicate", e);
            }
        }
    }

    Iterable<OperationExecutionListener<S>> getOperationExecutionListeners();

    /**
     * When a replica has initiated (not received through replication) an operation, this operation needs to be sent to
     * the master for execution from where it will replicate across the replication tree. This method uses the
     * {@link ReplicationMasterDescriptor#getSendReplicaInitiatedOperationToMasterURL(String) URL for sending an
     * operation to the replication servlet on the master} and through the POST request's output stream first sends the
     * target replicable's ID as a string using a {@link DataOutputStream}, then
     * {@link #writeOperation(OperationWithResult, OutputStream, boolean) serializes the operation}.
     */
    default <T> void sendReplicaInitiatedOperationToMaster(OperationWithResult<S, T> operation) throws IOException {
        ReplicationMasterDescriptor masterDescriptor = getMasterDescriptor();
        assert masterDescriptor != null;
        final OperationWithResultWithIdWrapper<S, T> operationWithResultWithIdWrapper = new OperationWithResultWithIdWrapper<S, T>(operation);
        addOperationSentToMasterForReplication(operationWithResultWithIdWrapper);
        URL url = masterDescriptor.getSendReplicaInitiatedOperationToMasterURL(this.getId().toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true); // we want to post the serialized operation
        connection.setRequestMethod("POST");
        logger.info("Sending operation "+operation+" to master "+masterDescriptor+"'s replicable with ID "+this+" for initial execution and replication");
        connection.connect();
        OutputStream outputStream = connection.getOutputStream();
        DataOutputStream dos = new DataOutputStream(outputStream);
        dos.writeUTF(getId().toString());
        this.writeOperation(operationWithResultWithIdWrapper, outputStream, /* closeStream */ true);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        bufferedReader.close();
    }

    /**
     * Checks whether this replicable is a replica. If yes, the operation is executed locally and sent to the master
     * server for execution. Otherwise, {@link #applyReplicated(OperationWithResult)} is invoked which executes and
     * replicates the operation immediately.
     */
    default <T> T apply(OperationWithResult<S, T> operation) {
        boolean needToRemoveThreadLocal = false;
        try {
            if (operation instanceof OperationWithResultWithIdWrapper<?, ?>) {
                idOfOperationBeingExecuted.set(((OperationWithResultWithIdWrapper<?, ?>) operation).getId());
                needToRemoveThreadLocal = true;
            }
            ReplicationMasterDescriptor masterDescriptor = getMasterDescriptor();
            final T result = applyReplicated(operation);
            if (masterDescriptor != null) {
                sendReplicaInitiatedOperationToMaster(operation);
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "apply", e);
            throw new RuntimeException(e);
        } finally {
            if (needToRemoveThreadLocal) {
                idOfOperationBeingExecuted.remove();
            }
        }
    }

    void addOperationSentToMasterForReplication(OperationWithResultWithIdWrapper<S, ?> operationWithResultWithIdWrapper);

    /**
     * @return the descriptor of the master from which this replica is replicating this {@link Replicable}, or
     *         {@code null} if this {@link Replicable} is currently running as a master.
     */
    ReplicationMasterDescriptor getMasterDescriptor();

    /**
     * The operation is executed by immediately {@link Operation#internalApplyTo(Object) applying} it to this
     * service object. It is then replicated to all replicas if and only if the operation is marked as
     * {@link OperationWithResult#isRequiresExplicitTransitiveReplication()}.
     * 
     * @see {@link #replicate(RacingEventServiceOperation)}
     */
    default <T> T applyReplicated(OperationWithResult<S, T> operation) {
        OperationWithResult<S, T> reso = (OperationWithResult<S, T>) operation;
        try {
            setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(true);
            @SuppressWarnings("unchecked")
            S replicable = (S) this;
            T result = reso.internalApplyTo(replicable);
            @SuppressWarnings("unchecked") // This is necessary because otherwise apply(...) couldn't bind the result type T
            O oo = (O) operation;
            if (oo.isRequiresExplicitTransitiveReplication()) {
                replicateReplicated(oo);
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "apply", e);
            throw new RuntimeException(e);
        } finally {
            setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(false);
        }
    }
}
