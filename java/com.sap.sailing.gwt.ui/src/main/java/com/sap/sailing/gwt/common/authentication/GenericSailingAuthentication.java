package com.sap.sailing.gwt.common.authentication;

import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.sapheader.GenericFlyoutAuthenticationView;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.client.UserService;

/**
 * Sailing-specific extension of {@link GenericAuthentication} using a {@link GenericFlyoutAuthenticationView}, which is
 * suitable in cases where the header scrolls with the page.
 */
public class GenericSailingAuthentication extends GenericAuthentication {
    private static final CommonSharedResources res = CommonSharedResources.INSTANCE;
    
    public GenericSailingAuthentication(UserService userService, AuthenticationMenuView userManagementMenuView) {
        super(userService, userManagementMenuView, new GenericFlyoutAuthenticationView(res),
                SailingAuthenticationEntryPointLinkFactory.INSTANCE, res);
        res.mainCss().ensureInjected();
    }
    
}
