package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface StringMessages extends Messages {
    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);

    String name();

    String password();

    String loggedIn();

    String signIn();

    String invalidUsername();

    String signUp();

    String signOut();

    String couldNotSignOut(String message);

    String createUser();

    String settings();

    String filterUsers();

    String welcome(String name);
}
