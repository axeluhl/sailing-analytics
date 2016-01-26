package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;

public class PasswordRecoveryViewImpl extends Composite implements PasswordRecoveryView {
    
    interface PasswordRecoveryViewImplUiBinder extends UiBinder<Widget, PasswordRecoveryViewImpl> {
    }
    
    private static PasswordRecoveryViewImplUiBinder uiBinder = GWT.create(PasswordRecoveryViewImplUiBinder.class);
    
    @UiField TextBox emailUi;
    @UiField TextBox usernameUi;
    
    @UiField DivElement formErrorUi;
    
    @UiField(provided = true)
    final UserManagementSharedResources res;

    private Presenter presenter;
    
    public PasswordRecoveryViewImpl(UserManagementSharedResources resources) {
        this.res = resources;
        UserManagementResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        formErrorUi.setInnerText(errorMessage);
        selectAll(usernameUi.getValue().isEmpty() ? emailUi : usernameUi);
    }
    
    @Override
    protected void onLoad() {
        selectAll(emailUi);
    }
    
    @UiHandler("resetPasswordUi")
    void onResetPasswordUiControlClicked(ClickEvent event) {
        triggerPasswordReset();
    }
    
    @UiHandler({ "emailUi", "usernameUi"})
    void onResetPasswordKeyPressed(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            triggerPasswordReset();
        }
    }
    
    private void triggerPasswordReset() {
        presenter.resetPassword(emailUi.getValue(), usernameUi.getValue());
    }
    
    private void selectAll(TextBox textBox) {
        textBox.setFocus(true);
        textBox.selectAll();
    }
}
