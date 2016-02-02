package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface AuthenticationMenuView extends IsWidget {
    
    void setPresenter(Presenter presenter);
    
    void setAuthenticated(boolean authenticated);
    
    void setOpen(boolean open);
    
    public interface Presenter {
        void toggleFlyout();
    }

}
