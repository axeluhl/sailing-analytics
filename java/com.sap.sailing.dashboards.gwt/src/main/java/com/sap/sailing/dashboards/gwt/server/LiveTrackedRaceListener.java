package com.sap.sailing.dashboards.gwt.server;

import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Informs the listener about changes in a certain environment, where a {@link TrackedRace} with
 * {@link TrackedRace#getStatus()} live is continuously required.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public interface LiveTrackedRaceListener {

    public void liveTrackedRaceDidChange(TrackedRace trackedRace);
}
