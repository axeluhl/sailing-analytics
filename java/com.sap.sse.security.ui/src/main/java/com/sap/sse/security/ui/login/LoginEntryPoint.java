package com.sap.sse.security.ui.login;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginEntryPoint implements EntryPoint {

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    @Override
    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        rootPanel.add(dockPanel);
        
        FlowPanel fp = new FlowPanel();
        Label nameLabel = new Label("Name: ");
        fp.add(nameLabel);
        final TextBox nameText = new TextBox();
        nameText.setName("username");
        fp.add(nameText);
        
        Label pwLabel = new Label("Password: ");
        fp.add(pwLabel);
        final TextBox pwText = new TextBox();
        pwText.setName("password");
        fp.add(pwText);
        
        SubmitButton submit = new SubmitButton("login");
        submit.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                userManagementService.login(nameText.getText(), pwText.getText(), new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        if (result != null && !result.equals("")){
                            Window.Location.replace(result);
                        }
                        else if (result != null && result.equals("")) {
                            Window.alert("Logged in!");
                        }
                        else {
                            Window.alert("Invalid Credentials!");
                        }
                        
                    }
                });
            }
        });
        fp.add(submit);
        
        dockPanel.add(fp);
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }
}
