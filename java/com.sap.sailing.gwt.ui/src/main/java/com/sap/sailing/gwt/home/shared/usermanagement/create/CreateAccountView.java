package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.ErrorMessageView;

public interface CreateAccountView extends ErrorMessageView, IsWidget {
    
    void setPresenter(Presenter presenter);
    
    HasEnabled getCreateAccountControl();
    
    public interface Presenter {
        
        boolean validate(String username, String password, String passwordConfirmation);
        
        void createAccount(String username, String fullName, String company, 
                String email, String password, String passwordConfirmation);
        
        void signIn();
        
        public interface Callback {
            
            String getCreateConfirmationUrl();
        }
    }
}
