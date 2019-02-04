package com.sap.sse.security.ui.client.component.usergroup.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.suggestion.AbstractListSuggestOracle;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedRoleDefinitionDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/** Suggest oracle for use in {@link UserGroupRoleDefinitionPanel} which oracles the role names */
public class RoleDefinitionSuggestOracle extends AbstractListSuggestOracle<StrippedRoleDefinitionDTO> {

    private Collection<StrippedRoleDefinitionDTO> allRoles = new ArrayList<>();

    public RoleDefinitionSuggestOracle(final UserManagementServiceAsync userManagementService,
            final StringMessages stringMessages) {
        userManagementService.getRoleDefinitions(new AsyncCallback<ArrayList<RoleDefinitionDTO>>() {
            @Override
            public void onSuccess(ArrayList<RoleDefinitionDTO> result) {
                for (RoleDefinitionDTO role : result) {
                    allRoles.add(new StrippedRoleDefinitionDTO(role.getId(), role.getName(), role.getPermissions()));
                }
                RoleDefinitionSuggestOracle.this.setSelectableValues(allRoles);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotLoadRoles());
            }
        });
    }

    public StrippedRoleDefinitionDTO fromString(final String value) {
        if (this.getSelectableValues() == null) {
            throw new NullPointerException("Role definitions are not loaded yet or could not be loaded.");
        }

        for (StrippedRoleDefinitionDTO role : this.getSelectableValues()) {
            if (role.getName().equals(value)) {
                return role;
            }
        }
        return null;
    }

    public void resetAndRemoveExistingRoles(Iterable<StrippedRoleDefinitionDTO> existingRoles) {
        ArrayList<StrippedRoleDefinitionDTO> roles = new ArrayList<>(allRoles);
        Util.removeAll(existingRoles, roles);
        RoleDefinitionSuggestOracle.this.setSelectableValues(roles);
    }

    @Override
    protected String createSuggestionKeyString(StrippedRoleDefinitionDTO value) {
        return value.getName();
    }

    @Override
    protected String createSuggestionAdditionalDisplayString(StrippedRoleDefinitionDTO value) {
        return null;
    }

    @Override
    protected Iterable<String> getMatchingStrings(StrippedRoleDefinitionDTO value) {
        return this.getSelectableValues().stream().map(r -> createSuggestionKeyString(r)).collect(Collectors.toList());
    }
}
