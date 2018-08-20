package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;

/**
 * Constructs wind trackers that link some wind data receiving facility to a {@link TrackedRace}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface WindTrackerFactory {
    WindTracker createWindTracker(DynamicTrackedRegatta trackedRegatta, RaceDefinition race, boolean correctByDeclination)
            throws Exception;

    /**
     * Returns a {@link WindTracker} is one has been previously created for <code>race</code> and hasn't been
     * stopped yet, <code>null</code> otherwise.
     */
    WindTracker getExistingWindTracker(RaceDefinition race);
}
