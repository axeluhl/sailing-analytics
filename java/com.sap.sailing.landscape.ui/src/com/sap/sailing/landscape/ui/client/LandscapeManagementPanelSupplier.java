package com.sap.sailing.landscape.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.AdminConsolePresenter;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;

public class LandscapeManagementPanelSupplier extends AdminConsolePanelSupplier<LandscapeManagementPanel> {
    private final AdminConsolePresenter presenter;
    private final AdminConsoleTableResources tableResources;

    public LandscapeManagementPanelSupplier(final AdminConsolePresenter presenter, final AdminConsoleTableResources tableResources) {
        super();
        this.tableResources = tableResources;
        this.presenter = presenter;
    }

    @Override
    public LandscapeManagementPanel init() {
        logger.info("Create LandscapeManagementPanel");
        final LandscapeManagementPanel landscapeManagementPanel = new LandscapeManagementPanel(
                StringMessages.INSTANCE, presenter.getUserService(), tableResources, presenter.getErrorReporter());
        landscapeManagementPanel.ensureDebugId("LandscapeManagementPanel");
        return landscapeManagementPanel;
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