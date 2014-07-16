package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RecordMarkGPSFix extends AbstractRaceOperation<Void> {
    private static final long serialVersionUID = -2149936580623244814L;
    private final Serializable markID;
    private final GPSFix fix;
    
    public RecordMarkGPSFix(RegattaAndRaceIdentifier raceIdentifier, Mark mark, GPSFix fix) {
        super(raceIdentifier);
        this.markID = mark.getId();
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
        Mark mark = getMarkById(trackedRace);
        trackedRace.recordFix(mark, fix);
        return null;
    }

    private Mark getMarkById(DynamicTrackedRace trackedRace) {
        Mark result = null;
        for (Mark mark : trackedRace.getMarks()) {
            if (mark.getId().equals(markID)) {
                result = mark;
                break;
            }
        }
        return result;
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
