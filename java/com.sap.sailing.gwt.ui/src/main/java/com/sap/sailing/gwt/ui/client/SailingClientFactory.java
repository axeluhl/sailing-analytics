package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.mvp.ClientFactory;
import com.sap.sse.gwt.client.mvp.ErrorView;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;

/**
 * A client factory that can deliver the sailing-specific services.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SailingClientFactory extends ClientFactory {
    SailingServiceAsync getSailingService();

    MediaServiceAsync getMediaService();
    
    UserManagementServiceAsync getUserManagementService();
    
    UserService getUserService();
    
    ErrorView createErrorView(String errorMessage, Throwable errorReason);
}
