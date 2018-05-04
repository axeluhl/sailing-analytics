package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.security.ui.client.WithSecurity;

/**
 * A client factory that can deliver the sailing-specific services.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SailingClientFactory extends ClientFactory, WithSecurity {

    SailingServiceAsync getSailingService();

    SailingServiceAsync getSailingService(ProvidesLeaderboardRouting routingProvider);

    MediaServiceAsync getMediaService();

    ErrorView createErrorView(String errorMessage, Throwable errorReason);
}
