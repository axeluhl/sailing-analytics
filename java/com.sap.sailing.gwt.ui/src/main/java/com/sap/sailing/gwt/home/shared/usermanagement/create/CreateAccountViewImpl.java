package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementResources;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.UserManagementSharedResources;

public class CreateAccountViewImpl extends Composite implements CreateAccountView {
    
    interface CreateAccountViewImplUiBinder extends UiBinder<Widget, CreateAccountViewImpl> {
    }
    
    private static CreateAccountViewImplUiBinder uiBinder = GWT.create(CreateAccountViewImplUiBinder.class);
    
    @UiField TextBox emailUi;
    @UiField TextBox usernameUi;
    @UiField PasswordTextBox passwordUi;
    @UiField PasswordTextBox passwordConfirmationUi;
    @UiField Anchor createAccountUi;
    @UiField Anchor signInUi;
    
    @UiField DivElement formErrorUi;
    
    @UiField(provided = true)
    UserManagementSharedResources res = SharedResources.INSTANCE;

    private Presenter presenter;
    
    public CreateAccountViewImpl() {
        UserManagementResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        StringMessages i18n = StringMessages.INSTANCE;
        setPlaceholder(passwordUi, i18n.newPasswordPlaceholder());
        setPlaceholder(passwordConfirmationUi, i18n.passwordRepeatPlaceholder());
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    @Override
    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
        selectAll(emailUi);
    }
    
    @Override
    protected void onLoad() {
        selectAll(emailUi);
    }
    
    @UiHandler("createAccountUi")
    void onCreateAccountUiControlClicked(ClickEvent event) {
        triggerCreateAccount();
    }
    
    @UiHandler({ "emailUi", "usernameUi", "passwordUi", "passwordConfirmationUi"})
    void onCreateAccountKeyPressed(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            triggerCreateAccount();
        }
    }
    
    @UiHandler("signInUi")
    void onSignInControlUiClicked(ClickEvent event) {
        presenter.signIn();
    }
    
    private void triggerCreateAccount() {
        String username = usernameUi.getValue(), email = emailUi.getValue();
        String password = passwordUi.getValue(), passwordConfirmation = passwordConfirmationUi.getValue();
        presenter.createAccount(username, email, password, passwordConfirmation);
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
    private void selectAll(TextBox textBox) {
        textBox.setFocus(true);
        textBox.selectAll();
    }
    
}
