package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.Timed;
import com.sap.sse.common.WithID;

public interface SensorFixMapper<FixT extends Timed, TrackT extends Track<?>, K extends WithID> {
    
    TrackT getTrack(DynamicTrackedRace race, K key);

    void addFix(TrackT track, FixT fix);
}
