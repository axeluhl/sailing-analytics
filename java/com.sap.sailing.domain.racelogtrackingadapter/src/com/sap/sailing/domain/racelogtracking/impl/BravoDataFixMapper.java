package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceBoatBravoMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.CompetitorBravoFixTrackImpl;

/**
 * {@link SensorFixMapper} implementation to handle {@link BravoFix}es.
 */
public class BravoDataFixMapper implements SensorFixMapper<BravoFix, DynamicSensorFixTrack<Competitor, BravoFix>, Competitor> {

    @Override
    public DynamicSensorFixTrack<Competitor, BravoFix> getTrack(DynamicTrackedRace race, Competitor key) {
        return race.getOrCreateSensorTrack(key, BravoFixTrack.TRACK_NAME, 
                () -> new CompetitorBravoFixTrackImpl(key, BravoFixTrack.TRACK_NAME, /* hasExtendedFixes */ false,
                        race.getTrack(key)));
    }
    
    @Override
    public BravoFix map(DoubleVectorFix fix) {
        return new BravoFixImpl(fix);
    }
    
    @Override
    public boolean isResponsibleFor(Class<? extends RegattaLogDeviceMappingEvent<?>> eventType) {
        return RegattaLogDeviceCompetitorBravoMappingEventImpl.class.isAssignableFrom(eventType)
                || RegattaLogDeviceBoatBravoMappingEventImpl.class.isAssignableFrom(eventType);
    }
}