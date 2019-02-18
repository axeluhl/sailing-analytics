package com.sap.sse.security.ui.client.component.usergroup.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.StrippedRoleDefinitionDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.roles.UserRoleDefinitionPanel;

/**
 * Suggest oracle for use in {@link UserGroupRoleDefinitionPanel} and {@link UserRoleDefinitionPanel}, which oracles the
 * role names.
 */
public class RoleDefinitionSuggestOracle extends MultiWordSuggestOracle {

    private final Map<String, StrippedRoleDefinitionDTO> allRoles = new HashMap<>();

    public RoleDefinitionSuggestOracle(final UserManagementServiceAsync userManagementService,
            final StringMessages stringMessages) {
        userManagementService.getRoleDefinitions(new AsyncCallback<ArrayList<RoleDefinitionDTO>>() {
            @Override
            public void onSuccess(ArrayList<RoleDefinitionDTO> result) {
                for (RoleDefinitionDTO role : result) {
                    allRoles.put(role.getName(),
                            new StrippedRoleDefinitionDTO(role.getId(), role.getName(), role.getPermissions()));
                }
                RoleDefinitionSuggestOracle.super.clear();
                RoleDefinitionSuggestOracle.super.setDefaultSuggestionsFromText(allRoles.keySet());
                RoleDefinitionSuggestOracle.super.addAll(allRoles.keySet());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(stringMessages.couldNotLoadRoles());
            }
        });
    }

    /**
     * @returns a {@link StrippedRoleDefinitionDTO}-object from the current selectable values of this oracle, which is
     *          associated with the role name.
     */
    public StrippedRoleDefinitionDTO fromString(final String roleName) {
        if (allRoles.isEmpty()) {
            throw new NullPointerException("Role definitions are not loaded yet or could not be loaded.");
        }
        return allRoles.get(roleName);
    }

    /**
     * Clears the oracle suggestions, adds all existing roles and finally removes the existing roles from the oracle.
     */
    public void resetAndRemoveExistingRoles(Iterable<StrippedRoleDefinitionDTO> existingRoles) {
        ArrayList<StrippedRoleDefinitionDTO> roles = new ArrayList<>(allRoles.values());
        Util.removeAll(existingRoles, roles);
        List<String> textSuggestions = roles.stream().map(StrippedRoleDefinitionDTO::getName)
                .collect(Collectors.toList());
        super.clear();
        super.setDefaultSuggestionsFromText(textSuggestions);
        super.addAll(textSuggestions);
    }
}
