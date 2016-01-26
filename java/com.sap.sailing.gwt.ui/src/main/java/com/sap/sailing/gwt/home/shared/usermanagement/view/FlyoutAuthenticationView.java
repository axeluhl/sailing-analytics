package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.view.AuthenticationView;

public interface FlyoutAuthenticationView extends AuthenticationView {
    
    boolean isShowing();
    
    void show();
    
    void hide();
    
    void setAutoHidePartner(IsWidget autoHidePartner);

}
