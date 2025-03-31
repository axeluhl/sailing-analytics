package com.sap.sailing.domain.racelogsensortracking;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sse.common.Timed;

public interface SensorFixMapperFactory {
    <FixT extends Timed, TrackT extends DynamicTrack<FixT>> SensorFixMapper<FixT, TrackT, Competitor> 
            createCompetitorMapper(Class<? extends RegattaLogDeviceMappingEvent<?>> eventType);
}
