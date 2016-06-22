package com.sap.sailing.domain.racelogtracking.impl.fixtracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;

/**
 * This is the main entry point of the {@link SensorFixStore} based fix tracking.
 * 
 * This listener is informed about every {@link TrackedRegatta} by {@link RacingEventService} via the implemented
 * {@link TrackedRegattaListener}. For every known {@link TrackedRegatta}, a {@link RegattaLogFixTrackerRaceListener} is
 * started.
 * 
 * In addition this is a {@link ReplicableWithObjectInputStream} because we need to know if the current node is a
 * replica. Replicas must not do any fix tracking because fixes are being loaded on the master and transferred to the
 * replicas through the replication mechanism. That's why in replication state, no
 * {@link RegattaLogFixTrackerRaceListener} instances are created at all.
 */
public class RegattaLogFixTrackerRegattaListener implements TrackedRegattaListener,
        ReplicableWithObjectInputStream<RegattaLogFixTrackerRegattaListener, OperationWithResult<RegattaLogFixTrackerRegattaListener, ?>> {
    
    private static final Logger log = Logger.getLogger(RegattaLogFixTrackerRegattaListener.class.getName());
    
    private final Map<Serializable, RegattaLogFixTrackerRaceListener> registeredTrackers = new HashMap<>();
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final SensorFixMapperFactory sensorFixMapperFactory;

    public RegattaLogFixTrackerRegattaListener(
            ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker,
            SensorFixMapperFactory sensorFixMapperFactory) {
        this.racingEventServiceTracker = racingEventServiceTracker;
        this.sensorFixMapperFactory = sensorFixMapperFactory;
    }

    @Override
    public synchronized void regattaAdded(TrackedRegatta trackedRegatta) {
        final Serializable regattaId = trackedRegatta.getRegatta().getId();
        // TODO: observe isReplica, it can change!
        if (!isReplica) {
            RegattaLogFixTrackerRaceListener tracker = new RegattaLogFixTrackerRaceListener((DynamicTrackedRegatta) 
                    trackedRegatta, racingEventServiceTracker.getService().getSensorFixStore(), sensorFixMapperFactory);
            this.stopIfNotNull(registeredTrackers.put(regattaId, tracker));
            log.fine("Added sensor data tracker to tracked regatta: " + trackedRegatta.getRegatta().getName());
        } else {
            log.warning("Regatta already known, not adding sensor twice");
        }
    }

    @Override
    public synchronized void regattaRemoved(TrackedRegatta trackedRegatta) {
        final Serializable regattaId = trackedRegatta.getRegatta().getId();
        try {
            this.stopIfNotNull(registeredTrackers.get(regattaId));
        } finally {
            registeredTrackers.remove(regattaId);
        }
    }
    
    private void stopIfNotNull(RegattaLogFixTrackerRaceListener tracker) {
        if (tracker != null) {
            try {
                tracker.stop();
            } catch (Exception exc) {
                log.log(Level.SEVERE, "Stopping of tracker failed: " + tracker, exc);
            }
        }
    }
    
    // Replication related methods and fields
    private final ConcurrentHashMap<OperationExecutionListener<RegattaLogFixTrackerRegattaListener>, OperationExecutionListener<RegattaLogFixTrackerRegattaListener>> operationExecutionListeners = new ConcurrentHashMap<>();
    private final ThreadLocal<Boolean> currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);
    private final Set<OperationWithResultWithIdWrapper<RegattaLogFixTrackerRegattaListener, ?>> operationsSentToMasterForReplication = new HashSet<>();
    private ReplicationMasterDescriptor master;
    private boolean isReplica = false;

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return master;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.master = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.master = null;
    }

    @Override
    public Iterable<OperationExecutionListener<RegattaLogFixTrackerRegattaListener>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public void addOperationExecutionListener(
            OperationExecutionListener<RegattaLogFixTrackerRegattaListener> listener) {
        this.operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(
            OperationExecutionListener<RegattaLogFixTrackerRegattaListener> listener) {
        this.operationExecutionListeners.remove(listener);
    }

    @Override
    public boolean isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster() {
        return currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(boolean b) {
        this.currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.set(b);
    }

    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<RegattaLogFixTrackerRegattaListener, ?> operationWithResultWithIdWrapper) {
        this.operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }

    @Override
    public boolean hasSentOperationToMaster(
            OperationWithResult<RegattaLogFixTrackerRegattaListener, ?> operation) {
        return operationsSentToMasterForReplication.remove(operation);
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        return new ObjectInputStream(is);
    }

    @Override
    public synchronized void initiallyFillFromInternal(ObjectInputStream is)
            throws IOException, ClassNotFoundException, InterruptedException {
        this.isReplica = true;
        this.registeredTrackers.values().forEach(this::stopIfNotNull);
        this.registeredTrackers.clear();
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
    }

    @Override
    public synchronized void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        this.isReplica = false;
    }

}
