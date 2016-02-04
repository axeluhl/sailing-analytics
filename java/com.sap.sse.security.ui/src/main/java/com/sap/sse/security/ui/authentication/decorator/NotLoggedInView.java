package com.sap.sse.security.ui.authentication.decorator;

import com.google.gwt.user.client.ui.IsWidget;

public interface NotLoggedInView extends IsWidget {

    void setPresenter(NotLoggedInPresenter presenter);

    void setMessage(String message);

    void setSignInText(String signInText);
}
