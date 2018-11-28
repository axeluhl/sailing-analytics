package com.sap.sse.security.ui.client.usermanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.AbstractRole;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.dto.AccountDTO;
import com.sap.sse.security.shared.dto.RoleDTO;
import com.sap.sse.security.shared.dto.RoleDefinitionDTO;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AbstractUserDialog.UserData;
import com.sap.sse.security.ui.client.component.ChangePasswordDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

/**
 * Edits a {@link UserDTO} object. {@link Role} handling is a bit special. As the
 * {@link UserManagementServiceAsync#setRolesForUser(String, Iterable, AsyncCallback)} method accepts a {@link Triple}
 * with the role definition UUID and optional tenant and user qualifications, the {@link UserDTO#getRoles()} value is
 * not suited for that method call. Additionally, the dialog doesn't have available all tenants / users to check a
 * qualification. All this dialog does is acquire the set of all {@link RoleDefinition}s known by the server so as
 * to offer the possible values.<p>
 * 
 * Therefore, the return type for this dialog is a {@link UserDTO}, augmented by the roles captured for the user,
 * as triples of role definition ID and optional tenant/user qualifications.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class UserEditDialog extends DataEntryDialog<Pair<UserDTO, Iterable<Triple<UUID, String, String>>>> {
    private final UserDTO userToEdit;
    private final TextBox username;
    private final TextBox fullName;
    private final TextBox company;
    private final TextBox email;
    private final VerticalPanel accountPanels;
    private final StringListEditorComposite permissionsEditor;
    private StringListEditorComposite rolesEditor; // set asynchronously after role definitions have been obtained from server
    private final FlowPanel rolesEditorWrapper;
    private final Map<String, RoleDefinition> serverRoleDefinitionsByName;

    private final UserService userService;
    private final ErrorReporter errorReporter;
    private static final StringMessages stringMessages = StringMessages.INSTANCE;
    
    /**
     * The class creates the UI-dialog to type in the Data about a competitor.
     * 
     * @param userToEdit
     *            The 'userToEdit' parameter contains the user which should be changed or initialized.
     */
    public UserEditDialog(UserDTO userToEdit, DialogCallback<Pair<UserDTO, Iterable<Triple<UUID, String, String>>>> callback,
            UserService userService, Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter) {
        super(stringMessages.editUser(), null, stringMessages.ok(), stringMessages
                .cancel(), /* validator */ null, /* animationEnabled */true, callback);
        this.ensureDebugId("UserEditDialog");
        this.errorReporter = errorReporter;
        this.userService = userService;
        this.serverRoleDefinitionsByName = new HashMap<>();
        this.userToEdit = userToEdit;
        this.username = createTextBox(userToEdit.getName());
        username.ensureDebugId("UsernameTextBox");
        username.setEnabled(false); // the username is key and cannot be changed
        this.email = createTextBox(userToEdit.getEmail());
        this.fullName = createTextBox(userToEdit.getFullName());
        this.company = createTextBox(userToEdit.getCompany());
        List<String> defaultPermissionNames = new ArrayList<>();
        for (final HasPermissions permission : additionalPermissions) {
            for (final Action action : permission.getAvailableActions()) {
                defaultPermissionNames.add(permission.getStringPermission(action));
            }
        }
        permissionsEditor = new StringListEditorComposite(userToEdit==null?Collections.<String>emptySet():userToEdit.getStringPermissions(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), defaultPermissionNames,
                stringMessages.enterPermissionName());
        rolesEditorWrapper = new FlowPanel();
        updateRolesAndInitializeRolesEditor(userToEdit);
        this.accountPanels = new VerticalPanel();
        for (AccountDTO a : userToEdit.getAccounts()) {
            DecoratorPanel accountPanelDecorator = new DecoratorPanel();
            FlowPanel accountPanelContent = new FlowPanel();
            accountPanelDecorator.setWidget(accountPanelContent);
            accountPanelContent.add(new Label(stringMessages.account(a.getAccountType())));
            if (a instanceof UsernamePasswordAccountDTO) {
                final Button changePasswordButton = new Button(stringMessages.changePassword());
                accountPanelContent.add(changePasswordButton);
                changePasswordButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        new ChangePasswordDialog(stringMessages, getUserManagementService(), userToEdit, new DataEntryDialog.DialogCallback<UserData>() {
                            @Override
                            public void ok(UserData userData) {
                                getUserManagementService().updateSimpleUserPassword(userToEdit.getName(), /* admin doesn't need to provide old password */ null,
                                        /* resetPasswordSecret */ null, userData.getPassword(), new MarkedAsyncCallback<Void>(
                                        new AsyncCallback<Void>() {
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                GWT.log(caught.getMessage());
                                                if (caught instanceof UserManagementException) {
                                                    String message = ((UserManagementException) caught).getMessage();
                                                    if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                                                        errorReporter.reportError(stringMessages.passwordDoesNotMeetRequirements());
                                                    } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                                        errorReporter.reportError(stringMessages.invalidCredentials());
                                                    } else {
                                                        errorReporter.reportError(stringMessages.errorChangingPassword(caught.getMessage()));
                                                    }
                                                } else {
                                                    errorReporter.reportError(stringMessages.errorChangingPassword(caught.getMessage()));
                                                }
                                            }

                                            @Override
                                            public void onSuccess(Void result) {
                                                Notification.notify(stringMessages.passwordSuccessfullyChanged(), NotificationType.SUCCESS);
                                            }
                                        }));
                            }
                            @Override public void cancel() { }
                        }).show();
                    }
                });
            } else if (a instanceof SocialUserDTO) {
                SocialUserDTO sua = (SocialUserDTO) a;
                FlexTable table = new FlexTable();
                int i = 0;
                for (Entry<String, String> e : sua.getProperties().entrySet()) {
                    if (e.getValue() != null) {
                        table.setText(i, 0, e.getKey().toLowerCase().replace('_', ' '));
                        table.setText(i, 1, e.getValue());
                        i++;
                    }
                }
                accountPanelContent.add(table);
            }
            accountPanels.add(accountPanelDecorator);
        }
    }
    
    private UserManagementServiceAsync getUserManagementService() {
        return userService.getUserManagementService();
    }

    private void updateRolesAndInitializeRolesEditor(final UserDTO user) {
        getUserManagementService().getRoleDefinitions(new AsyncCallback<ArrayList<RoleDefinitionDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
            @Override
            public void onSuccess(ArrayList<RoleDefinitionDTO> roleDefinitions) {
                serverRoleDefinitionsByName.clear();
                for (final RoleDefinitionDTO roleDefinition : roleDefinitions) {
                    serverRoleDefinitionsByName.put(roleDefinition.getName(), roleDefinition);
                }
                setRolesEditor(user);
            }
        });
    }

    /**
     * Assumes {@link #serverRoleDefinitionsByName} to be up to date; ideally called from an onSuccess callback after
     * retrieving a fresh roles copy from the server
     */
    private void setRolesEditor(UserDTO user) {
        rolesEditor = new StringListEditorComposite(
                user == null ? Collections.<String> emptySet() : user.getStringRoles(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), serverRoleDefinitionsByName.keySet(),
                stringMessages.enterRoleName());
        while (rolesEditorWrapper.getWidgetCount() > 0) {
            rolesEditorWrapper.remove(0);
        }
        rolesEditorWrapper.add(rolesEditor);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return email;
    }

    @Override
    protected Pair<UserDTO, Iterable<Triple<UUID, String, String>>> getResult() {
        final Iterable<Triple<UUID, String, String>> roles;
        if (rolesEditor != null) {
            final ArrayList<UUID> newRoleDefinitionIds = new ArrayList<>();
            for (String roleName : rolesEditor.getValue()) {
                final Triple<String, String, String> roleDefinitionNameAndTenantQualifierNameAndUserQualifierName = AbstractRole
                        .getRoleDefinitionNameAndTenantQualifierNameAndUserQualifierName(roleName);
                RoleDefinition roleDefinition = serverRoleDefinitionsByName.get(roleDefinitionNameAndTenantQualifierNameAndUserQualifierName.getA());
                if (roleDefinition != null) {
                    newRoleDefinitionIds.add(roleDefinition.getId());
                }    
            }
            roles = Util.map(rolesEditor.getValue(), roleName->{
                final Triple<String, String, String> roleDefinitionNameAndTenantQualifierNameAndUserQualifierName = AbstractRole
                        .getRoleDefinitionNameAndTenantQualifierNameAndUserQualifierName(roleName);
                
                final String roleNameToAdd = roleDefinitionNameAndTenantQualifierNameAndUserQualifierName.getA();
                final String roleTenantNameToAdd = roleDefinitionNameAndTenantQualifierNameAndUserQualifierName.getB();
                final String roleUsernameToAdd = roleDefinitionNameAndTenantQualifierNameAndUserQualifierName.getC();
                
                RoleDefinition roleDefinition = null;
                for (RoleDTO role : userToEdit.getRoles()) {
                    final String existingRoleName = role.getName();
                    final String existingTenantName = role.getQualifiedForTenant() == null ? null : role.getQualifiedForTenant().getName();
                    final String existingUsername = role.getQualifiedForUser() == null ? null : role.getQualifiedForUser().getName();
                    
                    // handles unchanged role associations to not rely on the current user to see all roles already associated
                    if (Util.equalsWithNull(existingRoleName, roleNameToAdd)
                            && Util.equalStringsWithEmptyIsNull(existingTenantName, roleTenantNameToAdd)
                            && Util.equalStringsWithEmptyIsNull(existingUsername, roleUsernameToAdd)) {
                        roleDefinition =role.getRoleDefinition();
                        break;
                    }
                }
                if (roleDefinition == null) {
                    roleDefinition = serverRoleDefinitionsByName.get(roleDefinitionNameAndTenantQualifierNameAndUserQualifierName.getA());
                }
                if (roleDefinition == null) {
                    // FIXME how to handle this case? Use null as role definition ID and validate afterwards
                    return null;
                }
                return new Triple<>(
                    roleDefinition.getId(), /* qualifying tenant name */ roleTenantNameToAdd,
                    /* qualifying user name */ roleUsernameToAdd);
            });
        } else {
            roles = Collections.emptyList();
        }
        final Iterable<WildcardPermission> newPermissionList = Util.map(permissionsEditor.getValue(), p->new WildcardPermission(p));
        UserDTO user = new UserDTO(userToEdit.getName(), email.getText(), fullName.getText(), company.getText(), userToEdit.getLocale(), userToEdit.isEmailValidated(),
                userToEdit.getAccounts(), /* roles */ Collections.emptyList(), userToEdit.getDefaultTenant(), newPermissionList, userToEdit.getUserGroups());
        return new Pair<>(user, roles);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(7, 2);
        result.setWidget(0, 0, new Label(stringMessages.username()));
        result.setWidget(0, 1, username);
        result.setWidget(1, 0, new Label(stringMessages.name()));
        result.setWidget(1, 1, fullName);
        result.setWidget(2, 0, new Label(stringMessages.email()));
        result.setWidget(2, 1, email);
        result.setWidget(3, 0, new Label(stringMessages.company()));
        result.setWidget(3, 1, company);
        result.setWidget(4, 0, new Label(stringMessages.permissions()));
        result.setWidget(4, 1, permissionsEditor);
        result.setWidget(5, 0, new Label(stringMessages.roles()));
        result.setWidget(5, 1, rolesEditorWrapper);
        result.setWidget(6, 0, accountPanels);
        return result;
    }

}
