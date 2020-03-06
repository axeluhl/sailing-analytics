package com.sap.sse.replication.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationService;

/**
 * While a regular OSGi {@link ServiceTracker} would {@link ServiceTracker#waitForService(long) wait} for the service's
 * appearance in the OSGi registry and then return it, this specialization is aware of the {@link ReplicationService}
 * and understands the replication life cycle which can be in {@link ReplicationService#isReplicationStarting()
 * starting} mode, and furthermore each replicable can be in the process of handling its initial load. This tracker has
 * a specialized {@link #waitForService(long)} implementation that waits for the {@link ReplicationService} to not be in
 * state {@link ReplicationService#isReplicationStarting()}, waits for the replicable requested using the regular
 * (super-class) {@link ServiceTracker#waitForService(long)} method and asserts the {@link Replicable} to return is
 * not in the state {@link Replicable#isCurrentlyFillingFromInitialLoad()}.<p>
 * 
 * By analogy, the {@link #getService()} and related methods return only services for which the above rules apply.
 * In particular, {@link #getService()} will return {@code null} if the {@link ReplicationService} is currently in mode
 * {@link ReplicationService#isReplicationStarting()} or the replicable found by the regular OSGi tracker is currently
 * {@link Replicable#isCurrentlyFillingFromInitialLoad() receiving its initial load}.<p>
 * 
 * Note that {@link #waitForService(long)} must not be used during the activation of a {@link Replicable}'s bundle before
 * that {@link Replicable} has been registered with the OSGi registry. Otherwise, a deadlock can result because the waiting
 * {@link Replicable} will hold up the completion of the replication start-up and hence wait forever.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <R>
 *            the type of {@link Replicable} to track
 */
public class FullyInitializedReplicableTracker<R extends Replicable<?, ?>> extends ServiceTracker<R, R> {
    public FullyInitializedReplicableTracker(BundleContext context, Class<R> clazz,
            ServiceTrackerCustomizer<R, R> customizer) {
        super(context, clazz, customizer);
    }

    public FullyInitializedReplicableTracker(BundleContext context, Filter filter,
            ServiceTrackerCustomizer<R, R> customizer) {
        super(context, filter, customizer);
    }

    public FullyInitializedReplicableTracker(BundleContext context, ServiceReference<R> reference,
            ServiceTrackerCustomizer<R, R> customizer) {
        super(context, reference, customizer);
    }

    public FullyInitializedReplicableTracker(BundleContext context, String clazz,
            ServiceTrackerCustomizer<R, R> customizer) {
        super(context, clazz, customizer);
    }

    @Override
    public R waitForService(long timeout) throws InterruptedException {
        final R replicable = super.waitForService(timeout);
        // TODO bug4006: continue here...
//        waitForReplicationServiceToBeReady();
//        waitForReplicableToBeFullyInitialized(replicable);
        return replicable;
    }

    @Override
    public R getService(ServiceReference<R> reference) {
        // TODO Implement FullyInitializedReplicableTracker.getService(...)
        return super.getService(reference);
    }
}
