package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordMarkGPSFix extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -2149936580623244814L;
    private final Mark mark;
    private final GPSFix fix;
    
    public RecordMarkGPSFix(RegattaAndRaceIdentifier raceIdentifier, Mark mark, GPSFix fix) {
        super(raceIdentifier);
        this.mark = mark;
        this.fix = fix;
    }

    /**
     * Operations of this type can be run in parallel to other operations; subsequent operations do not have to wait
     * for this operation's completion.
     */
    @Override
    public boolean requiresSynchronousExecution() {
        return false;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) toState.getTrackedRace(getRaceIdentifier());
        trackedRace.recordFix(mark, fix);
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
