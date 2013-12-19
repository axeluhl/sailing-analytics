package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.User;

public class AccountImpl implements Account {
    private final User user;
    
    public AccountImpl(User user) {
        super();
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return getUser().toString();
    }
}
