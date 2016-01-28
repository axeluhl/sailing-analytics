package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface FlyoutAuthenticationView extends AuthenticationView {
    
    boolean isShowing();
    
    void show();
    
    void hide();
    
    void setAutoHidePartner(IsWidget autoHidePartner);

}
