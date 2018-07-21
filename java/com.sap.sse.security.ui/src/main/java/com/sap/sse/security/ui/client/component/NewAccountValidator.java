package com.sap.sse.security.ui.client.component;

import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Encapsulates the rules for client-side username/password accounts, such as minimum username length,
 * password requirements and password/repeated password equality
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class NewAccountValidator {
    private static final int MINIMUM_USERNAME_LENGTH = 3;
    private static final int MINIMUM_PASSWORD_LENGTH = 5;
    
    protected final StringMessages stringMessages;
    
    public NewAccountValidator(com.sap.sse.security.ui.client.i18n.StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    /**
     * @return <code>null</code> if the credentials look good, or an end user-readable error message based on the
     *         {@link StringMessages} passed
     */
    public String validateUsernameAndPassword(String username, String password, String passwordRepeat) {
        String result = validateUsername(username);
        if (result == null) {
            result = validatePasswords(password, passwordRepeat);
        }
        return result;
    }

    private String validatePasswords(String password, String passwordRepeat) {
        final String result;
        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
            result = stringMessages.passwordMustHaveAtLeastNCharacters(MINIMUM_PASSWORD_LENGTH);
        } else if (!password.equals(passwordRepeat)) {
            result = stringMessages.passwordsDontMatch();
        } else {
            result = null;
        }
        return result;
    }

    protected String validateUsername(String username) {
        final String result;
        if (username == null || username.length() < MINIMUM_USERNAME_LENGTH) {
            result = stringMessages.usernameMustHaveAtLeastNCharacters(MINIMUM_USERNAME_LENGTH);
        } else {
            result = null;
        }
        return result;
    }
}
