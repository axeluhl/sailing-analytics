package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.security.shared.AbstractRole;
import com.sap.sse.security.shared.Permission;
import com.sap.sse.security.shared.PermissionsForRoleProvider;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.AccessControlListDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.TenantDTO;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserGroupDTO;

public class UserManagementPanel extends DockPanel {
    
    private List<UserCreatedEventHandler> userCreatedHandlers = new ArrayList<>();
    
    private List<UserDeletedEventHandler> userDeletedHandlers = new ArrayList<>();
    
    private SingleSelectionModel<AccessControlListDTO> aclSingleSelectionModel;
    private AccessControlListListDataProvider aclListDataProvider;
    
    private SingleSelectionModel<UserGroupDTO> userGroupSingleSelectionModel;
    private UserGroupListDataProvider userGroupListDataProvider;
    
    private SingleSelectionModel<UserDTO> singleSelectionModel;

    private UserListDataProvider userListDataProvider;
    
    public UserManagementPanel(final UserService userService, final StringMessages stringMessages, PermissionsForRoleProvider permissionsForRoleProvider) {
        this(userService, stringMessages, permissionsForRoleProvider, Collections.<AbstractRole>emptySet(), Collections.<Permission>emptySet());
    }
    
    public UserManagementPanel(final UserService userService, final StringMessages stringMessages, PermissionsForRoleProvider permissionsForRoleProvider,
            Iterable<AbstractRole> additionalRoles, Iterable<Permission> additionalPermissions) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        VerticalPanel west = new VerticalPanel();
        HorizontalPanel buttonPanel = new HorizontalPanel();
        west.add(buttonPanel);
        singleSelectionModel = new SingleSelectionModel<>();
        buttonPanel.add(new Button(stringMessages.refresh(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                userListDataProvider.updateDisplays();
            }
        }));
        Button createButton = new Button(stringMessages.createUser(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateUserDialog(stringMessages, userManagementService, userCreatedHandlers).show();
            }
        });
        buttonPanel.add(createButton);
        final Button deleteButton = new Button(stringMessages.deleteUser(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final UserDTO userToDelete = singleSelectionModel.getSelectedObject();
                final String username = userToDelete.getName();
                if (Window.confirm(stringMessages.doYouReallyWantToDeleteUser(username))) {
                    userManagementService.deleteUser(username, new AsyncCallback<SuccessInfo>() {
                        @Override
                        public void onSuccess(SuccessInfo result) {
                            for (UserDeletedEventHandler userDeletedHandler : userDeletedHandlers) {
                                userDeletedHandler.onUserDeleted(userToDelete);
                            }
                            Window.alert(result.getMessage());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(stringMessages.errorDeletingUser());
                        }
                    });
                }
            }
        });
        buttonPanel.add(deleteButton);
        deleteButton.setEnabled(singleSelectionModel.getSelectedObject() != null);
        
        // TODO: find the right place for the acl controls
        Button editACLButton = new Button("Edit ACL", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new EditAccessControlListDialog(stringMessages, userManagementService, aclListDataProvider, aclSingleSelectionModel.getSelectedObject()).show();
            }
        });
        buttonPanel.add(editACLButton);
        aclSingleSelectionModel = new SingleSelectionModel<>();
        final CellList<AccessControlListDTO> aclList = new CellList<AccessControlListDTO>(new AbstractCell<AccessControlListDTO>() {
            @Override
            public void render(Context context, AccessControlListDTO value, SafeHtmlBuilder sb) {
                if (value == null) {
                    return;
                }
                sb.appendHtmlConstant("<table>");
                sb.appendHtmlConstant("<tr>");
                sb.appendHtmlConstant("<td>");
                sb.appendEscaped(value.getId());
                sb.appendHtmlConstant("</td>");
                for (Map.Entry<UserGroupDTO, Set<String>> entry : value.getUserGroupPermissionMap().entrySet()) {
                    sb.appendHtmlConstant("<td>");
                    String concated = entry.getKey().getName() + ": ";
                    concated += String.join(", ", entry.getValue());
                    sb.appendEscaped(concated);
                    sb.appendHtmlConstant("</td>");
                }
                sb.appendHtmlConstant("</tr>");
                sb.appendHtmlConstant("</table>");
            }
            
        });
        aclList.setSelectionModel(aclSingleSelectionModel);
        aclListDataProvider = new AccessControlListListDataProvider(userManagementService);
        aclList.setPageSize(20);
        aclListDataProvider.addDataDisplay(aclList);
        SimplePager aclPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        aclPager.setDisplay(aclList);
        ScrollPanel aclPanel = new ScrollPanel(aclList);
        west.add(aclPager);
        west.add(aclPanel);
               
        // TODO: find the right place for the user group controls
        Button createUserGroupButton = new Button("Create user group", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateUserGroupDialog(stringMessages, userManagementService, userGroupListDataProvider).show();
            }
        });
        buttonPanel.add(createUserGroupButton);
        Button addUserToUserGroupButton = new Button("Add user to user group", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroupDTO userGroup = userGroupSingleSelectionModel.getSelectedObject();
                UserDTO user = singleSelectionModel.getSelectedObject();
                if (userGroup != null && user != null) {
                    userManagementService.addUserToUserGroup(user.getName(), userGroup.getName(), new AsyncCallback<UserGroupDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error adding user to user group.");
                        }
                        @Override
                        public void onSuccess(UserGroupDTO result) {
                            userGroupListDataProvider.updateDisplays();
                        }
                    });
                }
            }
        });
        buttonPanel.add(addUserToUserGroupButton);
        Button removeUserFromUserGroupButton = new Button("Remove user from user group", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UserGroupDTO userGroup = userGroupSingleSelectionModel.getSelectedObject();
                UserDTO user = singleSelectionModel.getSelectedObject();
                if (userGroup != null && user != null) {
                    userManagementService.removeUserFromUserGroup(user.getName(), userGroup.getName(), new AsyncCallback<UserGroupDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error removing user from user group.");
                        }
                        @Override
                        public void onSuccess(UserGroupDTO result) {
                            userGroupListDataProvider.updateDisplays();
                        }
                    });
                }
            }
        });
        buttonPanel.add(removeUserFromUserGroupButton);
        userGroupSingleSelectionModel = new SingleSelectionModel<>();
        final CellList<UserGroupDTO> userGroupList = new CellList<UserGroupDTO>(new AbstractCell<UserGroupDTO>() {
            @Override
            public void render(Context context, UserGroupDTO value, SafeHtmlBuilder sb) {
                if (value == null) {
                    return;
                }
                sb.appendHtmlConstant("<table>");
                sb.appendHtmlConstant("<tr>");
                sb.appendHtmlConstant("<td>");
                sb.appendEscaped(value.getName());
                sb.appendHtmlConstant("</td>");
                sb.appendHtmlConstant("<td>");
                sb.appendEscaped(value.getOwner() == null? "" : value.getOwner().getOwner());
                sb.appendHtmlConstant("</td>");
                sb.appendHtmlConstant("<td>");
                String concated = "";
                for (String username : value.getUsernames()) {
                    concated += username + ", ";
                }
                sb.appendEscaped(concated);
                sb.appendHtmlConstant("</td>");
                sb.appendHtmlConstant("<td>");
                sb.appendEscaped((value instanceof TenantDTO) ? "Tenant" : "User Group");
                sb.appendHtmlConstant("</td>");
                sb.appendHtmlConstant("</tr>");
                sb.appendHtmlConstant("</table>");
            }
            
        });
        userGroupList.setSelectionModel(userGroupSingleSelectionModel);
        userGroupListDataProvider = new UserGroupListDataProvider(userManagementService);
        userGroupList.setPageSize(20);
        userGroupListDataProvider.addDataDisplay(userGroupList);
        SimplePager userGroupPager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        userGroupPager.setDisplay(userGroupList);
        ScrollPanel userGroupPanel = new ScrollPanel(userGroupList);
        west.add(userGroupPager);
        west.add(userGroupPanel);
        
        final UserList userList = new UserList();
        userList.setSelectionModel(singleSelectionModel);
        TextBox filterBox = new TextBox();
        userListDataProvider = new UserListDataProvider(userManagementService, filterBox);
        final UserDetailsView userDetailsView = new UserDetailsView(userService,
                singleSelectionModel.getSelectedObject(), stringMessages, userListDataProvider,
                permissionsForRoleProvider, additionalRoles, additionalPermissions);
        add(userDetailsView, DockPanel.CENTER);
        userDetailsView.addUserChangeEventHandler(new UserChangeEventHandler() {
            @Override
            public void onUserChange(UserDTO user) {
                userListDataProvider.updateDisplays();
            }
        });
        singleSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                userDetailsView.updateUser(singleSelectionModel.getSelectedObject(), userManagementService);
                deleteButton.setEnabled(singleSelectionModel.getSelectedObject() != null);
            }
        });
        userList.setPageSize(20);
        userListDataProvider.addDataDisplay(userList);
        SimplePager pager = new SimplePager(TextLocation.CENTER, false, /* fast forward step size */ 50, true);
        pager.setDisplay(userList);
        ScrollPanel scrollPanel = new ScrollPanel(userList);
        filterBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                userListDataProvider.updateDisplays();
            }
        });
        filterBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                userListDataProvider.updateDisplays();
            }
        });
        filterBox.getElement().setPropertyString("placeholder", stringMessages.filterUsers());
        west.add(filterBox);
        west.add(pager);
        west.add(scrollPanel);
        add(west, DockPanel.WEST);
        addUserCreatedEventHandler(new UserCreatedEventHandler() {
            @Override
            public void onUserCreated(UserDTO user) {
                userListDataProvider.updateDisplays();
                if (user != null) {
                    for (UserDTO u : userList.getVisibleItems()) {
                        if (u.getName().equals(user.getName())) {
                            singleSelectionModel.setSelected(user, true);
                        }
                    }
                }
            }
        });
        addUserDeletedEventHandler(new UserDeletedEventHandler() {
            @Override
            public void onUserDeleted(UserDTO user) {
                userListDataProvider.updateDisplays();
            }
        });
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
