package com.sap.sailing.server.test;

import com.sap.sailing.server.gateway.jaxrs.spi.MasterDataResource;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.security.SecurityService;

/**
 * required for package visibility for getSecurityService
 */
public class DummyMasterDataRessource extends MasterDataResource {
    @Override
    protected SecurityService getSecurityService() {
        return super.getSecurityService();
    }

    @Override
    public ReplicationService getReplicationService() {
        return super.getReplicationService();
    }
}
