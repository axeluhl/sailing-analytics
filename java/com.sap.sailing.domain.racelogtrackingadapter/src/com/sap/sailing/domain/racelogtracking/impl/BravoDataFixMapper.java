package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.BravoFixTrackImpl;

public class BravoDataFixMapper implements SensorFixMapper<DoubleVectorFix, DynamicSensorFixTrack<Competitor, BravoFix>, Competitor> {

    @Override
    public DynamicSensorFixTrack<Competitor, BravoFix> getTrack(DynamicTrackedRace race, Competitor key) {
        return race.getOrCreateSensorTrack(key, BravoFixTrack.TRACK_NAME, 
                () -> new BravoFixTrackImpl<Competitor>(key, BravoFixTrack.TRACK_NAME));
    }

    @Override
    public void addFix(DynamicSensorFixTrack<Competitor, BravoFix> track, DoubleVectorFix fix) {
        track.add(new BravoFixImpl(fix));
    }
    
    @Override
    public boolean isResponsibleFor(Class<?> eventType) {
        return RegattaLogDeviceCompetitorBravoMappingEventImpl.class.isAssignableFrom(eventType);
    }
}
