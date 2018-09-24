package com.sap.sailing.gwt.ui.client;

import java.util.function.Consumer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.ServiceRoutingProvider;
import com.sap.sse.security.ui.client.AbstractSecureEntryPoint;

public abstract class AbstractSailingEntryPoint extends AbstractSecureEntryPoint<StringMessages> {
    private SailingServiceAsync sailingService;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
    }
    
    /**
     * Lazily initialize sailing service. Concrete entry point subclasses may influence sailing service creation by
     * implementing a given routing information providing interface {@link ProvidesLeaderboardRouting}. The routing
     * information provided via {@link ProvidesLeaderboardRouting#getLeaderboardName()} needs to be available when this
     * method is called for the first time. Repetitive calls will not cause a new leaderboard name to take effect.
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
    
    protected void runWithServerInfo(final Consumer<ServerInfoDTO> serverInfoConsumer) {
        getSailingService().getServerInfo(new AsyncCallback<ServerInfoDTO>() {
            @Override
            public void onSuccess(ServerInfoDTO result) {
                serverInfoConsumer.accept(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                serverInfoConsumer.accept(null);
            }
        });
    }
    
    @Override
    protected StringMessages createStringMessages() {
        return StringMessages.INSTANCE;
    }
}
