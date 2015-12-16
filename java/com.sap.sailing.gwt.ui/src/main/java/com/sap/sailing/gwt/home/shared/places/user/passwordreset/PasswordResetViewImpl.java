package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

public class PasswordResetViewImpl extends Composite implements PasswordResetView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, PasswordResetViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField
    protected Button changePasswordUi;
    @UiField
    protected PasswordTextBox newPasswordUi;
    @UiField
    protected PasswordTextBox newPasswordConfirmationUi;

    
    public PasswordResetViewImpl(String messageTitle, String message) {
        initWidget(uiBinder.createAndBindUi(this));
        changePasswordUi.setEnabled(false);
    }

    @Override
    public void setPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }

    @UiHandler("changePasswordUi")
    void onKeyUp(KeyUpEvent event) {
        changePasswordUi.setEnabled(isInputValid());
    }

    private boolean isInputValid() {
        String newP = newPasswordUi.getValue();
        String repP = newPasswordConfirmationUi.getValue();
        return (newP != null && !newP.isEmpty() && repP != null && !repP.isEmpty());
    }

    @UiHandler("changePasswordUi")
    void onChangePasswordClicked(ClickEvent event) {
        if (isInputValid()) {
            currentPresenter.resetPassword(newPasswordUi.getValue());
        }
    }
}
