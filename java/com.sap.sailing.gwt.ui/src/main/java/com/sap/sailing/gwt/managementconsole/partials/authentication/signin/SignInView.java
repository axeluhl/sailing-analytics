package com.sap.sailing.gwt.managementconsole.partials.authentication.signin;

import com.google.gwt.user.client.ui.IsWidget;

public interface SignInView extends IsWidget {

    void setPresenter(Presenter presenter);

    void clearInputs();

    void show();

    void hide();

    public interface Presenter {

        void login(String loginName, String password);

        void createAccount();

        void forgotPassword();

    }
}
