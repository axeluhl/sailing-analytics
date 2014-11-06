package com.sap.sailing.gwt.ui.client;

import com.sap.sse.security.ui.client.AbstractSecureEntryPoint;

public abstract class AbstractSailingEntryPoint extends AbstractSecureEntryPoint {
    @Override
    protected com.sap.sailing.gwt.ui.client.StringMessages getStringMessages() {
        return (com.sap.sailing.gwt.ui.client.StringMessages) super.getStringMessages();
    }
}
