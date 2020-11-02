package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.security.ui.client.component.UserGroupManagementPanel;

public class UserGroupManagementPanelSupplier extends AdminConsolePanelSupplier<UserGroupManagementPanel> {

    private final AdminConsoleTableResources tableResources;
    private final Presenter presenter;

    public UserGroupManagementPanelSupplier(final Presenter presenter,
            final AdminConsoleTableResources tableResources) {
        super();
        this.tableResources = tableResources;
        this.presenter = presenter;
    }

    @Override
    public UserGroupManagementPanel init() {
        logger.info("Create UserGroupManagementPanel");
        final UserGroupManagementPanel userGroupManagementPanel = new UserGroupManagementPanel(
                presenter.getUserService(), StringMessages.INSTANCE, SecuredDomainType.getAllInstances(),
                presenter.getErrorReporter(), tableResources);
        userGroupManagementPanel.ensureDebugId("userGroupManagementPanel");
        return userGroupManagementPanel;
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