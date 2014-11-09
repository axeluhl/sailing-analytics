package com.sap.sse.replication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;

import org.osgi.framework.BundleContext;

import com.sap.sse.common.WithID;
import com.sap.sse.operationaltransformation.Operation;
import com.sap.sse.operationaltransformation.OperationWithTransformationSupport;

/**
 * Represents a replicable part of an application. Such a replicable part is usually holder of the application state and
 * central transaction / operation processing unit. Processing {@link Operation operations} generally happens by
 * {@link Operation#applyTo(Object) applying} operations to this replicable. As such, this replicable plays the
 * role of the <code>toState</code> argument in the {@link Operation#applyTo(Object)} method calls.<p>
 * 
 * In addition to being the operation processor, a replicable must be able to serialize its state for the initial
 * load of a replica, and it must be able to initialize its state from this serialized form.<p>
 * 
 * For operation and initial load serialization, a replicable must provide a specialized {@link ObjectInputStream} that
 * ensures that on the receiving end there are no two Java object copies representing the same domain object where this
 * matters. This is particularly important for objects with mutable state that otherwise may run inconsistent when only
 * one of the multiple copies gets modified. Typically, the object input stream uses some sort of cache and overrides
 * the {@link ObjectInputStream#resolveObject} method, and the types whose objects are de-serialized need to co-operate by
 * implementing a marker interface telling the specialized object input stream to use the cache when resolving objects
 * of that type.<p>
 * 
 * Several {@link Replicable} instances can share the same replication service and the same set of replication channels.
 * In order to participate in replication, {@link Replicable} objects need to be registered as OSGi services for the
 * {@link Replicable} interface. The replication service will dynamically discover the {@link Replicable} objects when
 * initial load is requested and when an {@link Operation} is received. The initial load streams as well as each replication
 * operation need to identify the {@link Replicable} they refer to. This identification is added as an OSGi service registry
 * parameter that is used during service discovery.
 * 
 * @param <S> the type of state to which the operations are applied; usually this will be set to the implementing subclass
 * @param <O> type of operation that the replicable accepts
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Replicable<S, O extends OperationWithResult<S, ?>> extends WithID {
    /**
     * The name of the property to use in the <code>properties</code> dictionary in a call to
     * {@link BundleContext#registerService(Class, Object, java.util.Dictionary)} when registering a {@link Replicable}.
     * The value to provide for this property is the {@link Object#toString()} serialization of the {@link #getId()}
     * result for the {@link Replicable} object.
     */
    final String OSGi_Service_Registry_ID_Property_Name = "ID";
    
    /**
     * Produces an object input stream that can choose to resolve objects against a cache so that duplicate instances
     * are avoided.
     */
    ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException;

    /**
     * Executes an operation whose effects need to be replicated to any replica of this service known and
     * {@link OperationExecutionListener#executed(OperationWithTransformationSupport) notifies} all registered
     * operation execution listeners about the execution of the operation.
     */
    <T> T apply(OperationWithResult<S, T> operation);

    void addOperationExecutionListener(OperationExecutionListener<S> listener);

    void removeOperationExecutionListener(OperationExecutionListener<S> listener);

    /**
     * Before {@link #initiallyFillFrom(ObjectInputStream) initially loading a replica's state from a master instance},
     * the replica's old state needs to be "detached". This method clears all top-level data structures and stops all
     * tracking currently going on.<p>
     * 
     * The reason this operation needs to be callable separate from {@link #initiallyFillFrom(ObjectInputStream)} is that
     * it needs to happen before subscribing to the operation feed received from the master instance through the message bus
     * which in turn needs to happen before receiving the initial load.
     */
    void clearReplicaState() throws MalformedURLException, IOException, InterruptedException;

    /**
     * Dual, reading operation for {@link #serializeForInitialReplication(ObjectOutputStream)}. In other words, when
     * this operation returns, this service instance is in a state "equivalent" to that of the service instance that
     * produced the stream contents in its {@link #serializeForInitialReplication(ObjectOutputStream)}. "Equivalent"
     * here means that a replica will have equal sets of regattas, tracked regattas, leaderboards and leaderboard groups but
     * will not have any active trackers for wind or positions because it relies on these elements to be sent through
     * the replication channel.
     * <p>
     * 
     * Tracked regattas read from the stream are observed (see {@link RaceListener}) by this object for automatic updates
     * to the default leaderboard and for automatic linking to leaderboard columns. It is assumed that no explicit
     * replication of these operations will happen based on the changes performed on the replication master.<p>
     * 
     * <b>Caution:</b> All relevant contents of this service instance needs to be cleared before by a call to
     * {@link #clearReplicaState()}. It will be replaced by the stream contents.
     */
    void initiallyFillFrom(ObjectInputStream ois) throws IOException, ClassNotFoundException, InterruptedException;

    /**
     * Produces a one-shot serializable copy of those elements required for replication into <code>oos</code> so that
     * afterwards the {@link RacingEventServiceOperation}s can be {@link #apply(RacingEventServiceOperation) applied} to
     * maintain consistency with the master copy of the service. The dual operation is {@link #initiallyFillFrom}.
     */
    void serializeForInitialReplication(ObjectOutputStream oos) throws IOException;
}
