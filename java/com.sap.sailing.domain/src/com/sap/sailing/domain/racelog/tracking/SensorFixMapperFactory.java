package com.sap.sailing.domain.racelog.tracking;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.Timed;

public interface SensorFixMapperFactory {

    <FixT extends Timed, TrackT extends Track<FixT>> SensorFixMapper<FixT, TrackT, Competitor> 
            createCompetitorMapper(RegattaLogDeviceCompetitorMappingEvent event);
    
//    <FixT extends Timed, TrackT extends Track<FixT>> SensorFixMapper<FixT, TrackT, Mark> 
//            createMarkMapper(RegattaLogDeviceMarkMappingEvent event);
}
