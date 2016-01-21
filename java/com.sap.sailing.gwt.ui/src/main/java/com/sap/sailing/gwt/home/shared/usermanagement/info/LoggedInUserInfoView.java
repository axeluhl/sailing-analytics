package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.app.AuthenticationContext;

public interface LoggedInUserInfoView extends IsWidget {

    void setPresenter(Presenter presenter);
    void setUserInfo(AuthenticationContext userManagementContext);

    public interface Presenter {
        void gotoProfileUi();
        void signOut();
        
        public interface Callback {
            void handleUserProfileNavigation();
        }
    }
}
