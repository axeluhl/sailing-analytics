package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tracking.TrackListener;
import com.sap.sse.common.Timed;

public class TrackListenerCollection<ItemType, FixType extends Timed, ListenerType extends TrackListener> implements Serializable {
    private static final long serialVersionUID = -7324146114278369375L;
    private Set<ListenerType> listeners;
    
    public TrackListenerCollection() {
        listeners = new HashSet<>();
    }
    
    @SuppressWarnings("unchecked") // need typed generic cast
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        listeners = (Set<ListenerType>) ois.readObject();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        final Set<ListenerType> listenersToSerialize;
        synchronized (listeners) {
            listenersToSerialize = new HashSet<>();
            for (ListenerType listener : listeners) {
                if (!listener.isTransient()) {
                    listenersToSerialize.add(listener);
                }
            }
        }
        oos.writeObject(listenersToSerialize);
    }

    public void addListener(ListenerType listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(ListenerType listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * To iterate over the resulting listener list, synchronize on the iterable returned. Only this will avoid
     * {@link ConcurrentModificationException}s because listeners may be added on the fly, and this object will
     * synchronize on the listeners collection before adding on.
     */
    public Iterable<ListenerType> getListeners() {
        synchronized (listeners) {
            return new HashSet<ListenerType>(listeners);
        }
    }
}
