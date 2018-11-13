package com.sap.sailing.server.test;

import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.security.SecurityService;

public class DummyRacingEventService extends RacingEventServiceImpl {
    @Override
    public SecurityService getSecurityService() {
        return super.getSecurityService();
    }

    public DummyRacingEventService(DomainObjectFactory domainObjectFactory, MongoObjectFactory mongoObjectFactory,
            MediaDB mediaDB, WindStore windStore, SensorFixStore sensorFixStore, boolean restoreTrackedRaces) {
        super(domainObjectFactory, mongoObjectFactory, mediaDB, windStore, sensorFixStore, restoreTrackedRaces);
    }

    public DummyRacingEventService(WindStore windStore, SensorFixStore sensorFixStore,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        super(windStore, sensorFixStore, serviceFinderFactory);
    }
}
