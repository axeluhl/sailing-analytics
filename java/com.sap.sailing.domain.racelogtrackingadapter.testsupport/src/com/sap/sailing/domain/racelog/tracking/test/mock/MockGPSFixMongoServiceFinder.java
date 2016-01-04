package com.sap.sailing.domain.racelog.tracking.test.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.tracking.impl.CompactGPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.CompactGPSFixMovingImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.GPSFixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMovingMongoHandlerImpl;
import com.sap.sse.common.TypeBasedServiceFinder;

public class MockGPSFixMongoServiceFinder implements TypeBasedServiceFinder<GPSFixMongoHandler> {
    private final MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(null);
    private final DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(null, null);
    private final GPSFixMongoHandler movingHandler = new GPSFixMovingMongoHandlerImpl(mof, dof);
    private final GPSFixMongoHandler handler = new GPSFixMongoHandlerImpl(mof, dof);

    @Override
    public GPSFixMongoHandler findService(String fixType) {
        if (fixType.equals(GPSFixMovingImpl.class.getName()) ||
                fixType.equals(CompactGPSFixMovingImpl.class.getName())) return movingHandler;
        if (fixType.equals(GPSFixImpl.class.getName()) ||
                fixType.equals(CompactGPSFixImpl.class.getName()))  return handler;
        return null;
    }

    @Override
    public void setFallbackService(GPSFixMongoHandler fallback) {
    }
    
    @Override
    public Set<GPSFixMongoHandler> findAllServices() {
        return new HashSet<GPSFixMongoHandler>(Arrays.asList(movingHandler, handler));
    }
}
