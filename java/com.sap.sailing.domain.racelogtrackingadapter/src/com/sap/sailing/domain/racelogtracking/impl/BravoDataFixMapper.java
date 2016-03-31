package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;

public class BravoDataFixMapper implements SensorFixMapper<DoubleVectorFix, DynamicTrack<DoubleVectorFix>, Competitor> {

    @Override
    public DynamicTrack<DoubleVectorFix> getTrack(DynamicTrackedRace race, Competitor key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addFix(DynamicTrack<DoubleVectorFix> track, DoubleVectorFix fix) {
        // TODO Auto-generated method stub
    }

}
