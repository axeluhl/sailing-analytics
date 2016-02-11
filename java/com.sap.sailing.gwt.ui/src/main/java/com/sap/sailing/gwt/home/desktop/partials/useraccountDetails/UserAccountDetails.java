package com.sap.sailing.gwt.home.desktop.partials.useraccountDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.userdetails.AbstractUserDetails;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserAccountDetails extends AbstractUserDetails {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserAccountDetails> {
    }
    
    @UiField DivElement editImageLinkUi;
    
    public UserAccountDetails() {
        UseraccountDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setUser(UserDTO currentUser) {
        super.setUser(currentUser);
        // TODO correct message
        editImageLinkUi.setTitle("TODO picture of: " + currentUser.getName());
        // TODO use image from user when field is available
        editImageLinkUi.getStyle().setBackgroundImage("url(images/home/userdefault.svg)");
    }
}
