package com.sap.sse.security.operations;

import com.sap.sse.security.impl.ReplicableSecurityService;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.impl.User;

public class CreateUserOperation implements SecurityOperation<User> {
    private static final long serialVersionUID = 6433926741696643390L;
    protected final String username;
    protected final String email;
    protected final Account[] accounts;

    public CreateUserOperation(String username, String email, Account... accounts) {
        this.username = username;
        this.email = email;
        this.accounts = accounts;
    }

    @Override
    public User internalApplyTo(ReplicableSecurityService toState) throws Exception {
        return toState.internalCreateUser(username, email, accounts);
    }
}
