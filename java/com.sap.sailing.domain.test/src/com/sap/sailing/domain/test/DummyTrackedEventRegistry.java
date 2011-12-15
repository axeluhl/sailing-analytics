package com.sap.sailing.domain.test;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.TrackedEventRegistry;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedEventImpl;

public class DummyTrackedEventRegistry implements TrackedEventRegistry {
    private final Map<Event, DynamicTrackedEvent> eventTrackingCache;

    public DummyTrackedEventRegistry() {
        super();
        this.eventTrackingCache = new HashMap<Event, DynamicTrackedEvent>();
    }

    @Override
    public DynamicTrackedEvent getOrCreateTrackedEvent(Event event) {
        synchronized (eventTrackingCache) {
            DynamicTrackedEvent result = eventTrackingCache.get(event);
            if (result == null) {
                result = new DynamicTrackedEventImpl(event);
                eventTrackingCache.put(event, result);
            }
            return result;
        }
    }
    
    @Override
    public DynamicTrackedEvent getTrackedEvent(com.sap.sailing.domain.base.Event event) {
        return eventTrackingCache.get(event);
    }

    @Override
    public void removeTrackedEvent(Event event) {
        eventTrackingCache.remove(event);
    }
}
