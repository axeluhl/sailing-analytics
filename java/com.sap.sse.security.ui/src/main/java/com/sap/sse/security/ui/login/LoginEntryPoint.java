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
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.loginpanel.LoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginEntryPoint implements EntryPoint {

    private final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    
    private final StringMessages stringMessages = GWT.create(StringMessages.class);
    
    private final UserService userService = new UserService();

    @Override
    public void onModuleLoad() {
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                RemoteServiceMappingConstants.userManagementServiceRemotePath);
        
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);
        
        FlowPanel fp = new FlowPanel();
        RootPanel.get().add(new LoginPanel(Resources.INSTANCE.css()));
        
        Label nameLabel = new Label(stringMessages.name()+": ");
        fp.add(nameLabel);
        final TextBox nameText = new TextBox();
        fp.add(nameText);
        Label pwLabel = new Label(stringMessages.password()+": ");
        fp.add(pwLabel);
        final PasswordTextBox pwText = new PasswordTextBox();
        fp.add(pwText);
        SubmitButton submit = new SubmitButton(stringMessages.signIn());
        fp.add(submit);
        FormPanel formPanel = new FormPanel();
        formPanel.addSubmitHandler(new SubmitHandler() {
            @Override
            public void onSubmit(SubmitEvent event) {
                userService.login(nameText.getText(), pwText.getText(), new AsyncCallback<SuccessInfo>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (result.isSuccessful()) {
                            if (!result.getRedirectURL().equals("")) {
                                Window.Location.replace(result.getRedirectURL());
                            } else  {
                                Window.alert(stringMessages.loggedIn());
                            }
                        } else {
                            Window.alert(result.getMessage());
                        }
                    }
                });
            }
        });
        formPanel.add(fp);
        dockPanel.add(formPanel);
    }
}
