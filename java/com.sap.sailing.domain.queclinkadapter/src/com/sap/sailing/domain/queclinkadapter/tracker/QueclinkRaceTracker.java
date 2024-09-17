package com.sap.sailing.domain.queclinkadapter.tracker;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.AbstractRaceTrackerImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.WindStore;

/**
 * TODO: can we really have a "Queclink" tracker, or would this rather be at the level of their users, such as SailRacer?
 * Queclink would not have any information regarding what their trackers are used for. Either, this information is added
 * the "smartphone tracking" way through our own APIs and then Queclink messages are translated into fixes added to the
 * {@link SensorFixStore}, and with device mappings that use the Queclink trackers' IMEI for device identification.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class QueclinkRaceTracker extends AbstractRaceTrackerImpl<QueclinkConnectivityParameters> {

    public QueclinkRaceTracker(QueclinkConnectivityParameters connectivityParams) {
        super(connectivityParams);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Regatta getRegatta() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceDefinition getRace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceHandle getRaceHandle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DynamicTrackedRegatta getTrackedRegatta() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindStore getWindStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getID() {
        // TODO Auto-generated method stub
        return null;
    }
}
