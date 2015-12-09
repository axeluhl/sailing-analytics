package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.user.client.ui.IsWidget;

public interface LoggedInUserInfoView extends IsWidget {

    void setPresenter(Presenter presenter);

    public interface Presenter {
        void gotoProfileUi();
        void signOut();

    }

}
