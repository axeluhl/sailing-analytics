package com.sap.sailing.gwt.common.authentication;

import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.view.AbstractFlyoutAuthenticationView;

public class GenericSailingAuthenticationView extends AbstractFlyoutAuthenticationView {
    private static final SAPHeaderWithAuthenticationResources res = SAPHeaderWithAuthenticationResources.INSTANCE;

    public GenericSailingAuthenticationView(CommonSharedResources resources, boolean fixedPositioning) {
        super(resources);
        res.css().ensureInjected();
        
        popupPanel.setStyleName(res.css().usermanagement_view());
        popupPanel.setStyleName(res.css().fixed(), fixedPositioning);
    }
    
    public void show() {
        popupPanel.show();
        getPresenter().onVisibilityChanged(true);
    }
}
