package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.WindSource;

/**
 * Receives wind information and forwards it to a {@link TrackedRace}'s {@link TrackedRace#getOrCreateWindTrack(WindSource, long) wind
 * track}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface WindTracker {

    void stop();

}
