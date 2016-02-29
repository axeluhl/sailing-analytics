package com.sap.sailing.gwt.home.mobile.partials.userheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.userheader.AbstractUserHeader;
import com.sap.sse.gwt.common.CommonSharedResources;

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
