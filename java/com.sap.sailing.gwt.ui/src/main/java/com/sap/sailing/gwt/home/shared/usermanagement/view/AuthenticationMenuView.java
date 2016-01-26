package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface AuthenticationMenuView extends IsWidget {
    
    void setPresenter(Presenter presenter);
    
    void setAuthenticated(boolean authenticated);
    
    public interface Presenter {
        void toggleFlyout();
    }

}
