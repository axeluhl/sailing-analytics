package com.sap.sailing.mongodb.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.PlaceHolderDeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.impl.ConnectivityParamsHandler;
import com.sap.sailing.domain.tractracadapter.impl.RaceTrackingConnectivityParametersImpl;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.mongodb.MongoDBService;

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
                    final MongoObjectFactory mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(MockConnectivityParamsServiceFinderFactory.this);
                    final DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDomainObjectFactory(MongoDBService.INSTANCE, DomainFactory.INSTANCE, MockConnectivityParamsServiceFinderFactory.this);
                    return new ConnectivityParamsHandler(
                            MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                            MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory),
                            TracTracAdapterFactory.INSTANCE.getOrCreateTracTracAdapter(domainObjectFactory.getBaseDomainFactory()).getTracTracDomainFactory());
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
