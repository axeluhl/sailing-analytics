package com.sap.sse.security.ui.authentication.create;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.ErrorMessageView;

public interface CreateAccountView extends ErrorMessageView, IsWidget {
    
    void setPresenter(Presenter presenter);
    
    HasEnabled getCreateAccountControl();
    
    public interface Presenter {
        
        void onChangeEmail(String newValue);
        
        void onChangeUsername(String newValue);
        
        void onChangeFullName(String newValue);

        void onChangeLocale(String newValue);

        void onChangeCompany(String newValue);
        
        void onChangePassword(String newValue);
        
        void onChangePasswordConfirmation(String newValue);
        
        void createAccount();
        
        void signIn();
        
    }
}
