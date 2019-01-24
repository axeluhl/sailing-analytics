package com.sap.sailing.windestimation.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.OperationsToMasterSender;
import com.sap.sse.replication.ReplicableWithObjectInputStream;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.UnsentOperationsToMasterSender;
import com.sap.sse.util.ClearStateTestSupport;

public abstract class AbstractReplicableWithObjectInputStream<S, O extends OperationWithResult<S, ?>>
        implements ReplicableWithObjectInputStream<S, O>, ClearStateTestSupport {

    /**
     * This field is expected to be set by the {@link ReplicationService} once it has "adopted" this replicable. The
     * {@link ReplicationService} "injects" this service so it can be used here as a delegate for the
     * {@link UnsentOperationsToMasterSender#retrySendingLater(OperationWithResult, OperationsToMasterSender)} method.
     */
    private UnsentOperationsToMasterSender unsentOperationsToMasterSender;

    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not
     * currently replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;

    private final Set<OperationWithResult<S, ?>> operationsSentToMasterForReplication;

    private ThreadLocal<Boolean> currentlyFillingFromInitialLoad = ThreadLocal.withInitial(() -> false);

    private ThreadLocal<Boolean> currentlyApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);

    private final ConcurrentMap<OperationExecutionListener<S>, OperationExecutionListener<S>> operationExecutionListeners;

    public AbstractReplicableWithObjectInputStream() {
        this.operationsSentToMasterForReplication = new HashSet<>();
        this.operationExecutionListeners = new ConcurrentHashMap<>();
    }

    @Override
    public void setUnsentOperationToMasterSender(UnsentOperationsToMasterSender service) {
        this.unsentOperationsToMasterSender = service;
    }

    @Override
    public <S1, O1 extends OperationWithResult<S1, ?>, T> void retrySendingLater(
            OperationWithResult<S1, T> operationWithResult, OperationsToMasterSender<S1, O1> sender) {
        if (unsentOperationsToMasterSender != null) {
            unsentOperationsToMasterSender.retrySendingLater(operationWithResult, sender);
        }
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
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
        return currentlyFillingFromInitialLoad.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoad(boolean currentlyFillingFromInitialLoad) {
        this.currentlyFillingFromInitialLoad.set(currentlyFillingFromInitialLoad);
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
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(is);
        return ois;
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
