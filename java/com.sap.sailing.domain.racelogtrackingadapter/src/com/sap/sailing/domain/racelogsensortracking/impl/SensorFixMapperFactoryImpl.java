package com.sap.sailing.domain.racelogsensortracking.impl;

import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.tracking.SensorFixMapper;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sse.common.Timed;

public class SensorFixMapperFactoryImpl implements SensorFixMapperFactory {
    
    private final ServiceTracker<SensorFixMapper<?, ?, ?>, SensorFixMapper<?, ?, ?>> tracker;

    public SensorFixMapperFactoryImpl(ServiceTracker<SensorFixMapper<?, ?, ?>, SensorFixMapper<?, ?, ?>> tracker) {
        this.tracker = tracker;
    }
    
    @Override
    public <FixT extends Timed, TrackT extends Track<?>> SensorFixMapper<FixT, TrackT, Competitor>
            createCompetitorMapper(RegattaLogDeviceCompetitorMappingEvent event) {
        return createCompetitorMapper(event.getClass());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <FixT extends Timed, TrackT extends Track<?>> SensorFixMapper<FixT, TrackT, Competitor>
            createCompetitorMapper(Class<?> eventType) {
        for (ServiceReference<SensorFixMapper<?, ?, ?>> serviceReference : tracker.getServiceReferences()) {
            SensorFixMapper<?, ?, ?> service = tracker.getService(serviceReference);
            if (service != null && service.isResponsibleFor(eventType)) {
                return (SensorFixMapper<FixT, TrackT, Competitor>) service;
            }
        }
        throw new RuntimeException("No SensorFixMapper found for event type: " + eventType);
    }

}
