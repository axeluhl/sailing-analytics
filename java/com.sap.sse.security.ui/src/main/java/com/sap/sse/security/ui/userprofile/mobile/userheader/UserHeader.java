package com.sap.sse.security.ui.userprofile.mobile.userheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.userprofile.shared.userheader.AbstractUserHeader;

/**
 * Mobile (smartphone) implementation of {@link AbstractUserHeader}.
 */
public class UserHeader extends AbstractUserHeader {
    interface MyUiBinder extends UiBinder<Widget, UserHeader> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField(provided = true) final CommonSharedResources res;
    
    public UserHeader(CommonSharedResources res) {
        this.res = res;
        UserHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
