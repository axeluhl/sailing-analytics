package com.sap.sse.gwt.adminconsole;

import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceFactory;

public interface AdminConsolePresenter {
    ErrorReporter getErrorReporter();

    UserService getUserService();
    
    SubscriptionServiceFactory getSubscriptionServiceFactory();
}
