package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.user.client.ui.IsWidget;

public interface PasswordResetView extends IsWidget {

    void setPresenter(Presenter currentPresenter);

    public interface Presenter {
        void resetPassword(String newPassword, String newPasswordConfirmation);
    }

}

