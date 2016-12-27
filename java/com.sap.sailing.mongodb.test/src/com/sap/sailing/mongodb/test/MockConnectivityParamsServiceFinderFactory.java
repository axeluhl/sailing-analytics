package com.sap.sailing.mongodb.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.PlaceHolderDeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.tractracadapter.impl.RaceTrackingConnectivityParametersImpl;
import com.sap.sailing.domain.tractracadapter.persistence.impl.ConnectivityParamsHandler;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockConnectivityParamsServiceFinderFactory implements TypeBasedServiceFinderFactory {
    Map<Class<?>, TypeBasedServiceFinder<?>> serviceFinders = new HashMap<Class<?>, TypeBasedServiceFinder<?>>();

    public MockConnectivityParamsServiceFinderFactory() {
        serviceFinders.put(RaceTrackingConnectivityParametersHandler.class, new TypeBasedServiceFinder<RaceTrackingConnectivityParametersHandler>() {
            @Override
            public RaceTrackingConnectivityParametersHandler findService(String type)
                    throws NoCorrespondingServiceRegisteredException {
                switch (type) {
                case RaceTrackingConnectivityParametersImpl.TYPE:
                    return new ConnectivityParamsHandler();
                default:
                    return null;
                }
            }

            @Override
            public Set<RaceTrackingConnectivityParametersHandler> findAllServices() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void setFallbackService(RaceTrackingConnectivityParametersHandler fallback) {
                // TODO Auto-generated method stub
                
            }
        });
        serviceFinders.put(DeviceIdentifierMongoHandler.class, new TypeBasedServiceFinder<DeviceIdentifierMongoHandler>() {
            @Override
            public DeviceIdentifierMongoHandler findService(String type)
                    throws NoCorrespondingServiceRegisteredException {
                switch (type) {
                case RaceTrackingConnectivityParametersImpl.TYPE:
                    return new PlaceHolderDeviceIdentifierMongoHandler();
                default:
                    return null;
                }
            }

            @Override
            public Set<DeviceIdentifierMongoHandler> findAllServices() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void setFallbackService(DeviceIdentifierMongoHandler fallback) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz) {
        return (TypeBasedServiceFinder<ServiceT>) serviceFinders.get(clazz);
    }
}
