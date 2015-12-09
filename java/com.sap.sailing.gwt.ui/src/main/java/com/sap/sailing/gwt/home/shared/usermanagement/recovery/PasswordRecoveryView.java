package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.google.gwt.user.client.ui.IsWidget;

public interface PasswordRecoveryView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    public interface Presenter {
        
        void resetPassword(String loginName);
    }
}
