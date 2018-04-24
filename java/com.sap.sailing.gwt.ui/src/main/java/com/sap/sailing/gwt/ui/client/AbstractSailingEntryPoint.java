package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.ServiceRoutingProvider;
import com.sap.sse.security.ui.client.AbstractSecureEntryPoint;

public abstract class AbstractSailingEntryPoint extends AbstractSecureEntryPoint<StringMessages> {
    private SailingServiceAsync sailingService;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
    }
    
    /**
     * Lazily initialize sailing service. Concrete entry point subclasses may influence
     * sailing service creation by implementing a given routing information providing interface,
     * see @see 
     * 
     * @return
     */
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
    
    @Override
    protected StringMessages createStringMessages() {
        return StringMessages.INSTANCE;
    }
}
