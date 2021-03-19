package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sse.gwt.adminconsole.FilterablePanelProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.security.shared.dto.UserGroupDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.UserGroupManagementPanel;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserGroupManagementPanelWrapper extends UserGroupManagementPanel implements FilterablePanelProvider<UserGroupDTO> {

    public UserGroupManagementPanelWrapper(UserService userService, StringMessages stringMessages,
            ErrorReporter errorReporter, CellTableWithCheckboxResources tableResources) {
        super(userService, stringMessages, errorReporter, tableResources);
    }

    @Override
    public AbstractFilterablePanel<UserGroupDTO> getFilterablePanel() {
        return userGroupTableWrapper.getFilterField();
    }
}
