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
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMovingMongoHandlerImpl;
import com.sap.sse.common.TypeBasedServiceFinder;

public class MockGPSFixMongoServiceFinder implements TypeBasedServiceFinder<FixMongoHandler<?>> {
    private final MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(null);
    private final DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(null, null);
    private final FixMongoHandler<?> movingHandler = new GPSFixMovingMongoHandlerImpl(mof, dof);
    private final FixMongoHandler<?> handler = new GPSFixMongoHandlerImpl(mof, dof);

    @Override
    public FixMongoHandler<?> findService(String fixType) {
        if (fixType.equals(GPSFixMovingImpl.class.getName()) ||
                fixType.equals(CompactGPSFixMovingImpl.class.getName())) return movingHandler;
        if (fixType.equals(GPSFixImpl.class.getName()) ||
                fixType.equals(CompactGPSFixImpl.class.getName()))  return handler;
        return null;
    }

    @Override
    public void setFallbackService(FixMongoHandler<?> fallback) {
    }
    
    @Override
    public Set<FixMongoHandler<?>> findAllServices() {
        return new HashSet<FixMongoHandler<?>>(Arrays.asList(movingHandler, handler));
    }
}
