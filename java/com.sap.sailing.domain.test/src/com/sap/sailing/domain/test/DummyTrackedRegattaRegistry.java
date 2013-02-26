package com.sap.sailing.domain.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;

public class DummyTrackedRegattaRegistry implements TrackedRegattaRegistry {
    private final Map<Regatta, DynamicTrackedRegatta> regattaTrackingCache;

    public DummyTrackedRegattaRegistry() {
        super();
        this.regattaTrackingCache = new HashMap<Regatta, DynamicTrackedRegatta>();
    }

    @Override
    public DynamicTrackedRegatta getOrCreateTrackedRegatta(Regatta regatta) {
        synchronized (regattaTrackingCache) {
            DynamicTrackedRegatta result = regattaTrackingCache.get(regatta);
            if (result == null) {
                result = new DynamicTrackedRegattaImpl(regatta);
                regattaTrackingCache.put(regatta, result);
            }
            return result;
        }
    }
    
    @Override
    public DynamicTrackedRegatta getTrackedRegatta(com.sap.sailing.domain.base.Regatta regatta) {
        return regattaTrackingCache.get(regatta);
    }

    @Override
    public void removeTrackedRegatta(Regatta regatta) {
        regattaTrackingCache.remove(regatta);
    }

    @Override
    public Regatta getRememberedRegattaForRace(Serializable race) {
        return null;
    }

}
