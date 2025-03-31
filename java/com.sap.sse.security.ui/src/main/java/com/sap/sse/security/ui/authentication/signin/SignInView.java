package com.sap.sse.security.ui.authentication.signin;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.ErrorMessageView;

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
