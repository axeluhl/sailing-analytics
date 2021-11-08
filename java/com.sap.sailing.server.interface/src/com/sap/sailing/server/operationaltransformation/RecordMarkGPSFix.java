package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public abstract class RecordMarkGPSFix extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -2149936580623244814L;
    private final GPSFix fix;
    
    public RecordMarkGPSFix(RegattaAndRaceIdentifier raceIdentifier, GPSFix fix) {
        super(raceIdentifier);
        this.fix = fix;
    }

    protected GPSFix getFix() {
        return fix;
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
