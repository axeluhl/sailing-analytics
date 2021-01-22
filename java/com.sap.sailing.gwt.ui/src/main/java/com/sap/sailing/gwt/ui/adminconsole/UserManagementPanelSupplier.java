package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.adminconsole.AdminConsolePanelSupplier;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel;

public class UserManagementPanelSupplier
        extends AdminConsolePanelSupplier<UserManagementPanel<AdminConsoleTableResources>> {

    private final Presenter presenter;
    private final AdminConsoleTableResources tableResources;

    public UserManagementPanelSupplier(final Presenter presenter, final AdminConsoleTableResources tableResources) {
        super();
        this.tableResources = tableResources;
        this.presenter = presenter;
    }

    @Override
    public UserManagementPanel<AdminConsoleTableResources> init() {
        logger.info("Create UserManagementPanel");
        final UserManagementPanel<AdminConsoleTableResources> userManagementPanel = new UserManagementPanelWrapper(
                presenter.getUserService(), StringMessages.INSTANCE,
                SecuredDomainType.getAllInstances(), presenter.getErrorReporter(), tableResources);
        userManagementPanel.ensureDebugId("UserManagementPanel");
        return userManagementPanel;
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