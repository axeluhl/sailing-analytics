package com.sap.sailing.racecommittee.app.utils;

import android.os.Handler;
import android.os.Looper;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Reports a tick every second as a singleton.
 */
public enum TickSingleton {
    INSTANCE;

    private final List<TickListener> listeners = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            TimePoint now = MillisecondsTimePoint.now();
            final long delay = now.asMillis() / 1000 * 1000 + 1000 - now.asMillis();
            handler.postDelayed(this, delay);
            //Notify all listeners
            for (TickListener listener : listeners) {
                listener.notifyTick(now);
            }
        }
    };

    /**
     * If the {@link TickListener} has been registered, a new tick will be invoked immediately
     * to ensure that {@link TickListener#notifyTick(TimePoint)} is called as soon as possible.
     *
     * @param toRegister The listener to register
     */
    public void registerListener(TickListener toRegister) {
        if (listeners.add(toRegister)) {
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }
    }

    public void unregisterListener(TickListener toUnregister) {
        listeners.remove(toUnregister);
        if (listeners.isEmpty()) {
            handler.removeCallbacks(runnable);
        }
    }
}
