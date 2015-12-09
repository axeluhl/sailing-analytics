package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;

public class SignInForm extends Composite {
    
    interface SignInFormUiBinder extends UiBinder<Widget, SignInForm> {
    }
    
    private static SignInFormUiBinder uiBinder = GWT.create(SignInFormUiBinder.class);
    
    @UiField TextBox loginNameUi;
    @UiField PasswordTextBox passwordUi;
    @UiField Anchor forgotPasswordUi;
    @UiField Anchor createAccountUi;
    @UiField Anchor signInUi;
    @UiField Anchor loginFacebookUi;
    @UiField Anchor loginGoogleUi;
    
    @UiField DivElement formErrorUi;
    @UiField DivElement socialLoginUi;

    public SignInForm() {
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(loginNameUi, "TODO Username or Email");
        setPlaceholder(passwordUi, "TODO Password");
        if (!ExperimentalFeatures.SHOW_SOCIAL_LOGINS_FOR_USER_MANGEMENT) {
            socialLoginUi.removeFromParent();
        }
    }
    
    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
    }
    
    @UiHandler("forgotPasswordUi")
    void onForgotPasswordUiControlClicked(ClickEvent event) {
        // TODO
    }
    
    @UiHandler("createAccountUi")
    void onCreateAccountUiControlClicked(ClickEvent event) {
        // TODO
    }
    
    @UiHandler("signInUi")
    void onSignInControlUiClicked(ClickEvent event) {
        // String login = loginNameUi.getValue(), pw = passwordUi.getValue();
        // TODO
    }
    
    @UiHandler("loginFacebookUi")
    void onLoginFacebookUiClicked(ClickEvent event) {
        // TODO
    }
    
    @UiHandler("loginGoogleUi")
    void onLoginGoogleUiClicked(ClickEvent event) {
        // TODO
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
}
