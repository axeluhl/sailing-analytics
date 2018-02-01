package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.server.RacingEventService;

/**
 * Records a mark fix for a mark whose track was just created. Such an operation carries the full {@link Mark} object
 * because the receiver needs it in order to construct the mark track.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class RecordMarkGPSFixForNewMarkTrack extends RecordMarkGPSFix {
    private static final long serialVersionUID = 6462303849651033783L;
    private final Mark mark;

    public RecordMarkGPSFixForNewMarkTrack(RegattaAndRaceIdentifier raceIdentifier, Mark mark, GPSFix fix) {
        super(raceIdentifier, fix);
        this.mark = mark;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        DynamicTrackedRace trackedRace = getTrackedRace(toState);
	if (trackedRace != null) {
	    trackedRace.recordFix(mark, getFix(), /* onlyWhenInTrackingTimeInterval */ false); // record the fix in any case
	}
        return null;
    }
}
