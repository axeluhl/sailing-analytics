package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordBuoyGPSFix extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -2149936580623244814L;
    private final Buoy buoy;
    private final GPSFix fix;
    
    public RecordBuoyGPSFix(RegattaAndRaceIdentifier raceIdentifier, Buoy buoy, GPSFix fix) {
        super(raceIdentifier);
        this.buoy = buoy;
        this.fix = fix;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        trackedRace.recordFix(buoy, fix);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
