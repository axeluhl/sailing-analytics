package com.sap.sse.security.ui.userprofile.mobile.userdetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.userprofile.shared.userdetails.AbstractUserDetails;

/**
 * Mobile (smartphone) implementation of {@link AbstractUserDetails}.
 */
public class UserDetails extends AbstractUserDetails {

    interface MyUiBinder extends UiBinder<Widget, UserDetails> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField(provided = true) final CommonSharedResources res;
    
    public UserDetails(CommonSharedResources res) {
        this.res = res;
        UserDetailsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
