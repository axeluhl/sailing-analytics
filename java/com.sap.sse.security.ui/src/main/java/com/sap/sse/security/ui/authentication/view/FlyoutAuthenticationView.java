package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface FlyoutAuthenticationView extends AuthenticationView {
    
    void setPresenter(Presenter presenter);
    
    boolean isShowing();
    
    void show();
    
    void hide();
    
    void setAutoHidePartner(IsWidget autoHidePartner);
    
    public interface Presenter {
        void onVisibilityChanged(boolean isShowing);
    }

}
