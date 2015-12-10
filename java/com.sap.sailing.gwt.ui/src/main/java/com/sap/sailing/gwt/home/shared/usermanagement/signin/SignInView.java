package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.ErrorMessageView;

public interface SignInView extends ErrorMessageView, IsWidget {

    void setPresenter(Presenter presenter);
    
    public interface Presenter {
        
        void login(String loginName, String password);
        
        void createAccount();
        
        void forgotPassword();
        
        void loginWithFacebook();
        
        void loginWithGoogle();
    }

}
