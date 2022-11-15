package com.sap.sse.security.ui.userprofile.desktop.userdetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.authentication.resource.SharedAuthenticationResources;
import com.sap.sse.security.ui.userprofile.shared.userdetails.AbstractUserDetails;

/**
 * Desktop implementation of {@link AbstractUserDetails}.
 */
public class UserAccountDetails extends AbstractUserDetails {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserAccountDetails> {
    }
    
    @UiField DivElement editImageLinkUi;
    
    @UiField(provided = true) final CommonSharedResources res;
    
    public UserAccountDetails(CommonSharedResources res) {
        this.res = res;
        UseraccountDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setUser(UserDTO currentUser) {
        super.setUser(currentUser);
        // TODO correct message
//        editImageLinkUi.setTitle("TODO picture of: " + currentUser.getName());
        // TODO use image from user when field is available
        editImageLinkUi.getStyle().setBackgroundImage("url('" + SharedAuthenticationResources.INSTANCE.userdefault().getSafeUri().asString() + "')");
    }
}
