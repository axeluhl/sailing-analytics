package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementContext;

public interface LoggedInUserInfoView extends IsWidget {

    void setPresenter(Presenter presenter);
    void setUserInfo(UserManagementContext userManagementContext);

    public interface Presenter {
        void gotoProfileUi();
        void signOut();
    }
}
