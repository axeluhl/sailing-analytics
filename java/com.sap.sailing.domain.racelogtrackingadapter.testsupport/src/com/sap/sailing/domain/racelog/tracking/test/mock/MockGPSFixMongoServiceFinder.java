package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.GPSFixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMovingMongoHandlerImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sse.common.TypeBasedServiceFinder;

public class MockGPSFixMongoServiceFinder implements TypeBasedServiceFinder<GPSFixMongoHandler> {
    private final MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(null);
    private final DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(null, null);

    @Override
    public GPSFixMongoHandler findService(String fixType) {
        if (fixType.equals(GPSFixMovingImpl.class.getName())) return new GPSFixMovingMongoHandlerImpl(mof, dof);
        if (fixType.equals(GPSFixImpl.class.getName())) return new GPSFixMongoHandlerImpl(mof, dof);
        return null;
    }

    @Override
    public void setFallbackService(GPSFixMongoHandler fallback) {
    }
}
