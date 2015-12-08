package com.sap.sailing.gwt.home.shared.usermanagement;

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

public class CreateAccountForm extends Composite {
    
    interface CreateAccountFormUiBinder extends UiBinder<Widget, CreateAccountForm> {
    }
    
    private static CreateAccountFormUiBinder uiBinder = GWT.create(CreateAccountFormUiBinder.class);
    
    @UiField TextBox emailUi;
    @UiField TextBox usernameUi;
    @UiField PasswordTextBox passwordUi;
    @UiField PasswordTextBox passwordConfirmationUi;
    @UiField Anchor createAccountUi;
    @UiField Anchor signInUi;
    
    @UiField DivElement formErrorUi;
    
    public CreateAccountForm() {
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(emailUi, "TODO Email");
        setPlaceholder(usernameUi, "TODO Username");
        setPlaceholder(passwordUi, "TODO Password");
        setPlaceholder(passwordConfirmationUi, "TODO Password Confirmation");
    }
    
    @UiHandler("createAccountUi")
    void onCreateAccountUiControlClicked(ClickEvent event) {
        // TODO
    }
    
    @UiHandler("signInUi")
    void onSignInControlUiClicked(ClickEvent event) {
        // TODO
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
}
