package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.shared.UserDTO;

public interface LoggedInUserInfoView extends IsWidget {

    void setPresenter(Presenter presenter);
    void setUserInfo(UserDTO user);

    public interface Presenter {
        void gotoProfileUi();
        void signOut();
    }
}
