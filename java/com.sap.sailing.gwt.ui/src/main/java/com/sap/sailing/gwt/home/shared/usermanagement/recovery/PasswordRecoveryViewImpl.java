package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PasswordRecoveryViewImpl extends Composite implements PasswordRecoveryView {
    
    interface PasswordRecoveryViewImplUiBinder extends UiBinder<Widget, PasswordRecoveryViewImpl> {
    }
    
    private static PasswordRecoveryViewImplUiBinder uiBinder = GWT.create(PasswordRecoveryViewImplUiBinder.class);
    
    @UiField TextBox loginNameUi;
    
    @UiField DivElement formErrorUi;

    private Presenter presenter;
    
    public PasswordRecoveryViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(loginNameUi, "TODO Username or Email");
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
    }
    
    @UiHandler("resetPasswordUi")
    void onResetPasswordUiControlClicked(ClickEvent event) {
        presenter.resetPassword(loginNameUi.getValue());
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
}
