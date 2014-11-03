package com.sap.sse.security.ui.login;

import java.util.Collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.Resources;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.oauth.client.component.OAuthLoginPanel;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginView extends Composite {
    private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

    interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {
    }

    private final UserManagementServiceAsync userManagementService;
    private final UserService userService;
    
    @UiField TextBox nameText;
    @UiField Button passwordReset; 
    @UiField PasswordTextBox pwText;
    @UiField Button submit;
    @UiField Anchor signUpAnchor;
    @UiField HTMLPanel oAuthPanel;
    
    public LoginView(UserManagementServiceAsync userManagementService, UserService userService) {
        this.userManagementService = userManagementService;
        this.userService = userService;

        initWidget(uiBinder.createAndBindUi(this));

        String registrationLink = EntryPointLinkFactory.createRegistrationLink(Collections.<String, String> emptyMap());
        signUpAnchor.setHref(registrationLink);
        
        oAuthPanel.add(new OAuthLoginPanel(userManagementService, Resources.INSTANCE.css()));
        
        nameText.setFocus(true);
    }
    
    @UiHandler("submit")
    void submitClicked(ClickEvent e) {
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
                        Window.alert(StringMessages.INSTANCE.loggedIn(result.getUserDTO().getName()));
                    }
                } else {
                    if (SuccessInfo.FAILED_TO_LOGIN.equals(result.getMessage())) {
                        Window.alert(StringMessages.INSTANCE.failedToSignIn());
                    } else {
                        Window.alert(result.getMessage());
                    }
                }
            }
        });        
    }
    
    @UiHandler("passwordReset")
    void passwordResetClicked(ClickEvent e) {
        userManagementService.resetPassword(nameText.getText(), new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof UserManagementException) {
                    if (UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL.equals(caught.getMessage())) {
                        Window.alert(StringMessages.INSTANCE.cannotResetPasswordWithoutValidatedEmail(nameText.getText()));
                    } else {
                        Window.alert(StringMessages.INSTANCE.errorDuringPasswordReset(caught.getMessage()));
                    }
                } else {
                    Window.alert(StringMessages.INSTANCE.errorDuringPasswordReset(caught.getMessage()));
                }
            }

            @Override
            public void onSuccess(Void result) {
                Window.alert(StringMessages.INSTANCE.newPasswordSent(nameText.getText()));
            }
        }));
    }
}
