package com.sap.sailing.nmeaconnector.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.nmeaconnector.NMEAWindReceiver;

public class NMEAWindReceiverImpl implements NMEAWindReceiver {
    private final ConcurrentHashMap<WindListener, WindListener> listeners;
    
    public NMEAWindReceiverImpl() {
        super();
        this.listeners = new ConcurrentHashMap<>();
    }

    @Override
    public void addWindListener(WindListener listener) {
        listeners.put(listener, listener);
    }

    @Override
    public void removeWindListener(WindListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Wind wind) {
        for (WindListener listener : listeners.values()) {
            listener.windDataReceived(wind);
        }
    }
}
