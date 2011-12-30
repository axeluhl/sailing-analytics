package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;


public interface RaceChangeListener extends WindListener {
    void competitorPositionChanged(GPSFix fix, Competitor competitor);
    
    void buoyPositionChanged(GPSFix fix, Buoy buoy);
    
    /**
     * Invoked after the mark passings have been updated in the {@link TrackedRace}.
     * 
     * @param oldMarkPassing
     *            the mark passing replaced by <code>markPassing</code> or <code>null</code> if for the mark passing's
     *            waypoint no previous {@link MarkPassing} was recorded for the {@link MarkPassing#getCompetitor()
     *            competitor}.
     */
    void markPassingReceived(MarkPassing oldMarkPassing, MarkPassing markPassing);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

}
