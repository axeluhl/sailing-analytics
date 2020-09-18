package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.util.ThreadPoolUtil;

public class MetadataUpdater {
    private static final Logger logger = Logger.getLogger(MetadataUpdater.class.getName());
    private final ScheduledExecutorService executor;
    private final DeviceIdentifier forDevice;
    private final MetadataCollection metadataCollection;
    private Future<?> runningUpdate;
    private MetadataUpdate<?> nextUpdate;

    MetadataUpdater(ScheduledExecutorService executor, MetadataCollection metadataCollection, DeviceIdentifier forDevice) {
        super();
        this.executor = executor;
        this.metadataCollection = metadataCollection;
        this.forDevice = forDevice;
    }
    
    /**
     * Uses the {@link ThreadPoolUtil#getDefaultBackgroundTaskThreadPoolExecutor() default background thread pool
     * executor} for updating the metadata.
     * 
     * @param forDevice
     *            used to validate the constraint that all updates delivered to this updater must be for that device
     */
    MetadataUpdater(MetadataCollection metadataCollection, DeviceIdentifier forDevice) {
        this(ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor(), metadataCollection, forDevice);
    }
    
    synchronized <FixT extends Timed> void enqueueMetadataUpdate(DeviceIdentifier device, final Object dbDeviceId,
            final int nrOfTotalFixes, TimeRange fixesTimeRange, FixT latestFix) throws TransformationException {
        final MetadataUpdate<FixT> update = new MetadataUpdate<>(device, dbDeviceId, nrOfTotalFixes, fixesTimeRange, latestFix);
        if (runningUpdate == null) {
            setNextUpdate(update);
            scheduleUpdate();
        } else {
            if (getNextUpdate() == null) {
                setNextUpdate(update);
            } else {
                final MetadataUpdate<FixT> theNextUpdate = getNextUpdate();
                setNextUpdate(theNextUpdate.merge(update));
            }
        }
    }
    
    /**
     * Schedules a task that under this object's monitor ({@code synchronized}) obtains the {@link #getNextUpdate() next update}
     * and if one is found, {@link MetadataCollection#update(MetadataUpdate) updates} the MongoDB metadata collection accordingly.
     * Before terminating, this object's monitor is acquired again, and if then another {@link #getNextUpdate() next update} is
     * found, it is applied again; otherwise, the task ends.
     */
    private synchronized <FixT extends Timed> void scheduleUpdate() {
        assert runningUpdate == null;
        runningUpdate = executor.submit((Callable<Void>) ()->{
            MetadataUpdate<FixT> theNextUpdate;
            logger.fine(()->"Starting metadata updater task for device "+forDevice);
            do {
                synchronized (MetadataUpdater.this) {
                    theNextUpdate = getNextUpdate();
                    if (theNextUpdate == null) {
                        runningUpdate = null;
                    } else {
                        setNextUpdate(null);
                    }
                }
                if (theNextUpdate != null) {
                    metadataCollection.update(theNextUpdate);
                }
            } while (theNextUpdate != null);
            logger.fine(()->"Terminating metadata updater task for device "+forDevice);
            return null;
        });
    }

    private <FixT extends Timed> void setNextUpdate(MetadataUpdate<FixT> update) {
        assert update == null || update.getDevice().equals(forDevice);
        nextUpdate = update;
    }

    @SuppressWarnings("unchecked")
    private synchronized <FixT extends Timed> MetadataUpdate<FixT> getNextUpdate() {
        return (MetadataUpdate<FixT>) nextUpdate;
    }

    void waitForPendingUpdates() {
        final Future<?> theRunningUpdate;
        synchronized (this) {
            theRunningUpdate = runningUpdate;
        }
        if (theRunningUpdate != null) {
            try {
                theRunningUpdate.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.INFO, "Exception waiting for pending metadata updates", e);
            }
        }
    }
}
