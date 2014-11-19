package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Abstract entry point class, using this bundle's {@link com.sap.sse.security.ui.client.i18n.StringMessages}
 * for i18n support.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractSecurityEntryPoint extends AbstractSecureEntryPoint<StringMessages> {
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}
