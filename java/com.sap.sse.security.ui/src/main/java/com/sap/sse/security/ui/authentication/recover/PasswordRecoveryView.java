package com.sap.sse.security.ui.authentication.recover;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.ErrorMessageView;

public interface PasswordRecoveryView extends ErrorMessageView, IsWidget {

    void setPresenter(Presenter presenter);
    
    public interface Presenter {
        
        void resetPassword(String email, String username);
        
    }
}
