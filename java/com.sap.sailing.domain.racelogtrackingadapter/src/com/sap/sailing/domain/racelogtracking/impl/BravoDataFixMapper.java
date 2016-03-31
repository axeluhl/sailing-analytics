package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicTrackImpl;

public class BravoDataFixMapper implements SensorFixMapper<DoubleVectorFix, DynamicTrack<DoubleVectorFix>, Competitor> {
    
    private static final String TRACK_NAME = "BravoFixTrack";

    @Override
    public DynamicTrack<DoubleVectorFix> getTrack(DynamicTrackedRace race, Competitor key) {
        String lockName = TRACK_NAME + " for " + key;
        return race.getOrCreateSensorTrack(key, TRACK_NAME, () -> new DynamicTrackImpl<DoubleVectorFix>(lockName));
    }

    @Override
    public void addFix(DynamicTrack<DoubleVectorFix> track, DoubleVectorFix fix) {
        track.add(fix);
    }

}
