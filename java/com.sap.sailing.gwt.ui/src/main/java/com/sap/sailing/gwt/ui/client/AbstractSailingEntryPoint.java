package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sse.security.ui.client.AbstractSecureEntryPoint;

public abstract class AbstractSailingEntryPoint<T> extends AbstractSecureEntryPoint<StringMessages> {
    protected T sailingService;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        getUserService().addKnownHasPermissions(SecuredDomainType.getAllInstances());
    }

    /**
     * Lazily initialize sailing service. Concrete entry point subclasses may influence sailing service creation by
     * implementing a given routing information providing interface {@link ProvidesLeaderboardRouting}. The routing
     * information provided via {@link ProvidesLeaderboardRouting#getLeaderboardName()} needs to be available when this
     * method is called for the first time. Repetitive calls will not cause a new leaderboard name to take effect.
     */
    abstract protected T getSailingService();
    
    @Override
    protected StringMessages createStringMessages() {
        return StringMessages.INSTANCE;
    }
}
