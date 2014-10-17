package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;

public interface StringMessages extends com.sap.sse.gwt.client.StringMessages {
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

    String changePassword();

    String userDetails();

    String deleteUser();

    String deleteUserQuestion();

    String doYouReallyWantToDeleteUser(String username);

    String errorDeletingUser();

    String account(String accountType);
    
    String save();

    String remove();

    String add();

    String edit();

    String errorUpdatingRoles(String username, String message);

    String enterRoleName();

    String errorCreatingUser(String username, String message);

    String signedUpSuccessfully(String username);

    String unknownErrorCreatingUser(String username);

    String userAlreadyExists(String text);

    String currentPassword();

    String passwordSuccessfullyChanged();

    String passwordDoesNotMeetRequirements();

    String errorChangingPassword(String message);
    
}
