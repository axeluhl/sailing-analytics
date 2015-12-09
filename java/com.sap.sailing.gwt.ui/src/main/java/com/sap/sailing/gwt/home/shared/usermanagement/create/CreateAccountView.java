package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.user.client.ui.IsWidget;

public interface CreateAccountView extends IsWidget {
    
    void setPresenter(Presenter presenter);

    void setErrorMessage(String message);
    
    public interface Presenter {
        
        void createAccount(String username, String email, String password, String passwordConfirmation);
        
        void signIn();
        
    }


}
