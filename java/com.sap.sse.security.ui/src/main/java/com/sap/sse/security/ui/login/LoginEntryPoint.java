package com.sap.sse.security.ui.login;

import java.util.Collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.gwt.client.AbstractEntryPoint;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.oauth.client.component.OAuthLoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginEntryPoint extends AbstractEntryPoint {
    private final StringMessages stringMessages = GWT.create(StringMessages.class);
    private static final Resources resource = GWT.create(Resources.class);
    private UserManagementServiceAsync userManagementService;
    private UserService userService;
    
    @Override
    public void doOnModuleLoad() {
        userManagementService = GWT.create(UserManagementService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) userManagementService,
                RemoteServiceMappingConstants.userManagementServiceRemotePath);
        userService = new UserService(userManagementService);
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(dockPanel);
        
        FlowPanel fp = new FlowPanel();
        Label nameLabel = new Label(stringMessages.username());
        fp.add(nameLabel);
        final TextBox nameText = new TextBox();
        nameText.ensureDebugId("username");
        fp.add(nameText);
        final Button passwordReset = new Button(stringMessages.resetPassword());
        passwordReset.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                userManagementService.resetPassword(nameText.getText(), new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            if (UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL.equals(caught.getMessage())) {
                                Window.alert(stringMessages.cannotResetPasswordWithoutValidatedEmail(nameText.getText()));
                            } else {
                                Window.alert(stringMessages.errorDuringPasswordReset(caught.getMessage()));
                            }
                        } else {
                            Window.alert(stringMessages.errorDuringPasswordReset(caught.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Window.alert(stringMessages.newPasswordSent(nameText.getText()));
                    }
                }));
            }
        });
        fp.add(passwordReset);
        Label pwLabel = new Label(stringMessages.password());
        pwLabel.ensureDebugId("password");
        fp.add(pwLabel);
        final PasswordTextBox pwText = new PasswordTextBox();
        fp.add(pwText);
        final SubmitButton submit = new SubmitButton(stringMessages.signIn());
        submit.ensureDebugId("login");
        fp.add(submit);
        fp.add(new Anchor(new SafeHtmlBuilder().appendEscaped(stringMessages.signUp()).toSafeHtml(),
                EntryPointLinkFactory.createRegistrationLink(Collections.<String, String> emptyMap())));
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
                                Window.alert(stringMessages.loggedIn(result.getUserDTO().getName()));
                            }
                        } else {
                            if (SuccessInfo.FAILED_TO_LOGIN.equals(result.getMessage())) {
                                Window.alert(stringMessages.failedToSignIn());
                            } else {
                                Window.alert(result.getMessage());
                            }
                        }
                    }
                });
            }
        });
        fp.add(new OAuthLoginPanel(userManagementService, resource.css()));
        formPanel.add(fp);
        dockPanel.add(formPanel);
        nameText.setFocus(true);
    }
}
