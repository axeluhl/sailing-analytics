package com.sap.sailing.gwt.common.authentication;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.theme.component.sapheader2.SAPHeader2;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;
import com.sap.sse.security.ui.authentication.view.AuthenticationMenuViewImpl;

public class SAPHeaderWithAuthentication extends SAPHeader2 {
    private static final SAPHeaderWithAuthenticationResources res = SAPHeaderWithAuthenticationResources.INSTANCE;
    
    private final SimplePanel rightWrapper;

    private AuthenticationMenuView authenticationMenuView;

    public SAPHeaderWithAuthentication(String applicationName, Widget pageTitle) {
        super(applicationName, pageTitle);
        res.css().ensureInjected();
        
        FlowPanel rightWithAuthentication = new FlowPanel();
        rightWrapper = new SimplePanel();
        rightWrapper.addStyleName(res.css().header_right_wrapper());
        rightWithAuthentication.add(rightWrapper);
        
        Anchor authenticationMenu = new Anchor();
        authenticationMenu.addStyleName(res.css().usermanagement_icon());
        rightWithAuthentication.add(authenticationMenu);
        authenticationMenuView = new AuthenticationMenuViewImpl(authenticationMenu, res.css().usermanagement_loggedin(), res.css().usermanagement_open());
        
        super.addWidgetToRightSide(rightWithAuthentication);
    }

    @Override
    public void addWidgetToRightSide(Widget widget) {
        rightWrapper.add(widget);
    }
    
    public AuthenticationMenuView getAuthenticationMenuView() {
        return authenticationMenuView;
    }
}
