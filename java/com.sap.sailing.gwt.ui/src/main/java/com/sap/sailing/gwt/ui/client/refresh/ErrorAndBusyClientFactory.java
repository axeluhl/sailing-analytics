package com.sap.sailing.gwt.ui.client.refresh;

import com.sap.sse.gwt.client.mvp.ErrorView;

/**
 * ClientFactory definition to create error and busy (loading indicator) views.
 */
public interface ErrorAndBusyClientFactory {
    BusyView createBusyView();
    
    ErrorView createErrorView(String errorMessage, Throwable errorReason);
}
