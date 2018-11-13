package com.sap.sailing.server.test;

import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.server.testsupport.RacingEventServiceImplMock;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.security.SecurityService;

public class DummyRacingEventServiceImplMock extends RacingEventServiceImplMock {
    @Override
    public SecurityService getSecurityService() {
        return super.getSecurityService();
    }

    public DummyRacingEventServiceImplMock() {
        super();
    }

    public DummyRacingEventServiceImplMock(DataImportProgressImpl dataImportProgressImpl,
            TypeBasedServiceFinderFactory serviceFinderFactory) {
        super(dataImportProgressImpl, serviceFinderFactory);
    }

    public DummyRacingEventServiceImplMock(DataImportProgressImpl dataImportProgressImpl) {
        super(dataImportProgressImpl);
    }
}
