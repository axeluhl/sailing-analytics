package com.sap.sse.security.ui.usermanagement;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.security.ui.client.component.UserList;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class UserManagementEntryPoint implements EntryPoint {

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    @Override
    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        rootPanel.add(dockPanel);
        
        VerticalPanel vp = new VerticalPanel();
        final TextBox textbox = new TextBox();
        textbox.setText("Not logged in");
        vp.add(textbox);
        final Anchor aLogin = new Anchor(new SafeHtmlBuilder().appendEscaped("logout").toSafeHtml(), "/security/ui/Login.html?gwt.codesvr=127.0.0.1:9997");
        aLogin.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                userManagementService.logout(new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(Void result) {
                    }
                });
            }
        });
        vp.add(aLogin);
        final UserList userList = new UserList();
        vp.add(userList);
        dockPanel.add(vp);
        
        
        userManagementService.getUserList(new AsyncCallback<Collection<UserDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not load users.");
            }

            @Override
            public void onSuccess(Collection<UserDTO> result) {
                userList.setRowData(new ArrayList<UserDTO>(result));
            }
        });
        userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(UserDTO result) {
                if (result == null || result.getName() == null){
                    Window.alert("Error receiving user. Please login!");
                }
                else {
                    textbox.setText("Logged in as: " + result.getName());
                }
            }
        });
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }
}
