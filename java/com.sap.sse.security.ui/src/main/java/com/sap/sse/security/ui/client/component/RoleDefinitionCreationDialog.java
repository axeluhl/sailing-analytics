package com.sap.sse.security.ui.client.component;

import java.util.UUID;

import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RoleDefinitionCreationDialog extends AbstractRoleDefinitionDialog {
    private final UUID newRoleDefinitionId;
    
    public RoleDefinitionCreationDialog(StringMessages stringMessages, Iterable<WildcardPermission> allExistingPermissions,
            Iterable<RoleDefinitionDTO> allOtherRoles, DialogCallback<RoleDefinitionDTO> callback) {
        super(stringMessages, allExistingPermissions, allOtherRoles, callback);
        newRoleDefinitionId = UUID.randomUUID();
    }

    @Override
    protected UUID getRoleDefinitionId() {
        return newRoleDefinitionId;
    }

    @Override
    public void show() {
        super.show();
        roleDefinitionNameField.setFocus(true);
    }
}
