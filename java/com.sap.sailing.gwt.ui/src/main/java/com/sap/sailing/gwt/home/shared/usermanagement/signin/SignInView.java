package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.user.client.ui.IsWidget;

public interface SignInView extends IsWidget {

    void setPresenter(Presenter presenter);
    
    void setErrorMessage(String errorMessage);

    public interface Presenter {
        
        void login(String loginName, String password);
        
        void createAccount();
        
        void forgotPassword();
        
        void loginWithFacebook();
        
        void loginWithGoogle();
    }

}
