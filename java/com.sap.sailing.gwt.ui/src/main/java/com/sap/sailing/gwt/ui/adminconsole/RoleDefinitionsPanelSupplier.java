package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.security.ui.client.component.RoleDefinitionsPanel;

public class RoleDefinitionsPanelSupplier extends AdminConsolePanelSupplier<RoleDefinitionsPanel> {

    private final Presenter presenter;
    private final AdminConsoleTableResources tableResources;

    public RoleDefinitionsPanelSupplier(final Presenter presenter, final AdminConsoleTableResources tableResources) {
        super();
        this.tableResources = tableResources;
        this.presenter = presenter;
    }

    @Override
    public RoleDefinitionsPanel init() {
        logger.info("Create RoleDefinitionsPanel");
        final RoleDefinitionsPanel roleManagementPanel = new RoleDefinitionsPanelWrapper(StringMessages.INSTANCE,
                presenter.getUserService(), tableResources, presenter.getErrorReporter());
        roleManagementPanel.ensureDebugId("roleManagementPanel");
        return roleManagementPanel;
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