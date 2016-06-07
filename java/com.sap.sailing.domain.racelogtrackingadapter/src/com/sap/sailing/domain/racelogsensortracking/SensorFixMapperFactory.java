package com.sap.sailing.domain.racelogsensortracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.Timed;

public interface SensorFixMapperFactory {
    
    <FixT extends Timed, TrackT extends Track<?>> SensorFixMapper<FixT, TrackT, Competitor> 
            createCompetitorMapper(Class<?> eventType);

//    <FixT extends Timed, TrackT extends Track<?>> SensorFixMapper<FixT, TrackT, Mark> 
//            createMarkMapper(RegattaLogDeviceMarkMappingEvent event);
}
