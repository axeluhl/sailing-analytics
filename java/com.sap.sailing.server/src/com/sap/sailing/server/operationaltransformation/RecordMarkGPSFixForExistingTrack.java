package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;

public class RecordMarkGPSFixForExistingTrack extends RecordMarkGPSFix {
    private static final long serialVersionUID = -3953907832324217209L;
    private final Serializable markId;

    public RecordMarkGPSFixForExistingTrack(RegattaAndRaceIdentifier raceIdentifier, Mark mark, GPSFix fix) {
        super(raceIdentifier, fix);
        this.markId = mark.getId();
    }
    
    private Mark getMarkById(DynamicTrackedRace trackedRace) {
        Mark result = null;
        for (Mark mark : trackedRace.getMarks()) {
            if (mark.getId().equals(markId)) {
                result = mark;
                break;
            }
        }
        return result;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = getTrackedRace(toState);
	if (trackedRace != null) {
	    Mark mark = getMarkById(trackedRace);
	    trackedRace.recordFix(mark, getFix(), /* onlyWhenInTrackingTimeInterval */ false); // record the fix in any case
	}
        return null;
    }

    /**
     * Operations of this type can be run in parallel to other operations; subsequent operations do not have to wait
     * for this operation's completion.
     */
    @Override
    public boolean requiresSynchronousExecution() {
        return false;
    }

}
