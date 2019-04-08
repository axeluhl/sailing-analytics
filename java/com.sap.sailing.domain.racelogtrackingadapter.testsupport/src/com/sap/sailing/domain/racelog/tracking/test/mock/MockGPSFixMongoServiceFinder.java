package com.sap.sailing.domain.racelog.tracking.test.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixMovingImpl;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.DoubleVectorFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMongoHandlerImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.GPSFixMovingMongoHandlerImpl;

public class MockGPSFixMongoServiceFinder extends AbstractTypeBasedServiceFinder<FixMongoHandler<?>> {
    private final MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(null);
    private final DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(null, null);
    private final FixMongoHandler<?> movingHandler = new GPSFixMovingMongoHandlerImpl(mof, dof);
    private final FixMongoHandler<?> handler = new GPSFixMongoHandlerImpl(mof, dof);
    private final FixMongoHandler<?> doubleVectorFixHandler = new DoubleVectorFixMongoHandlerImpl(mof, dof);

    @Override
    public FixMongoHandler<?> findService(String fixType) {
        if (fixType.equals(GPSFixMovingImpl.class.getName()) ||
                fixType.equals(VeryCompactGPSFixMovingImpl.class.getName())) return movingHandler;
        if (fixType.equals(GPSFixImpl.class.getName()) ||
                fixType.equals(VeryCompactGPSFixImpl.class.getName()))  return handler;
        if (fixType.equals(DoubleVectorFixImpl.class.getName()))  return doubleVectorFixHandler;
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
