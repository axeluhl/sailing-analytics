package com.sap.sailing.gwt.home.mobile.partials.userdetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.userdetails.AbstractUserDetails;

public class UserDetails extends AbstractUserDetails {

    interface MyUiBinder extends UiBinder<Widget, UserDetails> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    public UserDetails() {
        UserDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
