package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.SensorFixTrackImpl;

public class BravoDataFixMapper implements SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<BravoFix>, Competitor> {
    
    private static final String TRACK_NAME = "BravoFixTrack";

    @Override
    public DynamicSensorFixTrack<BravoFix> getTrack(DynamicTrackedRace race, Competitor key) {
        String lockName = TRACK_NAME + " for " + key;
        return race.getOrCreateSensorTrack(key, TRACK_NAME, () -> new SensorFixTrackImpl<BravoFix>(
                BravoSensorDataMetadata.INSTANCE.getColumns(), lockName));
    }

    @Override
    public void addFix(DynamicSensorFixTrack<BravoFix> track, DoubleVectorFix fix) {
        track.add(new BravoFixImpl(fix));
    }

}
