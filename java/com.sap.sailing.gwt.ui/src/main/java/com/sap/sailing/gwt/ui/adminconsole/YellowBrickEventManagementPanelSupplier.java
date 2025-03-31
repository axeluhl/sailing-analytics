package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;

public class YellowBrickEventManagementPanelSupplier extends AdminConsolePanelSupplier<YellowBrickEventManagementPanel> {
    private final StringMessages stringMessages;
    private final Presenter presenter;
    private final AdminConsoleTableResources tableResources;

    public YellowBrickEventManagementPanelSupplier(final StringMessages stringMessages, final Presenter presenter,
            AdminConsoleTableResources tableResources) {
        super();
        this.stringMessages = stringMessages;
        this.presenter = presenter;
        this.tableResources = tableResources;
    }

    @Override
    public YellowBrickEventManagementPanel init() {
        YellowBrickEventManagementPanel yellowBrickEventManagementPanel = new YellowBrickEventManagementPanel(
                presenter, stringMessages, tableResources);
        yellowBrickEventManagementPanel.ensureDebugId("YellowBrickEventManagement");
        yellowBrickEventManagementPanel.refreshYellowBrickConnectors();
        presenter.getRegattasRefresher().addDisplayerAndCallFillOnInit(yellowBrickEventManagementPanel.getRegattasDisplayer());
        return yellowBrickEventManagementPanel;
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
