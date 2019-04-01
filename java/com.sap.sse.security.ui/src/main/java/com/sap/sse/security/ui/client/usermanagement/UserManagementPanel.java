package com.sap.sse.security.ui.client.usermanagement;

import static com.sap.sse.security.shared.impl.SecuredSecurityTypes.USER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.CreateUserDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.permissions.WildcardPermissionPanel;
import com.sap.sse.security.ui.client.usermanagement.roles.UserRoleDefinitionPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class UserManagementPanel<TR extends CellTableWithCheckboxResources> extends DockPanel {
    
    private final List<UserCreatedEventHandler> userCreatedHandlers = new ArrayList<>();
    private final List<UserDeletedEventHandler> userDeletedHandlers = new ArrayList<>();
    
    private final UserTableWrapper<RefreshableMultiSelectionModel<UserDTO>, TR> userList;
    private final RefreshableMultiSelectionModel<UserDTO> userSelectionModel;
    private final TextBox userNameTextbox;

    public UserManagementPanel(final UserService userService, final StringMessages stringMessages,
            ErrorReporter errorReporter, TR tableResources) {
        this(userService, stringMessages, Collections.<HasPermissions>emptySet(), errorReporter, tableResources);
    }
    
    public UserManagementPanel(final UserService userService, final StringMessages stringMessages,
            Iterable<HasPermissions> additionalPermissions, ErrorReporter errorReporter, TR tableResources) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        final VerticalPanel west = new VerticalPanel();
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, USER);
        west.add(buttonPanel);
        buttonPanel.addUnsecuredAction(stringMessages.refresh(), this::updateUsers);
        buttonPanel.addCreateActionWithoutServerCreateObjectPermissionCheck(stringMessages.createUser(),
                () -> new CreateUserDialog(stringMessages, userManagementService, userCreatedHandlers, userService)
                        .show());

        userNameTextbox = buttonPanel.addUnsecuredTextBox(stringMessages.username());
        buttonPanel.addUnsecuredAction(stringMessages.editRolesAndPermissionsForUser(""),
                () -> showRolesAndPermissionsEditDialog(userService, tableResources, errorReporter));

        userList = new UserTableWrapper<>(userService, additionalPermissions, stringMessages, errorReporter,
                /* multiSelection */ true, /* enablePager */ true, tableResources);
        userSelectionModel = userList.getSelectionModel();
        final Button deleteButton = buttonPanel.addRemoveAction(stringMessages.remove(), () -> {
                assert userSelectionModel.getSelectedSet().size() > 0;
                final Set<UserDTO> usersToDelete = new HashSet<>();
                final Set<String> usernamesToDelete = new HashSet<>();
                for (UserDTO userToDelete : userSelectionModel.getSelectedSet()) {
                    usersToDelete.add(userToDelete);
                    usernamesToDelete.add(userToDelete.getName());
                }
                if (Window.confirm(usernamesToDelete.size() == 1
                        ? stringMessages.doYouReallyWantToDeleteUser(usernamesToDelete.iterator().next())
                        : stringMessages.doYouReallyWantToDeleteNUsers(usernamesToDelete.size()))) {
                    userManagementService.deleteUsers(usernamesToDelete, new AsyncCallback<Set<SuccessInfo>>() {
                        @Override
                        public void onSuccess(Set<SuccessInfo> result) {
                            for (UserDTO userToDelete : usersToDelete) {
                                for (UserDeletedEventHandler userDeletedHandler : userDeletedHandlers) {
                                    userDeletedHandler.onUserDeleted(userToDelete);
                                }
                            }
                            for (SuccessInfo successInfo : result) {
                                Notification.notify(successInfo.getMessage(), NotificationType.SUCCESS);
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorDeletingUser(usernamesToDelete.iterator().next(),
                                caught.getMessage()));
                        }
                    });
                }
        });
        deleteButton.setEnabled(userSelectionModel.getSelectedSet().size() >= 1);
        userSelectionModel.addSelectionChangeHandler(event -> {
            deleteButton.setText(stringMessages.remove() + " (" + userSelectionModel.getSelectedSet().size() + ")");
            deleteButton.setEnabled(userSelectionModel.getSelectedSet().size() >= 1);
        });

        ScrollPanel scrollPanel = new ScrollPanel(userList.asWidget());
        LabeledAbstractFilterablePanel<UserDTO> filterBox = userList.getFilterField();
        filterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());
        west.add(filterBox);
        west.add(scrollPanel);
        add(west, DockPanel.WEST);
        addUserCreatedEventHandler(new UserCreatedEventHandler() {
            @Override
            public void onUserCreated(UserDTO user) {
                if (user != null) {
                    userList.getFilterField().add(user);
                    userSelectionModel.clear();
                    userSelectionModel.setSelected(user, true);
                }
            }
        });
        addUserDeletedEventHandler(new UserDeletedEventHandler() {
            @Override
            public void onUserDeleted(UserDTO user) {
                userList.getFilterField().remove(user);
            }
        });
        updateUsers();

        final HorizontalPanel detailsPanel = new HorizontalPanel();

        // add details panel for user roles
        final UserRoleDefinitionPanel userRoleDefinitionPanel = new UserRoleDefinitionPanel(userService, stringMessages,
                errorReporter,
                tableResources, userList.getSelectionModel(), () -> updateUsers());
        detailsPanel.add(userRoleDefinitionPanel);

        // add details panel for user permissions
        final WildcardPermissionPanel userPermissionPanel = new WildcardPermissionPanel(userService, stringMessages,
                errorReporter, tableResources, userList.getSelectionModel(), () -> updateUsers());
        detailsPanel.add(userPermissionPanel);

        west.add(detailsPanel);
    }
    
    /** shows the edit dialog */
    private void showRolesAndPermissionsEditDialog(UserService userService,
            final CellTableWithCheckboxResources tableResources, final ErrorReporter errorReporter) {
        new EditUserRolesAndPermissionsDialog(userNameTextbox.getText(), userService, errorReporter, tableResources,
                new DialogCallback<Void>() {
                    @Override
                    public void ok(Void editedObject) {
                        updateUsers();
                    }

                    @Override
                    public void cancel() {
                        updateUsers();
                    }
                }).show();
    }

    public void updateUsers() {
        userList.refreshUserList((Callback<Iterable<UserDTO>, Throwable>) null);
    }

    public void addUserCreatedEventHandler(UserCreatedEventHandler handler){
        this.userCreatedHandlers.add(handler);
    }
    
    public void removeUserCreatedEventHandler(UserCreatedEventHandler handler){
        this.userCreatedHandlers.remove(handler);
    }
    
    public static interface UserCreatedEventHandler extends EventHandler {
        void onUserCreated(UserDTO user);
    }

    public void addUserDeletedEventHandler(UserDeletedEventHandler handler){
        this.userDeletedHandlers.add(handler);
    }
    
    public void removeUserDeletedEventHandler(UserDeletedEventHandler handler){
        this.userDeletedHandlers.remove(handler);
    }
    
    public static interface UserDeletedEventHandler extends EventHandler {
        void onUserDeleted(UserDTO user);
    }
}
