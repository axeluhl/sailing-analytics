package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;

public class RoleDefinitionSuggestOracle extends AbstractListSuggestOracle<RoleDefinitionDTO> {

    public RoleDefinitionSuggestOracle(UserManagementServiceAsync userManagementService) {
        userManagementService.getRoleDefinitions(new AsyncCallback<ArrayList<RoleDefinitionDTO>>() {
            @Override
            public void onSuccess(ArrayList<RoleDefinitionDTO> result) {
                RoleDefinitionSuggestOracle.this.setSelectableValues(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO
            }
        });
    }

    public RoleDefinitionDTO fromString(String value) {
        // FIXME make sure this.getSelectableValues is already set
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
