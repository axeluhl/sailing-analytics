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
        this(applicationName, pageTitle, true);
    }
    
    public SAPHeaderWithAuthentication(String applicationName, Widget pageTitle, boolean withLogin) {
        super(applicationName, pageTitle);
        res.css().ensureInjected();
        
        FlowPanel rightWithAuthentication = new FlowPanel();
        rightWithAuthentication.addStyleName(res.css().header_right_wrapper());
        rightWithAuthentication.setStyleName(res.css().with_login(), withLogin);
        
        Anchor authenticationMenu = new Anchor();
        authenticationMenu.addStyleName(res.css().usermanagement_icon());
        rightWithAuthentication.add(authenticationMenu);
        authenticationMenuView = new AuthenticationMenuViewImpl(authenticationMenu, res.css().usermanagement_loggedin(), res.css().usermanagement_open());
        
        rightWrapper = new SimplePanel();
        rightWrapper.addStyleName(res.css().header_right_extension());
        rightWithAuthentication.add(rightWrapper);
        
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
