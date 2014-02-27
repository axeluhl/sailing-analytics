package com.sap.sailing.domain.common.racelog.tracking;


public class SingleTypeBasedServiceFinderImpl<ServiceType> implements
TypeBasedServiceFinder<ServiceType> {
    private final ServiceType service;
    private final String type;

    public SingleTypeBasedServiceFinderImpl(ServiceType service, String type) {
        this.service = service;
        this.type = type;
    }

    @Override
    public ServiceType findService(String type)
            throws NoCorrespondingServiceRegisteredException {
        if (type.equals(this.type)) return service;
        throw new NoCorrespondingServiceRegisteredException("Only one service registered", type, service.getClass());
    }

}
