package com.sap.sse.security.ui.usermanagement;

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.CreateUserPanel;
import com.sap.sse.security.ui.client.component.CreateUserPanel.UserCreationEventHandler;
import com.sap.sse.security.ui.client.component.SettingsPanel;
import com.sap.sse.security.ui.client.component.UserDetailsView;
import com.sap.sse.security.ui.client.component.UserList;
import com.sap.sse.security.ui.client.component.UserListDataProvider;
import com.sap.sse.security.ui.loginpanel.EntryPointLinkFactory;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class UserManagementEntryPoint implements EntryPoint {

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    
    private SimpleLayoutPanel center;
    
    private TextBox filterBox = new TextBox();
    
    ProvidesKey<UserDTO> keyProvider = new ProvidesKey<UserDTO>() {
        public Object getKey(UserDTO item) {
            // Always do a null check.
            return (item == null) ? null : item.getName();
        }
    };
    
    private UserListDataProvider userListDataProvider = new UserListDataProvider(userManagementService, filterBox, keyProvider);
    
    SingleSelectionModel<UserDTO>  singleSelectionModel = new SingleSelectionModel<>(keyProvider);
    
    @Override
    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        center = new SimpleLayoutPanel();
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);
        
        HorizontalPanel hp = new HorizontalPanel();
        LoginPanel.addUserStatusEventHandler(new UserStatusEventHandler() {
            
            @Override
            public void onUserStatusChange(UserDTO user) {
                if (user == null){
                    userManagementService.logout(new AsyncCallback<SuccessInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                        }

                        @Override
                        public void onSuccess(SuccessInfo result) {
                        }
                    });
                    Window.Location.replace(EntryPointLinkFactory.createLoginLink(new HashMap<String, String>()));
                }
            }
        });
        Button createButton = new Button("Create User", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                CreateUserPanel createUserPanel = new CreateUserPanel(userManagementService);
                createUserPanel.addUserCreationEventHandler(new UserCreationEventHandler() {
                    
                    @Override
                    public void onUserCreation(UserDTO user) {
                        userListDataProvider.updateDisplays();
                        if (user != null){
                            singleSelectionModel.setSelected(user, true);
                        }
                    }
                });
                center.setWidget(createUserPanel);
            }
        });
        hp.add(createButton);
        
        Button settingsButton = new Button("Settings", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                center.setWidget(new SettingsPanel(userManagementService));
            }
        });
        hp.add(settingsButton);
        
        dockPanel.addNorth(hp, 50);
        
        final UserList userList = new UserList(keyProvider);
        userList.setSelectionModel(singleSelectionModel);
        singleSelectionModel.addSelectionChangeHandler(new Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final UserDetailsView userDetailsView = new UserDetailsView(userManagementService, singleSelectionModel.getSelectedObject());
                userDetailsView.addUserChangeEventHandler(new UserChangeEventHandler() {
                    
                    @Override
                    public void onUserChange(UserDTO user) {
                        userListDataProvider.updateDisplays();
                    }
                });
                center.setWidget(userDetailsView);
            }
        });
        userList.setPageSize(4);
        userListDataProvider.addDataDisplay(userList);
        SimplePager pager = new SimplePager(TextLocation.CENTER, false, 10, true);
        pager.setDisplay(userList);
        ScrollPanel scrollPanel = new ScrollPanel(userList);
        VerticalPanel vp = new VerticalPanel();
        filterBox.addChangeHandler(new ChangeHandler() {
            
            @Override
            public void onChange(ChangeEvent event) {
                userListDataProvider.updateDisplays();
            }
        });
        filterBox.getElement().setPropertyString("placeholder", "Filter users...");
        vp.add(filterBox);
        vp.add(pager);
        vp.add(scrollPanel);
        dockPanel.addWest(vp, 350);
        
        dockPanel.add(center);
        CreateUserPanel createUserPanel = new CreateUserPanel(userManagementService);
        createUserPanel.addUserCreationEventHandler(new UserCreationEventHandler() {
            
            @Override
            public void onUserCreation(UserDTO user) {
                userListDataProvider.updateDisplays();
                if (user != null){
                    singleSelectionModel.setSelected(user, true);
                }
            }
        });
        center.setWidget(createUserPanel);
        RootPanel.get().add(new LoginPanel());
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }
}
