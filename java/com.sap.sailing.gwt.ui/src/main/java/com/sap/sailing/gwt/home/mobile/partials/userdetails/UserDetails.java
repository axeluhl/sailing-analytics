package com.sap.sailing.gwt.home.mobile.partials.userdetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class UserDetails extends Composite {

    interface MyUiBinder extends UiBinder<Widget, UserDetails> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    
    public UserDetails() {
        UserDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
