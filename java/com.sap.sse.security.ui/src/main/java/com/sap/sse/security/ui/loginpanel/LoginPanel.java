package com.sap.sse.security.ui.loginpanel;

import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.user.client.ui.Label;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.IconResources;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;
import com.sap.sse.security.ui.client.component.AbstractUserDialog.UserData;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.shared.oauthlogin.OAuthLogin;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;

public class LoginPanel extends HorizontalPanel implements UserStatusEventHandler {
    public final UserManagementServiceAsync userManagementService;
    
    public static final StringMessages stringMessages = GWT.create(StringMessages.class);

    private UserService userService;

    private final Anchor signInLink;
    private final Anchor signOutLink;
    private final Anchor signUpLink;
    private final Label welcomeMessage;

    private final OAuthLogin oAuthPanel;

    public LoginPanel(final LoginPanelCss css, final UserService userService) {
        this.userManagementService = userService.getUserManagementService();
        this.userService = userService;
        css.ensureInjected();
        getElement().addClassName(css.loginPanel());
        welcomeMessage = new Label();
        signInLink = new Anchor(stringMessages.signIn());
        signInLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SignInDialog signInDialog = new SignInDialog(stringMessages, userManagementService, userService, new DialogCallback<UserData>() {
                    @Override
                    public void ok(UserData userData) {
                        userService.login(userData.getUsername(), userData.getPassword(), new MarkedAsyncCallback<SuccessInfo>(new AsyncCallback<SuccessInfo>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.invalidCredentials(), NotificationType.ERROR);
                            }
                            @Override public void onSuccess(SuccessInfo result) {
                                if (!result.isSuccessful()) {
                                    Notification.notify(stringMessages.invalidCredentials(), NotificationType.ERROR);
                                }
                            }
                        }));
                    }
                    @Override public void cancel() {}
                });
                signInDialog.show();
            }
        });
        signUpLink = new Anchor(stringMessages.signUp());
        signUpLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SignUpDialog signUpDialog = new SignUpDialog(stringMessages, userManagementService, new DialogCallback<UserData>() {
                    @Override
                    public void ok(final UserData userData) {
                        userManagementService.createSimpleUser(userData.getUsername(), userData.getEmail(), userData.getPassword(),
                                /* fullName */ null, /* company */ null, LocaleInfo.getCurrentLocale().getLocaleName(),
                                EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()),
                                new MarkedAsyncCallback<UserDTO>(new AsyncCallback<UserDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.errorCreatingUser(userData.getUsername(), caught.getMessage()), NotificationType.ERROR);
                            }
                            @Override public void onSuccess(UserDTO result) {
                                userService.login(userData.getUsername(), userData.getPassword(), new MarkedAsyncCallback<SuccessInfo>(new AsyncCallback<SuccessInfo>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        // pretty strange; we just successfully created the user with these credentials...
                                        Notification.notify(stringMessages.invalidCredentials(), NotificationType.ERROR);
                                    }
                                    @Override public void onSuccess(SuccessInfo result) {}
                                }));
                            }
                        }));
                    }
                    @Override public void cancel() {}
                });
                signUpDialog.show();
            }
        });
        signOutLink = new Anchor(stringMessages.signOut());
        signOutLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                userService.logout();
            }
        });

        final ImageResource userImageResource = IconResources.INSTANCE.userIcon();
        ImageResourceRenderer renderer = new ImageResourceRenderer();
        final Anchor imageContainer = new Anchor(renderer.render(userImageResource));
        imageContainer.setHref(EntryPointLinkFactory.createPasswordResetLink(new HashMap<String, String>()));
        imageContainer.setTitle(stringMessages.editProfile());
        imageContainer.getElement().addClassName(css.userIcon());
        add(imageContainer);
        welcomeMessage.getElement().addClassName(css.welcomeMessage());
        signInLink.getElement().addClassName(css.link());
        signUpLink.getElement().addClassName(css.link());
        signOutLink.getElement().addClassName(css.link());
        add(welcomeMessage);
        add(signInLink);
        add(signUpLink);
        add(signOutLink);
        oAuthPanel = new OAuthLogin(userManagementService);
        add(oAuthPanel);
        userService.addUserStatusEventHandler(this);
        updateStatus();
    }

    public void updateStatus() {
        if (userService.getCurrentUser() != null) {
            String name = userService.getCurrentUser().getName();
            final String displayName;
            if (name == null) {
                displayName = "";
            } else if (name.contains("*")) {
                // FIXME social user account and name is separated by '*' character; see bug 2441
                displayName = name.split("\\*")[1];
            } else { 
                displayName = name;
            }
            final String croppedDisplayName;
            if (displayName.length() > 15) {
                croppedDisplayName = displayName.substring(0, 12) + "...";
            } else {
                croppedDisplayName = displayName;
            }
            welcomeMessage.setText(stringMessages.welcome(croppedDisplayName));
            signInLink.setVisible(false);
            signUpLink.setVisible(false);
            oAuthPanel.setVisible(false);
            signOutLink.setVisible(true);
        } else {
            welcomeMessage.setText("");
            signInLink.setVisible(true);
            signUpLink.setVisible(true);
            oAuthPanel.setVisible(true);
            signOutLink.setVisible(false);
        }
    }

    @Override
    public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
        updateStatus();
    }
}
