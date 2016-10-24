package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.ErrorMessageView;

public interface PasswordResetView extends ErrorMessageView, IsWidget {

    void setPresenter(Presenter currentPresenter);
    
    HasEnabled getChangePasswordControl();

    public interface Presenter {
        
        void onChangePassword(String newValue);
        
        void onChangePasswordConfirmation(String newValue);
        
        void resetPassword();
    }

}

