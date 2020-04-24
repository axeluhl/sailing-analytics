package com.sap.sse.security.ui.client.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public abstract class AbstractRoleDefinitionDialog extends DataEntryDialog<RoleDefinitionDTO> {
    static class RoleValidator implements DataEntryDialog.Validator<RoleDefinitionDTO> {
        private final StringMessages stringMessages;
        private final Map<String, RoleDefinition> allOtherRoles;
        private boolean roleDefinitionNameFieldChanged = false;

        public RoleValidator(StringMessages stringMessages, Iterable<RoleDefinitionDTO> allOtherRoles) {
            super();
            this.stringMessages = stringMessages;
            this.allOtherRoles = new HashMap<>();
            for (final RoleDefinition role : allOtherRoles) {
                this.allOtherRoles.put(role.getName(), role);
            }
        }

        @Override
        public String getErrorMessage(RoleDefinitionDTO valueToValidate) {
            final String result;
            if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                result = stringMessages.pleaseEnterARoleName();
            } else if (roleDefinitionNameFieldChanged && allOtherRoles.containsKey(valueToValidate.getName())) {
                result = stringMessages.otherRoleWithNameAlreadyExists(valueToValidate.getName());
            } else {
                result = null;
            }
            return result;
        }
        
        void setRoleDefinitionNameFieldChanged(ChangeEvent ce) {
            this.roleDefinitionNameFieldChanged = true;
        }
    }

    protected final TextBox roleDefinitionNameField;
    protected final GenericStringListInlineEditorComposite<WildcardPermission> permissionsList;
    private final StringMessages stringMessages;

    public AbstractRoleDefinitionDialog(StringMessages stringMessages,
            Iterable<WildcardPermission> allExistingPermissions, RoleValidator roleValidator,
            DialogCallback<RoleDefinitionDTO> callback) {
        super(stringMessages.roles(), stringMessages.editRoles(), stringMessages.ok(), stringMessages.cancel(),
                roleValidator, /* animationEnabled */ true, callback);
        this.stringMessages = stringMessages;
        roleDefinitionNameField = createTextBox("", /* visible length */ 20);
        roleDefinitionNameField.addChangeHandler(roleValidator::setRoleDefinitionNameFieldChanged);
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
        permissionsList.addValueChangeHandler(e->validateAndUpdate());
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(2, 2);
        result.setWidget(0,  0, new Label(stringMessages.name()));
        result.setWidget(0, 1, roleDefinitionNameField);
        result.setWidget(1, 0, new Label(stringMessages.permissions()));
        result.setWidget(1, 1, permissionsList);
        return result;
    }

    @Override
    protected RoleDefinitionDTO getResult() {
        final String newName = roleDefinitionNameField.getText();
        final List<WildcardPermission> permissions = permissionsList.getValue();
        return new RoleDefinitionDTO(getRoleDefinitionId(), newName, permissions);
    }
    
    protected abstract UUID getRoleDefinitionId();
}
