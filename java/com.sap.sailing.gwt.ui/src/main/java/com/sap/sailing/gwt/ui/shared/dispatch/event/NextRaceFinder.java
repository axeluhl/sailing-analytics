package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceTrackingState;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
public class NextRaceFinder implements RaceCallback {
    
    private final TimePoint now = MillisecondsTimePoint.now();
    private TimePoint nextStartTime;
    private RaceContext nextRace;

    @Override
    public void doForRace(RaceContext context) {
        TimePoint startTime = context.getStartTime();
        if(context.getRaceTrackingState() != RaceTrackingState.TRACKED_VALID_DATA || startTime == null || now.after(startTime)) {
            return;
        }
        if(nextRace == null || startTime.before(nextStartTime)) {
            nextRace = context;
            nextStartTime = startTime;
        }
    }

    public RaceContext getNextRace() {
        return nextRace;
    }
    
    public TimePoint getNextStartTime() {
        return nextStartTime;
    }
}
