package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.FilterablePanelProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.UserManagementPanel;

public class UserManagementPanelWrapper extends UserManagementPanel<AdminConsoleTableResources> implements FilterablePanelProvider<UserDTO> {
    
    public UserManagementPanelWrapper(final UserService userService, final StringMessages stringMessages,
            Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter, AdminConsoleTableResources tableResources) {
        super(userService, stringMessages, additionalPermissions, errorReporter, tableResources);
    }

    @Override
    public AbstractFilterablePanel<UserDTO> getFilterablePanel() {
        return userList.getFilterField();
    }

}
