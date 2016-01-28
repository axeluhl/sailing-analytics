package com.sap.sailing.gwt.home.mobile.partials.userdetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserDetails extends Composite {

    interface MyUiBinder extends UiBinder<Widget, UserDetails> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    public interface Presenter {
        void handleSaveChangesRequest(String fullName, String company);
        void handleEmailChangeRequest(String email);
        void handlePasswordChangeRequest(String oldPassword, String newPassword, String newPasswordConfirmation);
    }
    
    @UiField InputElement usernameUi;
    @UiField InputElement nameUi;
    @UiField InputElement companyUi;
    @UiField TextBox emailUi;
    @UiField PasswordTextBox oldPasswordUi;
    @UiField PasswordTextBox newPasswordUi;
    @UiField PasswordTextBox newPasswordConfirmationUi;
    
    private final Presenter presenter;
    
    public UserDetails(Presenter presenter) {
        this.presenter = presenter;
        UserDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        StringMessages i18n = StringMessages.INSTANCE;
        setPlaceholder(oldPasswordUi, i18n.oldPasswordPlaceholder());
        setPlaceholder(newPasswordUi, i18n.newPasswordPlaceholder());
        setPlaceholder(newPasswordConfirmationUi, i18n.passwordRepeatPlaceholder());
    }

    public void setUserManagementContext(AuthenticationContext userManagementContext) {
        UserDTO currentUser = userManagementContext.getCurrentUser();
        usernameUi.setValue(currentUser.getName());
        nameUi.setValue(currentUser.getFullName());
        companyUi.setValue(currentUser.getCompany());
        emailUi.setValue(currentUser.getEmail());
        oldPasswordUi.setValue("");
        newPasswordUi.setValue("");
        newPasswordConfirmationUi.setValue("");
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
    @UiHandler("saveChangesUi")
    void onSaveChangesClicked(ClickEvent event) {
        presenter.handleSaveChangesRequest(nameUi.getValue(), companyUi.getValue());
    }
    
    @UiHandler("changeEmailUi")
    void onChangeEmailClicked(ClickEvent event) {
        presenter.handleEmailChangeRequest(emailUi.getValue());
    }
    
    @UiHandler("changePasswordUi")
    void onChangePasswordClicked(ClickEvent event) {
        presenter.handlePasswordChangeRequest(oldPasswordUi.getValue(), newPasswordUi.getValue(),
                newPasswordConfirmationUi.getValue());
    }

}
