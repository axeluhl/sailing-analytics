package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;

public class WindPanel extends FormPanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    public WindPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        StringConstants stringConstants = GWT.create(StringConstants.class);
        this.setWidget(new Label(stringConstants.windPanelLabel()));
    }
}
