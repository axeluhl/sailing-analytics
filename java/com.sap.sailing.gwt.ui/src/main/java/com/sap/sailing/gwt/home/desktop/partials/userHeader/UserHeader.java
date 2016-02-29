package com.sap.sailing.gwt.home.desktop.partials.userHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.userheader.AbstractUserHeader;
import com.sap.sse.gwt.common.CommonSharedResources;

/**
 * Desktop implementation of {@link AbstractUserHeader}.
 */
public class UserHeader extends AbstractUserHeader {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, UserHeader> {
    }
    
    @UiField(provided = true) final CommonSharedResources res;
    
    public UserHeader(CommonSharedResources res) {
        this.res = res;
        UserHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
