package com.sap.sse.security.ui.client.component;

import java.util.UUID;

import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RoleDefinitionEditDialog extends AbstractRoleDefinitionDialog {
    private final UUID roleDefinitionId;
    
    public RoleDefinitionEditDialog(RoleDefinition roleDefinition, StringMessages stringMessages,
            Iterable<WildcardPermission> allExistingPermissions, Iterable<RoleDefinitionDTO> allOtherRoles,
            DialogCallback<RoleDefinitionDTO> callback) {
        super(stringMessages, allExistingPermissions, allOtherRoles, callback);
        roleDefinitionId = roleDefinition.getId();
        roleDefinitionNameField.setText(roleDefinition.getName());
        permissionsList.setValue(roleDefinition.getPermissions());
    }

    @Override
    protected UUID getRoleDefinitionId() {
        return roleDefinitionId;
    }

}
