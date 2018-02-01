package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Abstract entry point class, using this bundle's {@link com.sap.sse.security.ui.client.i18n.StringMessages}
 * for i18n support.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractSecurityEntryPoint extends AbstractSecureEntryPoint<StringMessages> {
    /**
     * The URL parameter name used to pass an application name to this entry point. This name will be put into the login
     * view's header.
     */
    protected static final String PARAM_APP = "app";
    
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }

    protected String getApplicationName(String defaultName) {
        String appName = Window.Location.getParameter(PARAM_APP);
        if(appName == null) {
            appName = defaultName;
        }
        return appName;
    }
}
