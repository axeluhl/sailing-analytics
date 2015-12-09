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

public class SignInViewImpl extends Composite implements SignInView {
    
    interface SignInViewImplUiBinder extends UiBinder<Widget, SignInViewImpl> {
    }
    
    private static SignInViewImplUiBinder uiBinder = GWT.create(SignInViewImplUiBinder.class);
    
    @UiField TextBox loginNameUi;
    @UiField PasswordTextBox passwordUi;
    @UiField Anchor forgotPasswordUi;
    @UiField Anchor createAccountUi;
    @UiField Anchor signInUi;
    @UiField Anchor loginFacebookUi;
    @UiField Anchor loginGoogleUi;
    
    @UiField DivElement formErrorUi;
    @UiField DivElement socialLoginUi;

    private Presenter presenter;

    public SignInViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(loginNameUi, "TODO Username or Email");
        setPlaceholder(passwordUi, "TODO Password");
        if (!ExperimentalFeatures.SHOW_SOCIAL_LOGINS_FOR_USER_MANGEMENT) {
            socialLoginUi.removeFromParent();
        }
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
    }
    
    @UiHandler("forgotPasswordUi")
    void onForgotPasswordUiControlClicked(ClickEvent event) {
        presenter.forgotPassword();
    }
    
    @UiHandler("createAccountUi")
    void onCreateAccountUiControlClicked(ClickEvent event) {
        presenter.createAccount();
    }
    
    @UiHandler("signInUi")
    void onSignInControlUiClicked(ClickEvent event) {
         presenter.login(loginNameUi.getValue(), passwordUi.getValue());
    }
    
    @UiHandler("loginFacebookUi")
    void onLoginFacebookUiClicked(ClickEvent event) {
        presenter.loginWithFacebook();
    }
    
    @UiHandler("loginGoogleUi")
    void onLoginGoogleUiClicked(ClickEvent event) {
        presenter.loginWithGoogle(); 
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
}
