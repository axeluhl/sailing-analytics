package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.sap.sse.security.ui.client.AbstractSecureEntryPoint;

public abstract class AbstractSailingEntryPoint extends AbstractSecureEntryPoint<StringMessages> {
    @Override
    protected StringMessages createStringMessages() {
        return GWT.create(StringMessages.class);
    }
}
