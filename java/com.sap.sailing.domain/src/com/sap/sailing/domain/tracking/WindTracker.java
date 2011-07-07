package com.sap.sailing.domain.tracking;

/**
 * Receives wind information and forwards it to a {@link TrackedRace}'s {@link TrackedRace#getWindTrack(WindSource) wind
 * track}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface WindTracker {

    void stop();

}
