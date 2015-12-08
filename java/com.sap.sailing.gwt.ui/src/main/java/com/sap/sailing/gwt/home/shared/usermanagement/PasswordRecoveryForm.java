package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PasswordRecoveryForm extends Composite {
    
    interface PasswordRecoveryFormUiBinder extends UiBinder<Widget, PasswordRecoveryForm> {
    }
    
    private static PasswordRecoveryFormUiBinder uiBinder = GWT.create(PasswordRecoveryFormUiBinder.class);
    
    @UiField TextBox loginNameUi;
    
    @UiField DivElement formErrorUi;
    
    public PasswordRecoveryForm() {
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(loginNameUi, "TODO Username or Email");
    }

    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
    }
    
    @UiHandler("resetPasswordUi")
    void onResetPasswordUiControlClicked(ClickEvent event) {
        // String login = loginNameUi.getValue();
        // TODO
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
}
