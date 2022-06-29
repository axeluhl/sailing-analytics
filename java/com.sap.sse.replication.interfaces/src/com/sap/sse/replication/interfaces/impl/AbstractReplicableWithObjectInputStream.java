package com.sap.sse.replication.interfaces.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.OperationsToMasterSendingQueue;
import com.sap.sse.replication.ReplicableWithObjectInputStream;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationReceiver;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.util.ClearStateTestSupport;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

/**
 * Subclasses must implement {@link #createObjectInputStreamResolvingAgainstCache(InputStream, Map)}, usually by instantiating an anonymous inner class
 * that is subclass of {@link ObjectInputStreamResolvingAgainstCache}, as in
 * <pre>
 *  public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
 *      return new ObjectInputStreamResolvingAgainstCache&lt;Object&gt;(is,
 *             new Object(), // dummy cache
 *             null) {       // resolve listener
 *      };
 *  }
 * </pre>
 * This way, the class is loaded by the same class loader that also loads the replicable's implementation class and hence sees all
 * the same classes that the replicable sees. This is important for the de-serialization process, especially for the operation objects.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <S>
 * @param <O>
 */
public abstract class AbstractReplicableWithObjectInputStream<S, O extends OperationWithResult<S, ?>>
        implements ReplicableWithObjectInputStream<S, O>, ClearStateTestSupport {

    /**
     * This field is expected to be set by the {@link ReplicationService} once it has "adopted" this replicable. The
     * {@link ReplicationService} "injects" this service so it can be used here as a delegate for the
     * {@link UnsentOperationsToMasterSender#retrySendingLater(OperationWithResult, OperationsToMasterSender)} method.
     */
    private OperationsToMasterSendingQueue unsentOperationsToMasterSender;

    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not
     * currently replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;

    private final Set<OperationWithResult<S, ?>> operationsSentToMasterForReplication;

    private volatile boolean currentlyFillingFromInitialLoad;

    private ThreadLocal<Boolean> currentlyApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);

    private final ConcurrentMap<OperationExecutionListener<S>, OperationExecutionListener<S>> operationExecutionListeners;

    public AbstractReplicableWithObjectInputStream() {
        this.operationsSentToMasterForReplication = new HashSet<>();
        this.operationExecutionListeners = new ConcurrentHashMap<>();
        this.currentlyFillingFromInitialLoad = false;
    }

    @Override
    public void setUnsentOperationToMasterSender(OperationsToMasterSendingQueue service) {
        this.unsentOperationsToMasterSender = service;
    }

    @Override
    public <S1, O1 extends OperationWithResult<S1, ?>, T1> void scheduleForSending(
            O1 operationWithResult, OperationsToMasterSender<S1, O1> sender) {
        if (unsentOperationsToMasterSender != null) {
            unsentOperationsToMasterSender.scheduleForSending(operationWithResult, sender);
        }
    }

    /**
     * The default implementation calls {@link #clearReplicaState()}. Subclasses may override in case they need more special
     * test case clearing support.
     */
    @Override
    public void clearState() throws Exception {
        clearReplicaState();
    }

    @Override
    public Serializable getId() {
        final String result = getClass().getName();
        assert !result.equals(ReplicationReceiver.VERSION_INDICATOR);
        return result;
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener<S> listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<S> listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoad() {
        return currentlyFillingFromInitialLoad;
    }

    @Override
    public void setCurrentlyFillingFromInitialLoad(boolean currentlyFillingFromInitialLoad) {
        this.currentlyFillingFromInitialLoad = currentlyFillingFromInitialLoad;
    }

    @Override
    public boolean isCurrentlyApplyingOperationReceivedFromMaster() {
        return currentlyApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyApplyingOperationReceivedFromMaster(boolean currentlyApplyingOperationReceivedFromMaster) {
        this.currentlyApplyingOperationReceivedFromMaster.set(currentlyApplyingOperationReceivedFromMaster);
    }

    @Override
    public Iterable<OperationExecutionListener<S>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return replicatingFromMaster;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = null;
    }

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<S, ?> operation) {
        return this.operationsSentToMasterForReplication.contains(operation);
    }

    @Override
    public void addOperationSentToMasterForReplication(OperationWithResultWithIdWrapper<S, ?> operation) {
        this.operationsSentToMasterForReplication.add(operation);
    }
}
