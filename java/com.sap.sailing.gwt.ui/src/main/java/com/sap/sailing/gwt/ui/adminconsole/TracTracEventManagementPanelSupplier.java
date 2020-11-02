package com.sap.sailing.gwt.ui.adminconsole;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;

public class TracTracEventManagementPanelSupplier extends AdminConsolePanelSupplier<TracTracEventManagementPanel> {

    private Logger logger = Logger.getLogger(this.getClass().toString());

    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final AdminConsoleTableResources tableResources;

    public TracTracEventManagementPanelSupplier(StringMessages stringMessages, Presenter presenter,
            AdminConsoleTableResources tableResources) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.tableResources = tableResources;
    }

    public TracTracEventManagementPanel init() {
        logger.info("Create RegattaManagementPanel");
        TracTracEventManagementPanel tractracEventManagementPanel = new TracTracEventManagementPanel(
                presenter.getSailingService(), presenter.getUserService(), presenter.getErrorReporter(), presenter,
                stringMessages, tableResources);
        tractracEventManagementPanel.ensureDebugId("TracTracEventManagement");
        tractracEventManagementPanel.refreshTracTracConnectors();
        presenter.getRegattasDisplayers().add(tractracEventManagementPanel);
        presenter.fillRegattas();
        return tractracEventManagementPanel;
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
