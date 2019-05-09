package com.sap.sailing.server.replication.test;

import com.sap.sailing.server.gateway.jaxrs.spi.MasterDataResource;
import com.sap.sse.security.SecurityService;

public class DummyMasterDataResource extends MasterDataResource {
    @Override
    protected SecurityService getSecurityService() {
        return super.getSecurityService();
    }
}
