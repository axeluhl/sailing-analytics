package com.sap.sailing.dashboards.gwt.client.i18n;

import com.google.gwt.core.client.GWT;

public interface StringMessages extends com.sap.sailing.gwt.ui.client.StringMessages {
    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);

    String sampleText();
}
