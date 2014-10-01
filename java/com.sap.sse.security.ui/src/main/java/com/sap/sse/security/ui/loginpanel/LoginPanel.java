package com.sap.sse.security.ui.loginpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
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
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.UserManagementImageResources;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.oauth.client.component.OAuthLoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginPanel extends FlowPanel implements UserStatusEventHandler {

    private static Storage localStorage = Storage.getLocalStorageIfSupported();

    private static final UserManagementServiceAsync userManagementService = GWT.create(UserManagementService.class);

    static {
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService, "service/usermanagement");
    }

    private static final List<UserStatusEventHandler> handlers = new ArrayList<>();

    private FormPanel loginPanel;
    private FlowPanel userPanel;
    private SimplePanel infoPanel;

    private static int counter = 0;

    private boolean expanded = false;
    private static UserDTO currentUser = null;

    private Label loginTitle;
    private Anchor loginLink;

    private TextBox name;

    private PasswordTextBox password;

    private FlowPanel wrapperPanel;

    private final int id;

    public LoginPanel() {
        id = counter++;
        StylesheetResources.INSTANCE.css().ensureInjected();

        wrapperPanel = new FlowPanel();
        wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanel());
        wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelCollapsed());
        FocusPanel titleFocus = new FocusPanel();
        FlowPanel titlePanel = new FlowPanel();
        titleFocus.setWidget(titlePanel);
        titlePanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelTitlePanel());
        loginTitle = new Label("");
        loginLink = new Anchor("Login");
        final ImageResource userImageResource = UserManagementImageResources.INSTANCE.userIcon();
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        titlePanel.add(new HTML(renderer.render(userImageResource)));
        titlePanel.add(loginTitle);
        titlePanel.add(loginLink);
        wrapperPanel.add(titleFocus);
        titleFocus.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                toggleLoginPanel();
            }
        });
        infoPanel = new SimplePanel();
        infoPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelInfoPanel());
        initLoginContent();
        initUserContent();
        infoPanel.setWidget(loginPanel);
        wrapperPanel.add(infoPanel);
        add(wrapperPanel);

        addUserStatusEventHandler(this);
        registerStorageListener();
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
                            fireUserUpdateEvent();
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
        Anchor register = new Anchor("Register",
                EntryPointLinkFactory.createRegistrationLink(new HashMap<String, String>()));
        formContent.add(register);

        formContent.add(new OAuthLoginPanel(userManagementService));

        loginPanel.setWidget(formContent);
    }

    private void initUserContent() {
        userPanel = new FlowPanel();
    }

    private void updateUserContent() {
        if (currentUser != null) {
            userPanel.clear();
            Anchor logout = new Anchor("Logout");
            userPanel.add(logout);
            logout.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    userManagementService.logout(new AsyncCallback<SuccessInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Could not log out:" + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(SuccessInfo result) {
                            currentUser = null;
                            updateStatus();
                            fireUserUpdateEvent();
                        }
                    });
                }
            });
        }
    }

    private static void updateUser() {
        userManagementService.getCurrentUser(new AsyncCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                if (result != null && currentUser != null && result.getName().equals(currentUser.getName())) {
                    return;
                }
                currentUser = result;
                System.out.println("User changed to " + (result == null ? "No User" : result.getName()) + "handlers: "
                        + handlers.size());
                for (UserStatusEventHandler handler : handlers) {
                    System.out.print(handler + ", ");
                    handler.onUserStatusChange(currentUser);
                }
                System.out.println();
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        });
    }

    private void updateStatus() {
        if (currentUser != null) {
            String name = currentUser.getName();
            if (name == null) {
                loginTitle.setText("Inavalid username!");
            } else {
                if (name.contains("*")) {
                    name = name.split("\\*")[1];
                }
                loginTitle.setTitle(name);
                if (name.length() > 15) {
                    name = name.substring(0, 12) + "...";
                }
                loginTitle.setText("Welcome, " + name + "! ");
            }
            infoPanel.setWidget(userPanel);
            loginLink.setText("");
            updateUserContent();
        } else {
            infoPanel.setWidget(loginPanel);
            loginTitle.setText("");
            loginLink.setText("Login");
        }
        expanded = false;
        wrapperPanel.addStyleName(StylesheetResources.INSTANCE.css().loginPanelCollapsed());
        wrapperPanel.removeStyleName(StylesheetResources.INSTANCE.css().loginPanelExpanded());
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

    public static UserDTO getCurrentUser() {
        return currentUser;
    }

    public static void addUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.add(handler);
    }

    public static void removeUserStatusEventHandler(UserStatusEventHandler handler) {
        handlers.remove(handler);
    }

    @Override
    public void onUserStatusChange(UserDTO user) {
        updateStatus();
    }

    public static void registerStorageListener() {
        if (localStorage != null) {
            Storage.addStorageEventHandler(new StorageEvent.Handler() {

                @Override
                public void onStorageChange(StorageEvent event) {
                    if ("update".equals(event.getKey()) && "true".equals(event.getNewValue())) {
                        updateUser();
                    }
                }
            });
        }
    };

    /**
     * Used to synchronize changes in the user status between all browser tabs/windows.
     * 
     */
    public static void fireUserUpdateEvent() {
        if (localStorage != null) {
            localStorage.setItem("update", "true");
            localStorage.setItem("update", "false");
        }
    };

    public static String getUpdateProperty() {
        if (localStorage != null) {
            return localStorage.getItem("update");
        }
        return null;
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoginPanel other = (LoginPanel) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LoginPanel [id=" + id + "]";
    }
}
