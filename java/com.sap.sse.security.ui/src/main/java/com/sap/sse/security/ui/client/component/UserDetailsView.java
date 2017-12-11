package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.DefaultPermissions;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.ui.client.IconResources;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AbstractUserDialog.UserData;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;

public class UserDetailsView extends FlowPanel {
    private List<UserChangeEventHandler> handlers = new ArrayList<>();

    private final StringMessages stringMessages;
    
    private final Label usernameLabel;
    private final Label emailLabel;
    private final StringListEditorComposite permissionsEditor;
    private final VerticalPanel accountPanels;
    private final ListBox allPermissionsList;
    private UserDTO user;
    private final ErrorReporter errorReporter;
    private final Map<String, Role> serverRoles;

    private final FlowPanel rolesEditorWrapper;

    public UserDetailsView(final UserService userService, UserDTO user, final StringMessages stringMessages,
            final UserListDataProvider userListDataProvider,
            Iterable<Permission> additionalPermissions, ErrorReporter errorReporter) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.user = user;
        serverRoles = new HashMap<>();
        addStyleName("userDetailsView");
        List<String> defaultPermissionNames = new ArrayList<>();
        for (DefaultPermissions defaultPermission : DefaultPermissions.values()) {
            defaultPermissionNames.add(defaultPermission.getStringPermission());
        }
        for (Permission permission : additionalPermissions) {
            defaultPermissionNames.add(permission.getStringPermission());
        }
        permissionsEditor = new StringListEditorComposite(user==null?Collections.<String>emptySet():user.getStringPermissions(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), defaultPermissionNames,
                stringMessages.enterPermissionName());
        permissionsEditor.addValueChangeHandler(new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                final Iterable<String> newPermissionList = event.getValue();
                final UserDTO selectedUser = UserDetailsView.this.user;
                userManagementService.setPermissionsForUser(selectedUser.getName(), newPermissionList, new MarkedAsyncCallback<SuccessInfo>(
                        new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.errorUpdatingPermissions(selectedUser.getName(), caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (!result.isSuccessful()) {
                                    Window.alert(stringMessages.errorUpdatingPermissions(selectedUser.getName(), result.getMessage()));
                                } else {
                                    userListDataProvider.updateDisplays();
                                    if (userService.getCurrentUser().getName().equals(selectedUser.getName())) {
                                        // if the current user's permissions changed, update the user object in the user service and notify others
                                        userService.updateUser(/* notify other instances */ true);
                                    }
                                }
                            }
                        }));
            }
        });
        usernameLabel = new Label();
        emailLabel = new Label();
        final Button changeEmail = new Button(stringMessages.edit());
        changeEmail.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final UserDTO clickedUser = UserDetailsView.this.user;
                new EditEmailDialogWithDefaultCallback(stringMessages, userManagementService, clickedUser,
                        new AsyncCallback<UserData>() {
                            @Override
                            public void onSuccess(UserData result) {
                                emailLabel.setText(result.getEmail());
                                for (UserChangeEventHandler handler : handlers) {
                                    handler.onUserChange(clickedUser);
                                }
                            }
                            @Override public void onFailure(Throwable caught) {}
                        }).show();
            }
        });
        Label title = new Label(stringMessages.userDetails());
        title.getElement().getStyle().setFontSize(25, Unit.PX);
        this.add(title);
        DecoratorPanel decoratorPanel = new DecoratorPanel();
        FlowPanel fp = new FlowPanel();
        fp.setWidth("100%");
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        final ImageResource userImageResource = IconResources.INSTANCE.userSmall();
        fp.add(new HTML(renderer.render(userImageResource)));
        HorizontalPanel namePanel = new HorizontalPanel();
        fp.add(namePanel);
        namePanel.add(new Label(stringMessages.name()+": "));
        namePanel.add(usernameLabel);
        HorizontalPanel emailPanel = new HorizontalPanel();
        fp.add(emailPanel);
        emailPanel.add(new Label(stringMessages.email() + ": "));
        emailPanel.add(emailLabel);
        emailPanel.add(changeEmail);
        accountPanels = new VerticalPanel();
        fp.add(accountPanels);
        decoratorPanel.setWidget(fp);
        this.add(decoratorPanel);
        rolesEditorWrapper = new FlowPanel();
        this.add(rolesEditorWrapper);
        updateRoles(userManagementService, userService, userListDataProvider);
        this.add(permissionsEditor);
        this.add(new Label(stringMessages.allPermissions()));
        allPermissionsList = new ListBox();
        allPermissionsList.setVisibleItemCount(10);
        allPermissionsList.setEnabled(false);
        this.add(allPermissionsList);
        updateUser(user, userManagementService, userService, userListDataProvider);
    }

    /**
     * Assumes {@link #serverRoles} to be up to date; ideally called from an onSuccess callback after
     * retrieving a fresh roles copy from the server
     */
    private void setRolesEditor(UserManagementServiceAsync userManagementService,
            UserService userService, UserListDataProvider userListDataProvider) {
        final StringListEditorComposite result = new StringListEditorComposite(
                user == null ? Collections.<String> emptySet() : user.getStringRoles(), stringMessages,
                com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), serverRoles.keySet(),
                stringMessages.enterRoleName());
        result.addValueChangeHandler(new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                final ArrayList<UUID> newRoleIds = new ArrayList<>();
                final UserDTO selectedUser = UserDetailsView.this.user;
                for (String roleName : event.getValue()) {
                    Role role = serverRoles.get(roleName);
                    if (role != null) {
                        newRoleIds.add(role.getId());
                    }    
                }
                userManagementService.setRolesForUser(selectedUser.getName(), newRoleIds, new MarkedAsyncCallback<SuccessInfo>(
                        new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert(stringMessages.errorUpdatingRoles(selectedUser.getName(), caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (!result.isSuccessful()) {
                                    Window.alert(stringMessages.errorUpdatingRoles(selectedUser.getName(), result.getMessage()));
                                } else {
                                    userListDataProvider.updateDisplays();
                                    if (userService.getCurrentUser().getName().equals(selectedUser.getName())) {
                                        // if the current user's roles changed, update the user object in the user service and notify others
                                        userService.updateUser(/* notify other instances */ true);
                                    }
                                }
                            }
                        }));
            }
        });
        while (rolesEditorWrapper.getWidgetCount() > 0) {
            rolesEditorWrapper.remove(0);
        }
        rolesEditorWrapper.add(result);
    }

    private void updateRoles(final UserManagementServiceAsync userManagementService, UserService userService, UserListDataProvider userListDataProvider) {
        userManagementService.getRoles(new AsyncCallback<ArrayList<Role>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
            @Override
            public void onSuccess(ArrayList<Role> roles) {
                serverRoles.clear();
                for (final Role role : roles) {
                    serverRoles.put(role.getName(), role);
                }
                setRolesEditor(userManagementService, userService, userListDataProvider);
            }
        });
    }

    public void updateUser(final UserDTO user, final UserManagementServiceAsync userManagementService, UserService userService, UserListDataProvider userListDataProvider) {
        this.user = user;
        updateRoles(userManagementService, userService, userListDataProvider);
        accountPanels.clear();
        if (user == null) {
            usernameLabel.setText("");
            emailLabel.setText("");
        } else {
            usernameLabel.setText(user.getName());
            emailLabel.setText(user.getEmail()+(user.isEmailValidated()?" \u2713":""));
            for (AccountDTO a : user.getAccounts()) {
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
                            new ChangePasswordDialog(stringMessages, userManagementService, UserDetailsView.this.user, new DataEntryDialog.DialogCallback<UserData>() {
                                @Override
                                public void ok(UserData userData) {
                                    userManagementService.updateSimpleUserPassword(UserDetailsView.this.user.getName(), /* admin doesn't need to provide old password */ null,
                                            /* resetPasswordSecret */ null, userData.getPassword(), new MarkedAsyncCallback<Void>(
                                            new AsyncCallback<Void>() {
                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    GWT.log(caught.getMessage());
                                                    if (caught instanceof UserManagementException) {
                                                        String message = ((UserManagementException) caught).getMessage();
                                                        if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                                                            Window.alert(stringMessages.passwordDoesNotMeetRequirements());
                                                        } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                                            Window.alert(stringMessages.invalidCredentials());
                                                        } else {
                                                            Window.alert(stringMessages.errorChangingPassword(caught.getMessage()));
                                                        }
                                                    } else {
                                                        Window.alert(stringMessages.errorChangingPassword(caught.getMessage()));
                                                    }
                                                }

                                                @Override
                                                public void onSuccess(Void result) {
                                                    Window.alert(stringMessages.passwordSuccessfullyChanged());
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
            permissionsEditor.setValue(user.getStringPermissions(), /* fireEvents */ false);
            allPermissionsList.clear();
            for (WildcardPermission permission : user.getAllPermissions()) {
                allPermissionsList.addItem(permission.toString());
            }
        }
    }

    public void addUserChangeEventHandler(UserChangeEventHandler handler) {
        this.handlers.add(handler);
    }

    public void removeUserChangeEventHandler(UserChangeEventHandler handler) {
        this.handlers.remove(handler);
    }
}
