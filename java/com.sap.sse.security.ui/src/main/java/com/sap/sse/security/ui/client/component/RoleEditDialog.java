package com.sap.sse.security.ui.client.component;

import java.util.UUID;

import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RoleEditDialog extends AbstractRoleDialog {
    private final UUID roleId;
    
    public RoleEditDialog(Role role, StringMessages stringMessages,
            Iterable<WildcardPermission> allExistingPermissions, Iterable<Role> allOtherRoles, com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Role> callback) {
        super(stringMessages, allExistingPermissions, allOtherRoles, callback);
        roleId = role.getId();
        roleNameField.setText(role.getName());
        permissionsList.setValue(role.getPermissions());
    }

    @Override
    protected UUID getRoleId() {
        return roleId;
    }

}
