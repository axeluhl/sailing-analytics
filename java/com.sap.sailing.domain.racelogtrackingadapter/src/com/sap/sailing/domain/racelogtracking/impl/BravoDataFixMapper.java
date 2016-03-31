package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.BravoFixTrackImpl;

public class BravoDataFixMapper implements SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<BravoFix>, Competitor> {

    @Override
    public DynamicSensorFixTrack<BravoFix> getTrack(DynamicTrackedRace race, Competitor key) {
        return race.getOrCreateSensorTrack(key, BravoFixTrack.TRACK_NAME, () -> new BravoFixTrackImpl(key));
    }

    @Override
    public void addFix(DynamicSensorFixTrack<BravoFix> track, DoubleVectorFix fix) {
        track.add(new BravoFixImpl(fix));
    }
}
