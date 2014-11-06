package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.security.ui.client.AbstractSecureEntryPoint;

public abstract class AbstractSailingEntryPoint extends AbstractSecureEntryPoint {
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(com.sap.sailing.gwt.ui.client.StringMessages.class);
    }

    @Override
    protected com.sap.sailing.gwt.ui.client.StringMessages getStringMessages() {
        return (com.sap.sailing.gwt.ui.client.StringMessages) super.getStringMessages();
    }
}
