package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tracking.TrackListener;

public class TrackListeners<ListenerT extends TrackListener<?>> implements Serializable {
    private static final long serialVersionUID = -7117842092078781722L;
    private Set<ListenerT> listeners;
    
    public TrackListeners() {
        listeners = new HashSet<ListenerT>();
    }
    
    @SuppressWarnings("unchecked") // need typed generic cast
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        listeners = (Set<ListenerT>) ois.readObject();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        final Set<ListenerT> listenersToSerialize;
        synchronized (listeners) {
            listenersToSerialize = new HashSet<ListenerT>();
            for (ListenerT listener : listeners) {
                if (!listener.isTransient()) {
                    listenersToSerialize.add(listener);
                }
            }
        }
        oos.writeObject(listenersToSerialize);
    }

    public void addListener(ListenerT listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(ListenerT listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * To iterate over the resulting listener list, synchronize on the iterable returned. Only this will avoid
     * {@link ConcurrentModificationException}s because listeners may be added on the fly, and this object will
     * synchronize on the listeners collection before adding on.
     */
    public Iterable<ListenerT> getListeners() {
        synchronized (listeners) {
            return new HashSet<ListenerT>(listeners);
        }
    }
}