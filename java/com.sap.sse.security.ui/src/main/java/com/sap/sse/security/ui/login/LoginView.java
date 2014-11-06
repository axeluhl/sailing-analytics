package com.sap.sse.security.ui.login;

import java.util.Collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.controls.PasswordTextBoxWithWatermark;
import com.sap.sse.gwt.client.controls.TextBoxWithWatermark;
import com.sap.sse.gwt.client.dialog.DialogUtils;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.ForgotPasswordDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.shared.oauthlogin.OAuthLogin;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class LoginView extends Composite {
    private static LoginViewUiBinder uiBinder = GWT.create(LoginViewUiBinder.class);

    interface LoginViewUiBinder extends UiBinder<Widget, LoginView> {
    }

    private final UserManagementServiceAsync userManagementService;
    private final UserService userService;
    
    @UiField TextBoxWithWatermark userNameTextBox;
    @UiField Label appName;
    @UiField Anchor forgotPasswordAnchor; 
    @UiField PasswordTextBoxWithWatermark passwordTextBox;
    @UiField Button loginButton;
    @UiField Anchor signUpAnchor;
    @UiField HTMLPanel oAuthPanel;
    
    public LoginView(UserManagementServiceAsync userManagementService, UserService userService, String appName) {
        this.userManagementService = userManagementService;
        this.userService = userService;
        LoginViewResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.appName.setText(appName);
        userNameTextBox.setWatermark(StringMessages.INSTANCE.username());
        userNameTextBox.setWatermarkStyleName(LoginViewResources.INSTANCE.css().textInput_watermark());
        passwordTextBox.setWatermark(StringMessages.INSTANCE.password());
        passwordTextBox.setWatermarkStyleName(LoginViewResources.INSTANCE.css().passwordTextInput_watermark());
        String registrationLink = EntryPointLinkFactory.createRegistrationLink(Collections.<String, String> emptyMap());
        signUpAnchor.setHref(registrationLink);
        oAuthPanel.add(new OAuthLogin(userManagementService));
        userNameTextBox.setFocus(true);
        DialogUtils.linkEnterToButton(loginButton, userNameTextBox, passwordTextBox);
        userNameTextBox.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                userNameTextBox.setFocus(true);
            }
        });
    }
    
    @UiHandler("loginButton")
    void loginButtonClicked(ClickEvent e) {
        userService.login(userNameTextBox.getText(), passwordTextBox.getText(), new AsyncCallback<SuccessInfo>() {
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
    
    @UiHandler("forgotPasswordAnchor")
    void forgotPasswordClicked(ClickEvent e) {
        new ForgotPasswordDialog(StringMessages.INSTANCE, userManagementService, userService.getCurrentUser()).show();
    }
}
