package com.sap.sailing.gwt.ui.client.refresh;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.gwt.client.mvp.ErrorView;

public interface ErrorAndBusyClientFactory {

    IsWidget createBusyView();
    
    ErrorView createErrorView(String errorMessage, Throwable errorReason);
}
