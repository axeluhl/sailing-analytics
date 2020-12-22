package com.sap.sailing.gwt.managementconsole.partials.authentication.signin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.managementconsole.partials.authentication.AuthenticationResources;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class SignInViewImpl implements SignInView {

    interface SignInViewImplUiBinder extends UiBinder<Widget, SignInViewImpl> {
    }

    private static SignInViewImplUiBinder uiBinder = GWT.create(SignInViewImplUiBinder.class);

    @UiField
    ManagementConsoleResources app_res;
    @UiField
    AuthenticationResources local_res;
    @UiField
    StringMessages i18n;
    @UiField
    TextBox username;
    @UiField
    PasswordTextBox password;
    @UiField
    Button forgotPassword, createAccount, signIn;

    private final PopupPanel popupPanel = new PopupPanel(false, true);
    private Presenter presenter;

    public SignInViewImpl() {
        popupPanel.setWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
        popupPanel.setStyleName(local_res.style().signIn());

        username.getElement().setAttribute("placeholder", i18n.username());
        password.getElement().setAttribute("placeholder", i18n.password());
    }

    @Override
    public Widget asWidget() {
        return popupPanel.getWidget();
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clearInputs() {
        username.setText(null);
        password.setText(null);
    }

    @Override
    public void show() {
        popupPanel.show();
        selectAll(username);
    }

    @Override
    public void hide() {
        popupPanel.hide();
    }

    @UiHandler("forgotPassword")
    void onForgotPasswordUiControlClicked(final ClickEvent event) {
        presenter.forgotPassword();
    }

    @UiHandler("createAccount")
    void onCreateAccountUiControlClicked(final ClickEvent event) {
        presenter.createAccount();
    }

    @UiHandler("signIn")
    void onSignInControlUiClicked(final ClickEvent event) {
        triggerLogin();
    }

    @UiHandler({ "username", "password" })
    void onSignInKeyPressed(final KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            triggerLogin();
        }
    }

    private void triggerLogin() {
        presenter.login(username.getValue(), password.getValue());
    }

    private void selectAll(final TextBox textBox) {
        textBox.setFocus(true);
        textBox.selectAll();
    }

}
