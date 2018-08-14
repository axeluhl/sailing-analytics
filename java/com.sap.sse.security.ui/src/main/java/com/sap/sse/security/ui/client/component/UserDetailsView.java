package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
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
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.DefaultPermissions;
import com.sap.sse.security.shared.DefaultRoles;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.UserManagementException;
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
    private final StringListEditorComposite rolesEditor;
    private final StringListEditorComposite permissionsEditor;
    private final VerticalPanel accountPanels;
    private final ListBox allPermissionsList;
    private UserDTO user;

    private final PermissionsForRoleProvider permissionForRoleProvider;

    public UserDetailsView(final UserService userService, UserDTO user, final StringMessages stringMessages,
            final UserListDataProvider userListDataProvider, PermissionsForRoleProvider permissionsForRoleProvider,
            Iterable<Role> additionalRoles, Iterable<Permission> additionalPermissions) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        this.stringMessages = stringMessages;
        this.permissionForRoleProvider = permissionsForRoleProvider;
        this.user = user;
        addStyleName("userDetailsView");
        List<String> defaultRoleNames = new ArrayList<>();
        for (DefaultRoles defaultRole : DefaultRoles.values()) {
            defaultRoleNames.add(defaultRole.getRolename());
        }
        for (Role role : additionalRoles) {
            defaultRoleNames.add(role.getRolename());
        }
        List<String> defaultPermissionNames = new ArrayList<>();
        for (DefaultPermissions defaultPermission : DefaultPermissions.values()) {
            defaultPermissionNames.add(defaultPermission.getStringPermission());
        }
        for (Permission permission : additionalPermissions) {
            defaultPermissionNames.add(permission.getStringPermission());
        }
        rolesEditor = new StringListEditorComposite(user == null ? Collections.<String> emptySet() : user.getRoles(),
                stringMessages, com.sap.sse.gwt.client.IconResources.INSTANCE.removeIcon(), defaultRoleNames,
                stringMessages.enterRoleName());
        rolesEditor.addValueChangeHandler(new ValueChangeHandler<Iterable<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                final Iterable<String> newRoleList = event.getValue();
                final UserDTO selectedUser = UserDetailsView.this.user;
                userManagementService.setRolesForUser(selectedUser.getName(), newRoleList, new MarkedAsyncCallback<SuccessInfo>(
                        new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.errorUpdatingRoles(selectedUser.getName(), caught.getMessage()), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (!result.isSuccessful()) {
                                    Notification.notify(stringMessages.errorUpdatingRoles(selectedUser.getName(), result.getMessage()), NotificationType.ERROR);
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
                                Notification.notify(stringMessages.errorUpdatingPermissions(selectedUser.getName(), caught.getMessage()), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(SuccessInfo result) {
                                if (!result.isSuccessful()) {
                                    Notification.notify(stringMessages.errorUpdatingPermissions(selectedUser.getName(), result.getMessage()), NotificationType.ERROR);
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
        this.add(rolesEditor);
        this.add(permissionsEditor);
        this.add(new Label(stringMessages.allPermissions()));
        allPermissionsList = new ListBox();
        allPermissionsList.setVisibleItemCount(10);
        allPermissionsList.setEnabled(false);
        this.add(allPermissionsList);
        updateUser(user, userManagementService);
    }

    public void updateUser(final UserDTO user, final UserManagementServiceAsync userManagementService) {
        this.user = user;
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
                                                            Notification.notify(stringMessages.passwordDoesNotMeetRequirements(), NotificationType.ERROR);
                                                        } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                                            Notification.notify(stringMessages.invalidCredentials(), NotificationType.ERROR);
                                                        } else {
                                                            Notification.notify(stringMessages.errorChangingPassword(caught.getMessage()), NotificationType.ERROR);
                                                        }
                                                    } else {
                                                        Notification.notify(stringMessages.errorChangingPassword(caught.getMessage()), NotificationType.ERROR);
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
            rolesEditor.setValue(user.getRoles(), /* fireEvents */ false);
            permissionsEditor.setValue(user.getStringPermissions(), /* fireEvents */ false);
            allPermissionsList.clear();
            for (String permission : user.getAllPermissions(permissionForRoleProvider)) {
                allPermissionsList.addItem(permission);
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
