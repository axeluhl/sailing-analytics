package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.interfaces.RacingEventService;

public class RecordMarkGPSFixForExistingTrack extends RecordMarkGPSFix {
    private static final Logger logger = Logger.getLogger(RecordMarkGPSFixForExistingTrack.class.getName());
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
	    if (mark != null) {
	        trackedRace.recordFix(mark, getFix(), /* onlyWhenInTrackingTimeInterval */ false); // record the fix in any case
	    } else {
	        logger.warning("Received GPS fix for mark with ID "+markId+" which was not found in tracked race "+trackedRace.getRace().getName()+
	                " in regatta "+trackedRace.getTrackedRegatta().getRegatta().getName()+
	                ". Maybe this event was processed after the mark had been removed.");
	    }
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
