package com.sap.sailing.racecommittee.app.utils;

import java.util.HashSet;
import java.util.Set;

import android.os.Handler;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Reports a tick every second as a singleton
 * 
 * TODO: make it possible to report ticks in other times (e.g. 2s...)
 */
public enum TickSingleton implements Runnable {
    INSTANCE;

    private Set<TickListener> listeners = new HashSet<>();
    private Handler handler;

    private TickSingleton() {
        handler = new Handler();
        handler.post(this);
    }

    public void registerListener(TickListener toRegister) {
        listeners.add(toRegister);
    }

    public void unregisterListener(TickListener toUnregister) {
        listeners.remove(toUnregister);
    }

    public void run() {
        TimePoint now = MillisecondsTimePoint.now();
        for (TickListener item : listeners) {
            if (item != null) {
                item.notifyTick(now);
            }
        }
        handler.removeCallbacks(this);
        handler.postDelayed(this, 200);
    }
}
