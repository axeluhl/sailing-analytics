package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.common.authentication.SailingAuthenticationEntryPointLinkFactory;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardResources.RaceBoardMainCss;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.generic.sapheader.GenericLoginHintPopup;

public class RaceBoardLoginHintPopup extends GenericLoginHintPopup {
    private static final RaceBoardMainCss style = RaceBoardResources.INSTANCE.mainCss();

    public RaceBoardLoginHintPopup(AuthenticationManager authenticationManager) {
        super(authenticationManager, SailingAuthenticationEntryPointLinkFactory.INSTANCE);
        style.ensureInjected();
        addStyleName(style.usermanagement_view());
        setStyleName(style.usermanagement_mobile(), DeviceDetector.isMobile());
        getWidget().addStyleName(style.usermanagement_view_content_wrapper());
        content.addStyleName(style.usermanagement_view_content());
    }
}
