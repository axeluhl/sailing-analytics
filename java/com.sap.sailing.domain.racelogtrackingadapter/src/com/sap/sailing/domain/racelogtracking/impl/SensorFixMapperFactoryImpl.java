package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapperFactory;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.Timed;

public class SensorFixMapperFactoryImpl implements SensorFixMapperFactory {

    @Override
    public <FixT extends Timed, TrackT extends Track<?>> SensorFixMapper<FixT, TrackT, Competitor>
            createCompetitorMapper(RegattaLogDeviceCompetitorMappingEvent event) {
        // TODO use interfaces to determine event type
        if (event instanceof RegattaLogDeviceCompetitorBravoMappingEventImpl) {
            return (SensorFixMapper<FixT, TrackT, Competitor>) new BravoDataFixMapper();
        }
        throw new RuntimeException("No SensorFixMapper found for event: " + event);
    }

}
