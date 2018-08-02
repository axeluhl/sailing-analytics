package com.sap.sse.security.ui.client.usermanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlListListDataProvider;
import com.sap.sse.security.ui.client.component.CreateUserDialog;
import com.sap.sse.security.ui.client.component.EditAccessControlListDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserManagementPanel<TR extends CellTableWithCheckboxResources> extends DockPanel {
    
    private final List<UserCreatedEventHandler> userCreatedHandlers = new ArrayList<>();
    
    private final List<UserDeletedEventHandler> userDeletedHandlers = new ArrayList<>();
    
    private final SingleSelectionModel<AccessControlListAnnotation> aclSingleSelectionModel;
    private final AccessControlListListDataProvider aclListDataProvider;
    
    private final UserTableWrapper<RefreshableMultiSelectionModel<UserDTO>, TR> userList;
    private final RefreshableMultiSelectionModel<UserDTO> userSelectionModel;

    public UserManagementPanel(final UserService userService, final StringMessages stringMessages,
            ErrorReporter errorReporter, TR tableResources) {
        this(userService, stringMessages, Collections.<Permission>emptySet(), errorReporter, tableResources);
    }
    
    public UserManagementPanel(final UserService userService, final StringMessages stringMessages,
            Iterable<Permission> additionalPermissions, ErrorReporter errorReporter, TR tableResources) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        VerticalPanel west = new VerticalPanel();
        HorizontalPanel buttonPanel = new HorizontalPanel();
        west.add(buttonPanel);
        buttonPanel.add(new Button(stringMessages.refresh(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateUsersAndACLs();
            }
        }));
        Button createButton = new Button(stringMessages.createUser(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateUserDialog(stringMessages, userManagementService, userCreatedHandlers).show();
            }
        });
        buttonPanel.add(createButton);
        final Button deleteButton = new Button(stringMessages.remove(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                assert userSelectionModel.getSelectedSet().size() == 1;
                final UserDTO userToDelete = userSelectionModel.getSelectedSet().iterator().next();
                final String username = userToDelete.getName();
                if (Window.confirm(stringMessages.doYouReallyWantToDeleteUser(username))) {
                    userManagementService.deleteUser(username, new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            for (UserDeletedEventHandler userDeletedHandler : userDeletedHandlers) {
                                userDeletedHandler.onUserDeleted(userToDelete);
                            }
                            Notification.notify(result.getMessage(), NotificationType.SUCCESS);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.errorDeletingUser(username, caught.getMessage()));
                        }
                    });
                }
            }
        });
        // TODO: find the right place for the acl controls
        Button editACLButton = new Button(stringMessages.editACL(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new EditAccessControlListDialog(stringMessages, userManagementService, aclListDataProvider, aclSingleSelectionModel.getSelectedObject()).show();
            }
        });
        editACLButton.setEnabled(false);
        buttonPanel.add(editACLButton);
        aclSingleSelectionModel = new SingleSelectionModel<>();
        aclSingleSelectionModel.addSelectionChangeHandler(e->editACLButton.setEnabled(aclSingleSelectionModel.getSelectedObject() != null));
        final CellTable<AccessControlListAnnotation> aclTable = new CellTable<>();
        TextColumn<AccessControlListAnnotation> idColumn = new TextColumn<AccessControlListAnnotation>() {
            @Override
            public String getValue(AccessControlListAnnotation acl) {
                return acl.getIdOfAnnotatedObjectAsString();
            }
        };
        TextColumn<AccessControlListAnnotation> displayNameColumn = new TextColumn<AccessControlListAnnotation>() {
            @Override
            public String getValue(AccessControlListAnnotation acl) {
                return acl.getDisplayNameOfAnnotatedObject()==null?"":acl.getDisplayNameOfAnnotatedObject();
            }
        };
        aclTable.addColumn(idColumn, stringMessages.id());
        aclTable.addColumn(displayNameColumn, stringMessages.displayName());
        aclTable.setSelectionModel(aclSingleSelectionModel);
        aclListDataProvider = new AccessControlListListDataProvider(userManagementService);
        aclTable.setPageSize(20);
        aclListDataProvider.addDataDisplay(aclTable);
        SimplePager aclPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        aclPager.setDisplay(aclTable);
        ScrollPanel aclPanel = new ScrollPanel(aclTable);
        west.add(aclPager);
        west.add(aclPanel);
        
        userList = new UserTableWrapper<>(
                userService, additionalPermissions, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true, tableResources);
        userSelectionModel = userList.getSelectionModel();
        buttonPanel.add(deleteButton);
        deleteButton.setEnabled(userSelectionModel.getSelectedSet().size() >= 1);
        userSelectionModel.addSelectionChangeHandler(e->{
            deleteButton.setText(stringMessages.remove()+" ("+userSelectionModel.getSelectedSet().size()+")");
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
        updateUsersAndACLs();
    }
    
    public void updateUsersAndACLs() {
        userList.refreshUserList((Callback<Iterable<UserDTO>, Throwable>) null);
        aclListDataProvider.updateDisplays();
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
