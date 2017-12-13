package com.sap.sse.security.ui.client.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public abstract class AbstractRoleDialog extends DataEntryDialog<Role> {
    private static class RoleValidator implements DataEntryDialog.Validator<Role> {
        private final StringMessages stringMessages;
        private final Map<String, Role> allOtherRoles;

        public RoleValidator(StringMessages stringMessages, Iterable<Role> allOtherRoles) {
            super();
            this.stringMessages = stringMessages;
            this.allOtherRoles = new HashMap<>();
            for (final Role role : allOtherRoles) {
                this.allOtherRoles.put(role.getName(), role);
            }
        }

        @Override
        public String getErrorMessage(Role valueToValidate) {
            final String result;
            if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                result = stringMessages.pleaseEnterARoleName();
            } else if (allOtherRoles.containsKey(valueToValidate.getName())) {
                result = stringMessages.otherRoleWithNameAlreadyExists(valueToValidate.getName());
            } else {
                result = null;
            }
            return result;
        }
    }

    protected final TextBox roleNameField;
    protected final GenericStringListInlineEditorComposite<WildcardPermission> permissionsList;
    private final StringMessages stringMessages;

    public AbstractRoleDialog(StringMessages stringMessages, Iterable<WildcardPermission> allExistingPermissions,
            Iterable<Role> allOtherRoles,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<Role> callback) {
        super(stringMessages.roles(), stringMessages.editRoles(), stringMessages.ok(), stringMessages.cancel(),
                new RoleValidator(stringMessages, allOtherRoles), /* animationEnabled */ true, callback);
        this.stringMessages = stringMessages;
        roleNameField = createTextBox("", /* visible length */ 20);
        permissionsList = new GenericStringListInlineEditorComposite<WildcardPermission>(Collections.emptySet(),
                stringMessages, IconResources.INSTANCE.removeIcon(), Util.map(allExistingPermissions, p->p.toString()),
                /* text box size */ 20) {
                    @Override
                    protected WildcardPermission parse(String s) {
                        return s==null || s.isEmpty() ? null : new WildcardPermission(s);
                    }

                    @Override
                    protected WildcardPermission parse(String s, WildcardPermission valueToUpdate) {
                        return parse(s);
                    }

                    @Override
                    protected String toString(WildcardPermission value) {
                        return value.toString();
                    }
        };
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(2, 2);
        result.setWidget(0,  0, new Label(stringMessages.name()));
        result.setWidget(0, 1, roleNameField);
        result.setWidget(1, 0, new Label(stringMessages.permissions()));
        result.setWidget(1, 1, permissionsList);
        return result;
    }

    @Override
    protected Role getResult() {
        final String newName = roleNameField.getText();
        final List<WildcardPermission> permissions = permissionsList.getValue();
        final Role result = new RoleImpl(getRoleId(), newName);
        result.setPermissions(permissions);
        return result;
    }
    
    protected abstract UUID getRoleId();
}
