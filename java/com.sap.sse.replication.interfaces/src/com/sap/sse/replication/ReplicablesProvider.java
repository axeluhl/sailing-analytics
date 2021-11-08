package com.sap.sse.replication;


public interface ReplicablesProvider {
    /**
     * Listeners of this type can be registered and de-registered using
     * {@link ReplicablesProvider#addReplicableLifeCycleListener(ReplicableLifeCycleListener)} and
     * {@link ReplicablesProvider#removeReplicableLifeCycleListener(ReplicableLifeCycleListener)}. They will be notified
     * of {@link Replicable}s being added to or removed from this server instance. A replication service may use this to
     * start listening to operations executed by a new {@link Replicable} or stop observing one that is being removed.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    public static interface ReplicableLifeCycleListener {
        void replicableAdded(Replicable<?, ?> replicable);
        void replicableRemoved(String replicableIdAsString);
    }
    
    @FunctionalInterface
    public static interface AddOnlyReplicableLifeCycleListener extends ReplicableLifeCycleListener {
        @Override
        default void replicableRemoved(String replicableIdAsString) {}
    }
    
    Iterable<Replicable<?, ?>> getReplicables();

    /**
     * Tries to obtain a {@link Replicable> by its {@link Replicable#getId() ID}, converted to a string using
     * {@link Object#toString()} on the ID.
     * 
     * @param wait
     *            if <code>true</code> and the replicable with the ID specified is not currently available, wait for it
     *            to become available; otherwise, return <code>null</code> immediately in case a replicable with the ID
     *            specified is not currently available
     * 
     * @return <code>null</code> if no such replicable can be found; the replicable otherwise
     */
    Replicable<?, ?> getReplicable(String replicableIdAsString, boolean wait);
    
    void addReplicableLifeCycleListener(ReplicableLifeCycleListener listener);
    
    void removeReplicableLifeCycleListener(ReplicableLifeCycleListener listener);
}
