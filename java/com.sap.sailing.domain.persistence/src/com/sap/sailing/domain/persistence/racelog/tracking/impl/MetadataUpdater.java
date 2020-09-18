package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * Manages the actual updates to the {@link MetadataCollection}, batching the update statements to the MongoDB by
 * combining update requests {@link #enqueueMetadataUpdate(DeviceIdentifier, Object, int, TimeRange, Timed) enqueued}
 * with this updater in case an update is currently running.
 * <p>
 * 
 * When holding this object's monitor (using {@code synchronized}), if {@link #runningUpdate} is not {@code null},
 * {@link #setNextUpdate(MetadataUpdate) setting an update request} will have this update request be picked up
 * by the running updating task. Also, under this object's monitor, if {@link #runningUpdate} is not {@code null}
 * and the update task has picked by an update for processing, {@link #getNextUpdate()} will return {@code null}
 * until a next update is {@link #setNextUpdate(MetadataUpdate) set}.<p>
 * 
 * The {@link #waitForPendingUpdates()} method can be used to wait until the pending update request(s) up to the
 * point when {@link #waitForPendingUpdates()} is called have been sent to MongoDB.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class MetadataUpdater {
    private static final Logger logger = Logger.getLogger(MetadataUpdater.class.getName());
    private final ScheduledExecutorService executor;
    private final DeviceIdentifier forDevice;
    private final MetadataCollection metadataCollection;
    private Future<?> runningUpdate;
    private MetadataUpdate<?> nextUpdate;
    
    /**
     * A counter for the updates processed. Can be used for synchronization purposes, e.g., by {@link #waitForPendingUpdates()}.
     */
    private long updatesProcessed;

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
        assert runningUpdate == null && nextUpdate != null;
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
                    updatesProcessed++;
                    MetadataUpdater.this.notifyAll();
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

    /**
     * An update may either currently be in progress, on its way to MongoDB, with no {@link #nextUpdate} in the queue, or
     * an update may have been queued in {@link #nextUpdate} which will be picked up by an already running update task
     * and will then be in progress.
     */
    void waitForPendingUpdates() {
        synchronized (this) {
            if (runningUpdate != null) {
                final long waitUntilTheseManyUpdatesProcessed = getNextUpdate() == null ? updatesProcessed+1 : updatesProcessed+2;
                while (updatesProcessed < waitUntilTheseManyUpdatesProcessed) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Interrupted while waiting for pending updates; continuing to wait...", e);
                    }
                }
            }
        }
    }
}
