package com.sap.sse.security.ui.client.component.usergroup.roles;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RoleDefinitionSuggestOracle extends AbstractListSuggestOracle<RoleDefinitionDTO> {

    public RoleDefinitionSuggestOracle(final UserManagementServiceAsync userManagementService,
            final StringMessages stringMessages) {
        userManagementService.getRoleDefinitions(new AsyncCallback<ArrayList<RoleDefinitionDTO>>() {
            @Override
            public void onSuccess(ArrayList<RoleDefinitionDTO> result) {
                RoleDefinitionSuggestOracle.this.setSelectableValues(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotLoadRoles());
            }
        });
    }

    public RoleDefinitionDTO fromString(final String value) {
        if (this.getSelectableValues() == null) {
            throw new NullPointerException("Role definitions are not loaded yet or could not be loaded.");
        }

        for (RoleDefinitionDTO role : this.getSelectableValues()) {
            if (role.getName().equals(value)) {
                return role;
            }
        }
        return null;
    }

    @Override
    protected String createSuggestionKeyString(RoleDefinitionDTO value) {
        return value.getName();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(RoleDefinitionDTO value) {
        return null;
    }

    @Override
    protected Iterable<String> getMatchingStrings(RoleDefinitionDTO value) {
        return this.getSelectableValues().stream().map(r -> createSuggestionKeyString(r)).collect(Collectors.toList());
    }
}
