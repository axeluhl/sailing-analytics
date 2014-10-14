package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface StringMessages extends Messages {
    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);

    String name();

    String password();

    String passwordRepeat();

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

    String tryingToVerifyUser();

    String fetchingUserInformation();

    String signedInAs(String name);

    String close();

    String loading();

    String users();

    String addURLFilter();

    String ok();

    String cancel();

    String username();

    String email();

    String usernameMustHaveAtLeastNCharacters(int minimumUsernameLength);

    String passwordMustHaveAtLeastNCharacters(int minimumPasswordLength);

    String passwordsDontMatch();

    String couldNotCreateUser(String message);

}
