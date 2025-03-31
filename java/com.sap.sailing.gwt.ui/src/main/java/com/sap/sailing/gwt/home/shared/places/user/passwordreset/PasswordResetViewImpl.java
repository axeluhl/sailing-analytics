package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

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
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordResetViewImpl extends Composite implements PasswordResetView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, PasswordResetViewImpl> {
    }

    private Presenter currentPresenter;

    @UiField StringMessages i18n_sec;
    @UiField protected DivElement errorsUi;
    @UiField protected Button changePasswordUi;
    @UiField protected PasswordTextBox newPasswordUi;
    @UiField protected PasswordTextBox newPasswordConfirmationUi;

    public PasswordResetViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(newPasswordUi, i18n_sec.newPasswordPlaceholder());
        setPlaceholder(newPasswordConfirmationUi, i18n_sec.passwordRepeatPlaceholder());
        
        newPasswordUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                currentPresenter.onChangePassword(newPasswordUi.getValue());
            }
        });
        newPasswordConfirmationUi.addKeyUpHandler(new FieldKeyUpHandler() {
            @Override
            void updateFieldValue() {
                currentPresenter.onChangePasswordConfirmation(newPasswordConfirmationUi.getValue());
            }
        });
    }
    
    @Override
    public HasEnabled getChangePasswordControl() {
        return changePasswordUi;
    }

    @Override
    public void setPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }
    
    @Override
    public void setErrorMessage(String errorMessage) {
        errorsUi.setInnerText(errorMessage);
    }
    
    @Override
    protected void onLoad() {
        selectAll(newPasswordUi);
    }

    @UiHandler("changePasswordUi")
    void onChangePasswordClicked(ClickEvent event) {
        currentPresenter.resetPassword();
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
                currentPresenter.resetPassword();
            } else {
                updateFieldValue();
            }
        }
        
        abstract void updateFieldValue();
    }
}
