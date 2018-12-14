package com.sap.sse.replication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Logger;


import com.sap.sse.common.WithID;
import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.operationaltransformation.OperationWithTransformationSupport;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;
import com.sap.sse.util.ThreadLocalTransporter;

/**
 * Represents a replicable part of an application. Such a replicable part is usually holder of application state and a
 * transaction / operation processing unit. Processing {@link Operation operations} generally happens by
 * {@link Operation#applyTo(Object) applying} operations to this replicable. As such, this replicable plays the role of
 * the <code>toState</code> argument in the {@link Operation#applyTo(Object)} method calls.
 * <p>
 * 
 * In addition to being the operation processor, a replicable must be able to serialize its state for the initial load
 * of a replica, and it must be able to initialize its state from this serialized form. In doing so it is important to
 * not include state that is controlled by another replicable. References to such objects need to be <em>transient</em>
 * which means they are not serialized together with the initial load of this replica. If such references need to be
 * re-established after de-serialization then they need to represented by some key which can later be looked up in the
 * other replicable which can be discovered through the OSGi service registry on the receiving end.
 * <p>
 * 
 * For operation and initial load serialization, a replicable is provided an {@link InputStream} from which to read the
 * operations and the initial load. A typical implementation for reading the stream's contents will use a specialized
 * {@link ObjectInputStream} such as a specialization of {@link ObjectInputStreamResolvingAgainstCache} that ensures
 * that on the receiving end there are no two Java object copies representing the same domain object where this matters.
 * This is particularly important for objects with mutable state that otherwise may run inconsistent when only one of
 * the multiple copies gets modified. Typically, the object input stream uses some sort of cache and overrides the
 * {@link ObjectInputStream#resolveObject} method, and the types whose objects are de-serialized need to co-operate by
 * implementing a marker interface telling the specialized object input stream to use the cache when resolving objects
 * of that type.
 * <p>
 * 
 * Several {@link Replicable} instances can share the same replication service and the same set of replication channels.
 * In order to participate in replication, {@link Replicable} objects need to be registered as OSGi services for the
 * {@link Replicable} interface. The replication service will dynamically discover the {@link Replicable} objects when
 * initial load is requested and when an {@link Operation} is received. The initial load streams as well as each
 * replication operation need to identify the {@link Replicable} they refer to. This identification is added as an OSGi
 * service registry parameter that is used during service discovery.
 * 
 * @param <S>
 *            the type of state to which the operations are applied; usually this will be set to the implementing
 *            subclass
 * @param <O>
 *            type of operation that the replicable accepts
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Replicable<S, O extends OperationWithResult<S, ?>> extends Replicator<S, O>, WithID {
    static final Logger logger = Logger.getLogger(Replicable.class.getName());
    
    /**
     * The name of the property to use in the <code>properties</code> dictionary in a call to
     * {@link BundleContext#registerService(Class, Object, java.util.Dictionary)} when registering a {@link Replicable}.
     * The value to provide for this property is the {@link Object#toString()} serialization of the {@link #getId()}
     * result for the {@link Replicable} object.
     */
    final String OSGi_Service_Registry_ID_Property_Name = "ID";
    
    /**
     * If this object is not a replica, executes the <code>operation</code>. By
     * {@link OperationExecutionListener#executed(OperationWithTransformationSupport) notifying} all registered
     * operation execution listeners about the execution of the operation, the <code>operation</code> will in particular
     * be replicated to all replicas registered.
     * <p>
     * 
     * If this object is a replica, the operation will not be executed locally and will instead be forwarded to the
     * master server for execution from where it is expected to replicate to all replicas including this object where
     * the {@link #applyReplicated(OperationWithResult)} method will then carry out the operation.
     * <p>
     * 
     * To determine whether this {@link Replicable} is a replica, this method uses the
     * {@link ReplicationService#getReplicatingFromMaster()} method which also provides the master server's connectivity
     * information required to forward the <code>operation</code>.
     */
    <T> T apply(OperationWithResult<S, T> operation);

    /**
     * Executes an operation received from another (usually "master") server where this object lives on a replica. The
     * <code>operation</code>'s effects also need to be replicated to any replica of this service known and
     * {@link OperationExecutionListener#executed(OperationWithTransformationSupport) notifies} all registered operation
     * execution listeners about the execution of the operation.<p>
     * 
     * One important difference to {@link #apply(OperationWithResult)} is that the operation will be applied immediately
     * in any case whereas {@link #apply(OperationWithResult)} will check first if this is a replica and in that case
     * forward the operation to the master for first execution instead of initiating the execution on the replica.
     */
    <T> T applyReplicated(OperationWithResult<S, T> operation);
    
    void startedReplicatingFrom(ReplicationMasterDescriptor master);
    
    void stoppedReplicatingFrom(ReplicationMasterDescriptor master);

    /**
     * An operation execution listener must be able to process notifications of operations being executed that have
     * type <code>S</code> or any more specific type. The listener can achieve this also by accepting any type more general than
     * <code>S</code>.
     */
    void addOperationExecutionListener(OperationExecutionListener<S> listener);

    void removeOperationExecutionListener(OperationExecutionListener<S> listener);

    /**
     * Before {@link #initiallyFillFrom(ObjectInputStream) initially loading a replica's state from a master instance},
     * the replica's old state needs to be "detached". This method clears all top-level in-memory data structures and stops all
     * tracking currently going on. It may choose to leave any persistent content unchanged as the persistence layer is in
     * an undefined state on a replica anyhow. This seems like the safer bet, particularly in case of an accidental
     * mis-configuration of the replica's DB connection parameters which may lead to an inadvertent overwriting of the
     * master's DB contents.<p>
     * 
     * The reason this operation needs to be callable separate from {@link #initiallyFillFrom(ObjectInputStream)} is that
     * it needs to happen before subscribing to the operation feed received from the master instance through the message bus
     * which in turn needs to happen before receiving the initial load.
     */
    void clearReplicaState() throws MalformedURLException, IOException, InterruptedException;

    /**
     * Dual, reading operation for {@link #serializeForInitialReplication(OutputStream)}. In other words, when this
     * operation returns, this service instance is in a state "equivalent" to that of the service instance that produced
     * the stream contents in its {@link #serializeForInitialReplication(OutputStream)}. "Equivalent" here means that a
     * replica will have equal sets of objects serialized in the initial load but will usually not have any transient
     * processes replicated that are responsible for building and maintaining the state on the master side because it
     * relies on these elements to be sent through the replication channel.
     * <p>
     * 
     * <b>Caution:</b> All relevant contents of this service instance needs to be cleared before by a call to
     * {@link #clearReplicaState()}. It will be replaced by the stream contents.
     */
    void initiallyFillFrom(InputStream is) throws IOException, ClassNotFoundException, InterruptedException;

    /**
     * Produces a one-shot serializable copy of those elements required for replication into <code>os</code> by wrapping
     * that stream by an {@link ObjectOutputStream} so that afterwards the {@link OperationWithResult}s can be
     * {@link #apply(OperationWithResult) applied} to maintain consistency with the master copy of the service. The dual
     * operation is {@link #initiallyFillFrom}.
     */
    void serializeForInitialReplication(OutputStream os) throws IOException;

    /**
     * From an input stream, reads an operation that can be {@link #apply(OperationWithResult) applied} to this object.
     * Separating reading and applying gives clients an opportunity to queue operations, e.g., in order to wait until
     * receiving and {@link #initiallyFillFrom(InputStream) filling} the initial load has completed.
     */
    O readOperation(InputStream inputStream) throws IOException, ClassNotFoundException;

    /**
     * Writes an operation to an output stream such that it can be read by {@link #readOperation}.
     * 
     * @param closeStream
     *            if <code>true</code>, the stream will be closed after having written the operation; in any case, the
     *            content written will be flushed to the <code>outputStream</code> so that the caller may continue to
     *            invoke this method for other operations and/or on other replicables without producing corrupt data.
     */
    void writeOperation(OperationWithResult<?, ?> operation, OutputStream outputStream, boolean closeStream) throws IOException;

    /**
     * Checks if {@link #hasSentOperationToMaster(OperationWithResultWithIdWrapper) the operation was previously
     * sent to the master}. If so, the operation is ignored because it has been applied before to this replica.
     * Otherwise, it is locally applied and replicated, using a call to {@link #applyReplicated(OperationWithResult)}. 
     */
    default void applyReceivedReplicated(OperationWithResult<S, ?> operation) {
        if (!hasSentOperationToMaster(operation)) {
            assert !isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster();
            try {
                setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(true);
                applyReplicated(operation);
            } finally {
                setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(false);
            }
        } else {
            logger.fine("Ignoring operation "+operation+" received back from master after having sent it there for execution and replication earlier");
        }
    }

    /**
     * Responds with what has been passed to the last invocation to
     * {@link #setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(boolean)} in the calling thread;
     * the default is <code>false</code>. This is required in order to not replicate operations triggered on the replica
     * while receiving the initial load back to the master.
     */
    boolean isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster();
    
    /**
     * {@link #isCurrentlyFillingFromInitialLoad} responds with what has been passed to the last invocation to this
     * method in the calling thread; the default is <code>false</code>. This is required in order to not replicate
     * operations triggered on the replica while receiving the initial load back to the master.
     */
    void setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(boolean b);

    default ThreadLocalTransporter getThreadLocalTransporterForCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster() {
        return new ThreadLocalTransporter() {
            private boolean currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster;
            private boolean currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMasterAtBeginningOfTask;
            
            @Override
            public void rememberThreadLocalStates() {
                currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster();
            }

            @Override
            public void pushThreadLocalStates() {
                currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMasterAtBeginningOfTask = isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster();
                setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster);
            }

            @Override
            public void popThreadLocalStates() {
                setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMasterAtBeginningOfTask);
            }
        };
    }
    
    /**
     * If an operation equal to <code>operationWithResultWithIdWrapper</code> has previously been passed to a call to
     * {@link #addOperationSentToMasterForReplication(OperationWithResultWithIdWrapper)}, the call returns <code>true</code>
     * exactly once.
     */
    boolean hasSentOperationToMaster(OperationWithResult<S, ?> operation);
    
}
