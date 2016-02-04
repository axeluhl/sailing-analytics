package com.sap.sailing.gwt.common.authentication;

import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.client.UserService;

public class FixedSailingAuthentication extends GenericAuthentication {
    private static final CommonSharedResources res = CommonSharedResources.INSTANCE;
    
    public FixedSailingAuthentication(UserService userService, AuthenticationMenuView userManagementMenuView) {
        super(userService, userManagementMenuView, new FixedSailingAuthenticationView(res), res);
        res.mainCss().ensureInjected();
    }
    
}
