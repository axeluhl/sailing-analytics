package com.sap.sailing.gwt.common.authentication;

import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.sapheader.FixedFlyoutAuthenticationView;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.client.UserService;

/**
 * Sailing-specific extension of {@link GenericAuthentication} using a {@link FixedFlyoutAuthenticationView}, which is
 * suitable in cases where the header is fixed on top of the page.
 */
public class FixedSailingAuthentication extends GenericAuthentication {
    private static final CommonSharedResources res = CommonSharedResources.INSTANCE;
    
    public FixedSailingAuthentication(UserService userService, AuthenticationMenuView userManagementMenuView) {
        super(userService, userManagementMenuView, new FixedFlyoutAuthenticationView(res),
                SailingAuthenticationEntryPointLinkFactory.INSTANCE, res);
        res.mainCss().ensureInjected();
        new FixedSailingLoginHintPopup(getAuthenticationManager());
    }
    
}
