package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class CreateAccountViewImpl extends Composite implements CreateAccountView {
    
    interface CreateAccountViewImplUiBinder extends UiBinder<Widget, CreateAccountViewImpl> {
    }
    
    private static CreateAccountViewImplUiBinder uiBinder = GWT.create(CreateAccountViewImplUiBinder.class);
    
    @UiField TextBox emailUi;
    @UiField TextBox usernameUi;
    @UiField TextBox nameUi;
    @UiField TextBox companyUi;
    @UiField PasswordTextBox passwordUi;
    @UiField PasswordTextBox passwordConfirmationUi;
    @UiField Button createAccountUi;
    @UiField Button signInUi;
    
    @UiField DivElement formErrorUi;
    
    @UiField(provided = true)
    final UserManagementSharedResources res;

    private Presenter presenter;
    
    public CreateAccountViewImpl(UserManagementSharedResources resources) {
        this.res = resources;
        UserManagementResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        StringMessages i18n = StringMessages.INSTANCE;
        setPlaceholder(passwordUi, i18n.newPasswordPlaceholder());
        setPlaceholder(passwordConfirmationUi, i18n.passwordRepeatPlaceholder());
    }
    
    @Override
    public HasEnabled getCreateAccountControl() {
        return createAccountUi;
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    @Override
    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
    }
    
    @Override
    protected void onLoad() {
        selectAll(emailUi);
    }
    
    @UiHandler("createAccountUi")
    void onCreateAccountUiControlClicked(ClickEvent event) {
        triggerValidateOrCreateAccount(true);
    }
    
    @UiHandler({ "emailUi", "usernameUi", "nameUi", "companyUi", "passwordUi", "passwordConfirmationUi"})
    void onCreateAccountKeyPressed(KeyUpEvent event) {
        triggerValidateOrCreateAccount(event.getNativeKeyCode() == KeyCodes.KEY_ENTER);
    }
    
    @UiHandler("signInUi")
    void onSignInControlUiClicked(ClickEvent event) {
        presenter.signIn();
    }
    
    private void triggerValidateOrCreateAccount(boolean create) {
        String username = usernameUi.getValue(), password = passwordUi.getValue(); 
        String passwordConfirmation = passwordConfirmationUi.getValue();
        if (create) {
            String email = emailUi.getValue(), fullName = nameUi.getValue(), company = companyUi.getValue();
            presenter.createAccount(username, fullName, company, email, password, passwordConfirmation);
        } else {
            presenter.validate(username, password, passwordConfirmation);
        }
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
    private void selectAll(TextBox textBox) {
        textBox.setFocus(true);
        textBox.selectAll();
    }
    
}
