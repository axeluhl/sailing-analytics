package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sse.gwt.adminconsole.FilterablePanelProvider;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.RoleDefinitionsPanel;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RoleDefinitionsPanelWrapper extends RoleDefinitionsPanel implements FilterablePanelProvider<RoleDefinitionDTO> {
    public RoleDefinitionsPanelWrapper(StringMessages stringMessages, UserService userService,
            CellTableWithCheckboxResources tableResources, ErrorReporter errorReporter) {
        super(stringMessages, userService, tableResources, errorReporter);
    }

    @Override
    public AbstractFilterablePanel<RoleDefinitionDTO> getFilterablePanel() {
        return filterablePanelRoleDefinitions;
    }
}
