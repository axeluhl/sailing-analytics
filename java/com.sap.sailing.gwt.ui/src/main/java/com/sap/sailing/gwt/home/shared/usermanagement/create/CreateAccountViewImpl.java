package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;
import com.sap.sse.security.ui.authentication.create.CreateAccountView;
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
        
        emailUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                presenter.onChangeEmail(emailUi.getValue());
            }
        });
        usernameUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                presenter.onChangeUsername(usernameUi.getValue());
            }
        });
        nameUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                presenter.onChangeFullName(nameUi.getValue());
            }
        });
        companyUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                presenter.onChangeCompany(companyUi.getValue());
            }
        });
        passwordUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                presenter.onChangePassword(passwordUi.getValue());
            }
        });
        passwordConfirmationUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                presenter.onChangePasswordConfirmation(passwordConfirmationUi.getValue());
            }
        });
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
        presenter.createAccount();
    }
    
    @UiHandler("signInUi")
    void onSignInControlUiClicked(ClickEvent event) {
        presenter.signIn();
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
    private void selectAll(TextBox textBox) {
        textBox.setFocus(true);
        textBox.selectAll();
    }
    
    private abstract class FieldKeyUpHandler implements KeyUpHandler {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                presenter.createAccount();
            } else {
                updateFieldValue();
            }
        }
        
        abstract void updateFieldValue();
    }
}
