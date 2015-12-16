package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.user.client.ui.Widget;

public interface PasswordResetView {

    Widget asWidget();

    void setPresenter(Presenter currentPresenter);

    public interface Presenter {
        void resetPassword(String newPassword);
    }

}

