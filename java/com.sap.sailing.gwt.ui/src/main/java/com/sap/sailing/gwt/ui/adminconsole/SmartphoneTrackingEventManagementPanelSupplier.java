package com.sap.sailing.gwt.ui.adminconsole;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;

public class SmartphoneTrackingEventManagementPanelSupplier extends AdminConsolePanelSupplier<SmartphoneTrackingEventManagementPanel> {

    private Logger logger = Logger.getLogger(this.getClass().toString());

    private final StringMessages stringMessages;
    private final Presenter presenter;

    public SmartphoneTrackingEventManagementPanelSupplier(StringMessages stringMessages, Presenter presenter) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
    }

    public SmartphoneTrackingEventManagementPanel init() {
        logger.info("Create RegattaManagementPanel");
        final SmartphoneTrackingEventManagementPanel raceLogTrackingEventManagementPanel = new SmartphoneTrackingEventManagementPanel(
                presenter, stringMessages);
        raceLogTrackingEventManagementPanel.ensureDebugId("SmartphoneTrackingPanel");
        presenter.getRegattasDisplayers().add(raceLogTrackingEventManagementPanel);
        presenter.getLeaderboardsDisplayer().add(raceLogTrackingEventManagementPanel);
        presenter.fillRegattas();
        presenter.fillLeaderboards();
        return raceLogTrackingEventManagementPanel;
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