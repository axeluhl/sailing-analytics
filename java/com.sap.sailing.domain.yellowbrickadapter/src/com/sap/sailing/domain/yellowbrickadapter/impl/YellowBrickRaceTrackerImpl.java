package com.sap.sailing.domain.yellowbrickadapter.impl;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.WindStore;

public class YellowBrickRaceTrackerImpl extends AbstractRaceTrackerImpl {
    public YellowBrickRaceTrackerImpl(RaceTrackingConnectivityParameters connectivityParams) {
        super(connectivityParams);
    }

    @Override
    public Regatta getRegatta() {
        // TODO Implement YellowBrickRaceTrackerImpl.getRegatta(...)
        return null;
    }

    @Override
    public RaceDefinition getRace() {
        // TODO Implement YellowBrickRaceTrackerImpl.getRace(...)
        return null;
    }

    @Override
    public RaceHandle getRaceHandle() {
        // TODO Implement YellowBrickRaceTrackerImpl.getRaceHandle(...)
        return null;
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        // TODO Implement YellowBrickRaceTrackerImpl.getTrackedRegatta(...)
        return null;
    }

    @Override
    public WindStore getWindStore() {
        // TODO Implement YellowBrickRaceTrackerImpl.getWindStore(...)
        return null;
    }

    @Override
    public Object getID() {
        // TODO Implement YellowBrickRaceTrackerImpl.getID(...)
        return null;
    }

}
