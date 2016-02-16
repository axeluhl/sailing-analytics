package com.sap.sailing.gwt.common.authentication;

import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthenticationResources;
import com.sap.sse.security.ui.authentication.view.AbstractFlyoutAuthenticationView;

public class GenericSailingAuthenticationView extends AbstractFlyoutAuthenticationView {
    protected static final SAPHeaderWithAuthenticationResources res = SAPHeaderWithAuthenticationResources.INSTANCE;

    public GenericSailingAuthenticationView(CommonSharedResources resources) {
        super(resources);
        res.css().ensureInjected();
        
        popupPanel.setStyleName(res.css().usermanagement_view());
    }
    
    public void show() {
        popupPanel.show();
        getPresenter().onVisibilityChanged(true);
    }
}
