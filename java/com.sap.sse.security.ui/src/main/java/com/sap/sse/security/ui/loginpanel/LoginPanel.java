package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginPanel extends FlowPanel {

    private UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    private FormPanel loginPanel;
    private FlowPanel userPanel;
    private SimplePanel infoPanel;

    private boolean expanded = false;
    private UserDTO currentUser = null;

    private Label loginTitle1;
    private Anchor loginLink;
    private Label loginTitle2;

    private TextBox name;

    private PasswordTextBox password;

    private FlowPanel wrapperPanel;

    public LoginPanel() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        wrapperPanel = new FlowPanel();
        wrapperPanel.addStyleName("loginPanel");
        wrapperPanel.addStyleName("loginPanel-collapsed");
        loginTitle1 = new Label("");
        loginTitle2 = new Label("");
        loginLink = new Anchor("Login");
        loginLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (currentUser != null){
                    userManagementService.logout(new AsyncCallback<SuccessInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(SuccessInfo result) {
                            Window.alert(result.getMessage());
                            currentUser = null;
                            updateStatus();
                        }
                    });
                }
                else {
                    toggleLoginPanel();
                }
            }
        });
        wrapperPanel.add(loginTitle1);
        wrapperPanel.add(loginLink);
        wrapperPanel.add(loginTitle2);
        infoPanel = new SimplePanel();
        infoPanel.addStyleName("loginPanel-infoPanel");
        initLoginContent();
        initUserContent();
        infoPanel.setWidget(loginPanel);
        wrapperPanel.add(infoPanel);
        add(wrapperPanel);
        updateUser();
    }

    private void initLoginContent() {
        loginPanel = new FormPanel();
        loginPanel.addSubmitHandler(new SubmitHandler() {
            
            @Override
            public void onSubmit(SubmitEvent event) {
                userManagementService.login(name.getText(), password.getText(), new AsyncCallback<SuccessInfo>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (result.isSuccessful()) {
                            updateUser();
                        } else {
                            Window.alert(result.getMessage());
                        }
                    }
                });
            }
        });

        FlowPanel formContent = new FlowPanel();

        Label nameLabel = new Label("Name: ");
        formContent.add(nameLabel);
        name = new TextBox();
        formContent.add(name);
        Label passwordLabel = new Label("Password: ");
        formContent.add(passwordLabel);
        password = new PasswordTextBox();
        formContent.add(password);
        SubmitButton submit = new SubmitButton("Login");
        formContent.add(submit);

        loginPanel.setWidget(formContent);
    }

    private void initUserContent() {
        userPanel = new FlowPanel();
    }

    private void updateUserContent() {
        if (currentUser != null) {
            userPanel.clear();
            Label name = new Label(currentUser.getName());
            userPanel.add(name);
        }
    }

    private void updateUser() {
        userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {

            @Override
            public void onSuccess(UserDTO result) {
                currentUser = result;
                updateStatus();
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        });
    }

    private void updateStatus() {
        if (currentUser != null) {
            infoPanel.setWidget(userPanel);
            loginTitle1.setText("Welcome, " + currentUser.getName() + "! (");
            loginLink.setText("Logout");
            loginTitle2.setText(")");
            updateUserContent();
        } else {
            infoPanel.setWidget(loginPanel);
            loginTitle1.setText("");
            loginLink.setText("Login");
            loginTitle2.setText("");
        }
        expanded = false;
        wrapperPanel.addStyleName("loginPanel-collapsed");
        wrapperPanel.removeStyleName("loginPanel-expanded");
    }

    private void toggleLoginPanel() {
        if (expanded) {
            expanded = false;
            wrapperPanel.addStyleName("loginPanel-collapsed");
            wrapperPanel.removeStyleName("loginPanel-expanded");
        } else {
            expanded = true;
            wrapperPanel.addStyleName("loginPanel-expanded");
            wrapperPanel.removeStyleName("loginPanel-collapsed");
        }
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length()-2)+1);
        
        serviceToRegister.setServiceEntryPoint(baseURL + servicePath);
    }
}
