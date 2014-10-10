package com.sap.sse.security.ui.loginpanel;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.oauth.client.component.OAuthLoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginPanel extends FlowPanel implements UserStatusEventHandler {
    public final UserManagementServiceAsync userManagementService;
    
    public static final StringMessages stringMessages = GWT.create(StringMessages.class);

    private FormPanel loginPanel;
    private FlowPanel userPanel;
    private SimplePanel infoPanel;

    private static int counter = 0;

    private boolean expanded = false;
    private UserService userService;

    private Label loginTitle;
    private Anchor loginLink;

    private TextBox name;

    private PasswordTextBox password;

    private FlowPanel wrapperPanel;

    private final int id;

    private final Css css;

    public LoginPanel(final Css css, UserService userService) {
        this.userManagementService = userService.getUserManagementService();
        this.userService = userService;
        this.css = css;
        id = counter++;
        css.ensureInjected();
        wrapperPanel = new FlowPanel();
        wrapperPanel.addStyleName(css.loginPanel());
        wrapperPanel.addStyleName(css.loginPanelCollapsed());
        FocusPanel titleFocus = new FocusPanel();
        FlowPanel titlePanel = new FlowPanel();
        titleFocus.setWidget(titlePanel);
        titlePanel.addStyleName(css.loginPanelTitlePanel());
        loginTitle = new Label("");
        loginLink = new Anchor(stringMessages.signIn());
        final ImageResource userImageResource = Resources.INSTANCE.userIcon();
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
        infoPanel.addStyleName(css.loginPanelInfoPanel());
        initLoginContent();
        initUserContent();
        infoPanel.setWidget(loginPanel);
        wrapperPanel.add(infoPanel);
        add(wrapperPanel);
        addUserStatusEventHandler(this);
    }

    private void initLoginContent() {
        loginPanel = new FormPanel();
        loginPanel.addSubmitHandler(new SubmitHandler() {
            @Override
            public void onSubmit(SubmitEvent event) {
                userService.login(name.getText(), password.getText(),
                        new AsyncCallback<SuccessInfo>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(SuccessInfo result) {
                        if (!result.isSuccessful()) {
                            Window.alert(result.getMessage());
                        }
                    }
                });
            }
        });
        FlowPanel formContent = new FlowPanel();
        Label nameLabel = new Label(stringMessages.name()+": ");
        formContent.add(nameLabel);
        name = new TextBox();
        formContent.add(name);
        Label passwordLabel = new Label(stringMessages.password()+": ");
        formContent.add(passwordLabel);
        password = new PasswordTextBox();
        formContent.add(password);
        SubmitButton submit = new SubmitButton(stringMessages.signIn());
        formContent.add(submit);
        Anchor register = new Anchor(stringMessages.signUp(),
                EntryPointLinkFactory.createRegistrationLink(new HashMap<String, String>()));
        formContent.add(register);
        formContent.add(new OAuthLoginPanel(userManagementService));
        loginPanel.setWidget(formContent);
    }

    private void initUserContent() {
        userPanel = new FlowPanel();
    }

    private void updateUserContent() {
        if (getCurrentUser() != null) {
            userPanel.clear();
            Anchor logout = new Anchor(stringMessages.signOut());
            userPanel.add(logout);
            logout.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    userService.logout();
                }
            });
        }
    }

    public void updateStatus() {
        if (getCurrentUser() != null) {
            String name = getCurrentUser().getName();
            if (name == null) {
                loginTitle.setText(stringMessages.invalidUsername());
            } else {
                if (name.contains("*")) {
                    name = name.split("\\*")[1];
                }
                loginTitle.setTitle(name);
                if (name.length() > 15) {
                    name = name.substring(0, 12) + "...";
                }
                loginTitle.setText(stringMessages.welcome(name));
            }
            infoPanel.setWidget(userPanel);
            loginLink.setText("");
            updateUserContent();
        } else {
            infoPanel.setWidget(loginPanel);
            loginTitle.setText("");
            loginLink.setText(stringMessages.signIn());
        }
        expanded = false;
        wrapperPanel.addStyleName(css.loginPanelCollapsed());
        wrapperPanel.removeStyleName(css.loginPanelExpanded());
    }

    private void toggleLoginPanel() {
        if (expanded) {
            expanded = false;
            wrapperPanel.addStyleName(css.loginPanelCollapsed());
            wrapperPanel.removeStyleName(css.loginPanelExpanded());
        } else {
            expanded = true;
            wrapperPanel.addStyleName(css.loginPanelExpanded());
            wrapperPanel.removeStyleName(css.loginPanelCollapsed());
        }
    }

    public UserDTO getCurrentUser() {
        return userService.getCurrentUser();
    }

    public void addUserStatusEventHandler(UserStatusEventHandler handler) {
        userService.addUserStatusEventHandler(handler);
    }

    public void removeUserStatusEventHandler(UserStatusEventHandler handler) {
        userService.removeUserStatusEventHandler(handler);
    }

    @Override
    public void onUserStatusChange(UserDTO user) {
        updateStatus();
    }

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
