package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.ErrorMessageView;

public interface CreateAccountView extends ErrorMessageView, IsWidget {
    
    void setPresenter(Presenter presenter);
    
    public interface Presenter {
        
        void createAccount(String username, String fullName, String company, 
                String email, String password, String passwordConfirmation);
        
        void signIn();
        
        public interface Callback {
            
            String getCreateConfirmationUrl();
        }
    }
}
