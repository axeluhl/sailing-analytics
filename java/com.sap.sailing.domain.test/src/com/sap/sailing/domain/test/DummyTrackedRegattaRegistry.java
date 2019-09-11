package com.sap.sailing.domain.test;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
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

    @Override
    public void removeRace(Regatta regatta, RaceDefinition race)
            throws MalformedURLException, IOException, InterruptedException {
    }

    @Override
    public boolean isRaceBeingTracked(Regatta regattaContext, RaceDefinition r) {
        return false;
    }

    @Override
    public void stopTracking(Regatta regatta, RaceDefinition race) throws MalformedURLException, IOException,
            InterruptedException {
    }

    @Override
    public void stopTracker(Regatta regatta, RaceTracker tracker)
            throws MalformedURLException, IOException, InterruptedException {
    }
}
