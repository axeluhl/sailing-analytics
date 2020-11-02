package com.sap.sailing.gwt.ui.adminconsole;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;

public class SwissTimingReplayConnectorPanelSupplier
        extends AdminConsolePanelSupplier<SwissTimingReplayConnectorPanel> {

    private Logger logger = Logger.getLogger(this.getClass().toString());

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final CellTableWithCheckboxResources tableResources;

    public SwissTimingReplayConnectorPanelSupplier(StringMessages stringMessages, Presenter presenter,
            CellTableWithCheckboxResources tableResources) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.tableResources = tableResources;
    }

    public SwissTimingReplayConnectorPanel init() {
        logger.info("Create RegattaManagementPanel");
        SwissTimingReplayConnectorPanel swissTimingReplayConnectorPanel = new SwissTimingReplayConnectorPanel(presenter,
                stringMessages, tableResources);
        swissTimingReplayConnectorPanel.ensureDebugId("swissTimingReplayConnectorPanel");
        presenter.getRegattasDisplayers().add(swissTimingReplayConnectorPanel);
        presenter.fillRegattas();
        return swissTimingReplayConnectorPanel;
    }

    @Override
    public void getAsync(RunAsyncCallback callback) {
        GWT.runAsync(new RunAsyncCallback() {

            @Override
            public void onSuccess() {
                widget = init();
                callback.onSuccess();
            }

            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }
        });
    }

}
