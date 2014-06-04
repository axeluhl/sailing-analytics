package com.sap.sse.security.ui.loginpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.security.ui.client.UserChangeEventHandler;
import com.sap.sse.security.ui.client.UserManagementImageResources;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginPanel extends FlowPanel {

    private UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);
    
    private static List<UserStatusEventHandler> handlers = new ArrayList<>();

    private FormPanel loginPanel;
    private FlowPanel userPanel;
    private SimplePanel infoPanel;

    private boolean expanded = false;
    private static UserDTO currentUser = null;

    private Label loginTitle1;
    private Anchor loginLink;
    private Label loginTitle2;

    private TextBox name;

    private PasswordTextBox password;

    private FlowPanel wrapperPanel;

    public LoginPanel() {
        registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
        
        StylesheetResources.INSTANCE.css().ensureInjected();
        
        wrapperPanel = new FlowPanel();
        wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanel());
        wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelCollapsed());
        FlowPanel titlePanel = new FlowPanel();
        titlePanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelTitlePanel());
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
        final ImageResource userImageResource = UserManagementImageResources.INSTANCE.userIcon();
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        titlePanel.add(new HTML(renderer.render(userImageResource)));
        titlePanel.add(loginTitle1);
        titlePanel.add(loginLink);
        titlePanel.add(loginTitle2);
        wrapperPanel.add(titlePanel);
        infoPanel = new SimplePanel();
        infoPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelInfoPanel());
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
        Anchor register = new Anchor("Register", EntryPointLinkFactory.createRegistrationLink(new HashMap<String, String>()));
        formContent.add(register);

        loginPanel.setWidget(formContent);
    }

    private void initUserContent() {
        userPanel = new FlowPanel();
    }

    private void updateUserContent() {
        if (currentUser != null) {
            userPanel.clear();
//            Label name = new Label(currentUser.getName());
//            userPanel.add(name);
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
        wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelCollapsed());
        wrapperPanel.removeStyleName(StylesheetResources.INSTANCE.css().loginPanelExpanded());
        for (UserStatusEventHandler handler : handlers){
            handler.onUserStatusChange(currentUser);
        }
    }

    private void toggleLoginPanel() {
        if (expanded) {
            expanded = false;
            wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelCollapsed());
            wrapperPanel.removeStyleName(StylesheetResources.INSTANCE.css().loginPanelExpanded());
        } else {
            expanded = true;
            wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelExpanded());
            wrapperPanel.removeStyleName(StylesheetResources.INSTANCE.css().loginPanelCollapsed());
        }
    }
    
    protected void registerASyncService(ServiceDefTarget serviceToRegister, String servicePath) {
        String moduleBaseURL = GWT.getModuleBaseURL();
        String baseURL = moduleBaseURL.substring(0, moduleBaseURL.indexOf('/', moduleBaseURL.indexOf(':')+3)+1);
        serviceToRegister.setServiceEntryPoint(baseURL + "security/ui/" + servicePath);
    }
    
    public static UserDTO getCurrentUser(){
        return currentUser;
    }
    
    public static void addUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.add(handler);
    }

    public static void removeUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.remove(handler);
    }
}
