package com.sap.sailing.domain.racelogtracking.impl;

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

public class RegattaLogSensorDataTrackerTrackedRegattaListener implements TrackedRegattaListener,
        ReplicableWithObjectInputStream<RegattaLogSensorDataTrackerTrackedRegattaListener, OperationWithResult<RegattaLogSensorDataTrackerTrackedRegattaListener, ?>> {
    
    private static final Logger log = Logger.getLogger(RegattaLogSensorDataTrackerTrackedRegattaListener.class.getName());
    
    private final Map<Serializable, RegattaLogSensorDataTracker> registeredTrackers = new HashMap<>();
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final SensorFixMapperFactory sensorFixMapperFactory;

    public RegattaLogSensorDataTrackerTrackedRegattaListener(
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
            RegattaLogSensorDataTracker tracker = new RegattaLogSensorDataTracker((DynamicTrackedRegatta) 
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
    
    private void stopIfNotNull(RegattaLogSensorDataTracker tracker) {
        if (tracker != null) {
            try {
                tracker.stop();
            } catch (Exception exc) {
                log.log(Level.SEVERE, "Stopping of tracker failed: " + tracker, exc);
            }
        }
    }
    
    // Replication related methods and fields
    private final ConcurrentHashMap<OperationExecutionListener<RegattaLogSensorDataTrackerTrackedRegattaListener>, OperationExecutionListener<RegattaLogSensorDataTrackerTrackedRegattaListener>> operationExecutionListeners = new ConcurrentHashMap<>();
    private final ThreadLocal<Boolean> currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = ThreadLocal.withInitial(() -> false);
    private final Set<OperationWithResultWithIdWrapper<RegattaLogSensorDataTrackerTrackedRegattaListener, ?>> operationsSentToMasterForReplication = new HashSet<>();
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
    public Iterable<OperationExecutionListener<RegattaLogSensorDataTrackerTrackedRegattaListener>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public void addOperationExecutionListener(
            OperationExecutionListener<RegattaLogSensorDataTrackerTrackedRegattaListener> listener) {
        this.operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(
            OperationExecutionListener<RegattaLogSensorDataTrackerTrackedRegattaListener> listener) {
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
            OperationWithResultWithIdWrapper<RegattaLogSensorDataTrackerTrackedRegattaListener, ?> operationWithResultWithIdWrapper) {
        this.operationsSentToMasterForReplication.add(operationWithResultWithIdWrapper);
    }

    @Override
    public boolean hasSentOperationToMaster(
            OperationWithResult<RegattaLogSensorDataTrackerTrackedRegattaListener, ?> operation) {
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
