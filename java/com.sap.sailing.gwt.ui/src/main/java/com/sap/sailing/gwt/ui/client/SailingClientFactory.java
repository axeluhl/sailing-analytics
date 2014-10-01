package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.gwt.client.mvp.ErrorView;

/**
 * A client factory that can deliver the sailing-specific services.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SailingClientFactory extends ClientFactory {
    SailingServiceAsync getSailingService();
    
    ErrorView createErrorView(String errorMessage, Throwable errorReason);
}
