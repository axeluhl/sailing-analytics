package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.ErrorMessageView;

public interface PasswordRecoveryView extends ErrorMessageView, IsWidget {

    void setPresenter(Presenter presenter);
    
    public interface Presenter {
        
        void resetPassword(String email, String username);
    }
}
