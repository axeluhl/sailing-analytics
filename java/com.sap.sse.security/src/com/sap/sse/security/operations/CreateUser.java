package com.sap.sse.security.operations;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;

public class CreateUser implements SecurityOperation<User> {
    private static final long serialVersionUID = 516901362622767925L;
    private final String username;
    private final String email;
    private final String password;
    private final String validationBaseURL;

    public CreateUser(String username, String email, String password, String validationBaseURL) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.validationBaseURL = validationBaseURL;
    }

    @Override
    public User internalApplyTo(SecurityService toState) throws Exception {
        return toState.createSimpleUser(username, email, password, validationBaseURL);
    }
}
