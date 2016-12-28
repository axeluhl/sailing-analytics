package com.sap.sailing.mongodb.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.PlaceHolderDeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.RaceLogConnectivityParams;
import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingAdapterFactoryImpl;
import com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParametersHandler;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.impl.RaceTrackingConnectivityParametersImpl;
import com.sap.sailing.domain.tractracadapter.impl.TracTracConnectivityParamsHandler;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
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
    private RacingEventService racingEventService;

    public MockConnectivityParamsServiceFinderFactory() {
        serviceFinders.put(RaceTrackingConnectivityParametersHandler.class, new TypeBasedServiceFinder<RaceTrackingConnectivityParametersHandler>() {
            @Override
            public RaceTrackingConnectivityParametersHandler findService(String type)
                    throws NoCorrespondingServiceRegisteredException {
                final MongoObjectFactory mongoObjectFactory = racingEventService.getMongoObjectFactory();
                final DomainObjectFactory domainObjectFactory = racingEventService.getDomainObjectFactory();
                switch (type) {
                case RaceTrackingConnectivityParametersImpl.TYPE:
                    return new TracTracConnectivityParamsHandler(
                            MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                            MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory),
                            TracTracAdapterFactory.INSTANCE.getOrCreateTracTracAdapter(domainObjectFactory.getBaseDomainFactory()).getTracTracDomainFactory());
                case SwissTimingTrackingConnectivityParameters.TYPE:
                    return new com.sap.sailing.domain.swisstimingadapter.impl.SwissTimingConnectivityParamsHandler(
                            MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory, domainObjectFactory),
                            MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(mongoObjectFactory, domainObjectFactory),
                            new SwissTimingAdapterFactoryImpl().getOrCreateSwissTimingAdapter(domainObjectFactory.getBaseDomainFactory()).getSwissTimingDomainFactory());
                case RaceLogConnectivityParams.TYPE:
                    return new com.sap.sailing.domain.racelogtracking.impl.RaceLogConnectivityParamsHandler(racingEventService);
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
    
    /**
     * Must be called right after the constructor invocation when this object has been used to initialize a
     * {@link RacingEventServiceImpl} object.
     */
    public void setRacingEventService(RacingEventService racingEventService) {
        this.racingEventService = racingEventService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <ServiceT> TypeBasedServiceFinder<ServiceT> createServiceFinder(Class<ServiceT> clazz) {
        return (TypeBasedServiceFinder<ServiceT>) serviceFinders.get(clazz);
    }

}
