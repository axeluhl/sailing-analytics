package com.sap.sse.replication.impl;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicablesProvider;

public abstract class AbstractReplicablesProvider implements ReplicablesProvider {
    private final Set<ReplicableLifeCycleListener> listeners;
    
    public AbstractReplicablesProvider() {
        this.listeners = new HashSet<>();
    }
    
    @Override
    public void addReplicableLifeCycleListener(ReplicableLifeCycleListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeReplicableLifeCycleListener(ReplicableLifeCycleListener listener) {
        listeners.remove(listener);
    }
    
    protected void notifyReplicableLifeCycleListenersAboutReplicableAdded(Replicable<?, ?> replicable) {
        for (ReplicableLifeCycleListener listener : listeners) {
            listener.replicableAdded(replicable);
        }
    }

    protected void notifyReplicableLifeCycleListenersAboutReplicableRemoved(String replicableIdAsString) {
        for (ReplicableLifeCycleListener listener : listeners) {
            listener.replicableRemoved(replicableIdAsString);
        }
    }

    /**
     * This default implementation loops over the result of {@link #getReplicables()} and returns the first object whose
     * {@link Replicable#getId() ID} equals the value of the parameter <code>replicableIdAsString</code>.
     * 
     * @throws NoSuchElementException if no replicable by that name is found
     */
    @Override
    public Replicable<?, ?> getReplicable(String replicableIdAsString, boolean wait) {
        return StreamSupport.stream(getReplicables().spliterator(), /* parallel */false)
                .filter(r -> r.getId().toString().equals(replicableIdAsString)).findAny().orElse(null);
    }
}
