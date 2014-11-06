package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.StringMessages;

/**
 * Abstract entry point class, using this bundle's {@link com.sap.sse.security.ui.client.i18n.StringMessages}
 * for i18n support.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractSecurityEntryPoint extends AbstractSecureEntryPoint {

    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(com.sap.sse.security.ui.client.i18n.StringMessages.class);
    }

    @Override
    protected com.sap.sse.security.ui.client.i18n.StringMessages getStringMessages() {
        return (com.sap.sse.security.ui.client.i18n.StringMessages) super.getStringMessages();
    }

}
