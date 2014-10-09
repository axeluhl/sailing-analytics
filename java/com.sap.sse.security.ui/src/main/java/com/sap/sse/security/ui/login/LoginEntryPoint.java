package com.sap.sse.security.ui.login;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginEntryPoint implements EntryPoint {

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    @Override
    public void onModuleLoad() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);
        
        FlowPanel fp = new FlowPanel();
        
        RootPanel.get().add(new LoginPanel(Resources.INSTANCE.css()));
        
        Label nameLabel = new Label("Name: ");
        fp.add(nameLabel);
        final TextBox nameText = new TextBox();
        nameText.setName("username");
        fp.add(nameText);
        
        Label pwLabel = new Label("Password: ");
        fp.add(pwLabel);
        final PasswordTextBox pwText = new PasswordTextBox();
        pwText.setName("password");
        fp.add(pwText);
        
        SubmitButton submit = new SubmitButton("login");
        fp.add(submit);
        
        FormPanel formPanel = new FormPanel();
        formPanel.addSubmitHandler(new SubmitHandler() {
            
            @Override
            public void onSubmit(SubmitEvent event) {
                userManagementService.login(nameText.getText(), pwText.getText(), new AsyncCallback<SuccessInfo>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (result.isSuccessful() && !result.getMessage().equals("")){
                            Window.Location.replace(result.getMessage());
                        }
                        else if (result.isSuccessful()) {
                            Window.alert("Logged in!");
                        }
                        else {
                            Window.alert(result.getMessage());
                        }
                        
                    }
                });
            }
        });
        formPanel.add(fp);
        dockPanel.add(formPanel);
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }
}
