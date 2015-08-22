package com.sap.sailing.dashboards.gwt.server;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Is continuously looking for {@link TrackedRace} with {@link TrackedRace#getStatus()} live and
 * notifies {@link LiveTrackedRaceListener} if the current live {@link TrackedRace} has changed.
 * 
 * 
 * @author Alexander Ries (D062114)
 *
 */
public interface LiveTrackedRaceProvider {
    
    List<LiveTrackedRaceListener> listener = new ArrayList<LiveTrackedRaceListener>();
    
    public void notifyLiveTrackedRaceListenerAboutLiveTrackedRaceChange(TrackedRace trackedRace);
    
    default void addLiveTrackedRaceListener(LiveTrackedRaceListener liveTrackedRaceListener){
        listener.add(liveTrackedRaceListener);
    }
    
    default void removeLiveTrackedRaceListener(LiveTrackedRaceListener liveTrackedRaceListener){
        listener.remove(liveTrackedRaceListener);
    }
}
