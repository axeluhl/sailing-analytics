package com.sap.sse.security.ui.client.component;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class UserManagementPanel extends DockPanel {
    
    private List<UserCreationEventHandler> handlers = new ArrayList<>();
    
    private SingleSelectionModel<UserDTO> singleSelectionModel;

    private UserListDataProvider userListDataProvider;
    
    public UserManagementPanel(final UserService userService, final StringMessages stringMessages) {
        final UserManagementServiceAsync userManagementService = userService.getUserManagementService();
        VerticalPanel west = new VerticalPanel();
        singleSelectionModel = new SingleSelectionModel<>();
        Button createButton = new Button(stringMessages.createUser(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new CreateUserDialog(stringMessages, userManagementService, handlers).show();
            }
        });
        west.add(createButton);
        final UserList userList = new UserList();
        userList.setSelectionModel(singleSelectionModel);
        final UserDetailsView userDetailsView = new UserDetailsView(userService, singleSelectionModel.getSelectedObject(), stringMessages);
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
                userDetailsView.updateUser(singleSelectionModel.getSelectedObject());
            }
        });
        userList.setPageSize(20);
        TextBox filterBox = new TextBox();
        userListDataProvider = new UserListDataProvider(userManagementService, filterBox);
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
        addUserCreationEventHandler(new UserCreationEventHandler() {
            @Override
            public void onUserCreation(UserDTO user) {
                userListDataProvider.updateDisplays();
                if (user != null) {
                    singleSelectionModel.setSelected(user, true);
                }
            }
        });
    }
    
    public void addUserCreationEventHandler(UserCreationEventHandler handler){
        this.handlers.add(handler);
    }
    
    public void removeUserCreationEventHandler(UserCreationEventHandler handler){
        this.handlers.remove(handler);
    }
    
    public static interface UserCreationEventHandler extends EventHandler {
        
        void onUserCreation(UserDTO user);
    }
}
