package com.sap.sailing.gwt.home.desktop.partials.useraccountDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.app.UserManagementContext;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserAccountDetails extends Composite {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserAccountDetails> {
    }
    
    public interface Presenter {
    }
    
    @UiField AnchorElement editImageLinkUi;
    @UiField InputElement usernameUi;
    @UiField InputElement nameUi;
    @UiField TextBox emailUi;
    @UiField PasswordTextBox oldPasswordUi;
    @UiField PasswordTextBox newPasswordUi;
    @UiField PasswordTextBox newPasswordConfirmationUi;
    
    public UserAccountDetails(Presenter presenter) {
        UseraccountDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        setPlaceholder(oldPasswordUi, "TODO placeholder text");
        setPlaceholder(newPasswordUi, "TODO placeholder text");
        setPlaceholder(newPasswordConfirmationUi, "TODO placeholder text");
    }

    public void setUserManagementContext(UserManagementContext userManagementContext) {
        UserDTO currentUser = userManagementContext.getCurrentUser();
        // TODO correct message
        editImageLinkUi.setTitle("TODO picture of: " + currentUser.getName());
        // TODO use image from user when field is available
        editImageLinkUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
        
        nameUi.setValue(currentUser.getName());
        usernameUi.setValue(currentUser.getName());
        emailUi.setValue(currentUser.getEmail());
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
}
