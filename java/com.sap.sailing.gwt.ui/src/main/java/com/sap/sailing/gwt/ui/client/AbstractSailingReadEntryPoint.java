package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.ServiceRoutingProvider;

public class AbstractSailingReadEntryPoint extends AbstractSailingEntryPoint<SailingServiceAsync> {
    
    @Override
    protected SailingServiceAsync getSailingService() {
        if (sailingService == null) {
            if (this instanceof ServiceRoutingProvider) {
                sailingService = SailingServiceHelper.createSailingServiceInstance((ServiceRoutingProvider)this);
            } else {
                sailingService = SailingServiceHelper.createSailingServiceInstance();
            }
        }
        return sailingService;
    }
}
