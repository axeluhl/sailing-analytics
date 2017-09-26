package com.sap.sailing.gwt.common.authentication;

import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.generic.sapheader.FixedLoginHintPopup;

/**
 * Sailing specific version of {@link FixedLoginHintPopup}.
 */
public class FixedSailingLoginHintPopup extends FixedLoginHintPopup {

    public FixedSailingLoginHintPopup(AuthenticationManager authenticationManager) {
        super(authenticationManager, SailingAuthenticationEntryPointLinkFactory.INSTANCE);
    }

}
