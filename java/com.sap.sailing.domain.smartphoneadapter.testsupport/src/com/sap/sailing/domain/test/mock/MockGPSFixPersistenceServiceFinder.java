package com.sap.sailing.domain.test.mock;

import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.persistence.devices.GPSFixPersistenceHandler;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.GPSFixMovingPersistenceHandlerImpl;
import com.sap.sailing.domain.persistence.impl.GPSFixPersistenceHandlerImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class MockGPSFixPersistenceServiceFinder implements TypeBasedServiceFinder<GPSFixPersistenceHandler> {
	private final MongoObjectFactoryImpl mof = new MongoObjectFactoryImpl(null);
	private final DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(null, null);
    
    @Override
    public GPSFixPersistenceHandler findService(String fixType) {
        if (fixType.equals(GPSFixMovingImpl.class.getName())) return new GPSFixMovingPersistenceHandlerImpl(mof, dof);
        if (fixType.equals(GPSFixImpl.class.getName())) return new GPSFixPersistenceHandlerImpl(mof, dof);
        return null;
    }
}
