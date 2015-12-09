package com.sap.sailing.gwt.home.desktop.partials.useraccountDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
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
    @UiField InputElement emailUi;
    
    public UserAccountDetails(Presenter presenter) {
        UseraccountDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
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
}
