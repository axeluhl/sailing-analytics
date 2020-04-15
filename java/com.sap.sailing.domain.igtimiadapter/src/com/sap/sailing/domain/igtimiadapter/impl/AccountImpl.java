package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.User;

public class AccountImpl implements Account {
    private static final long serialVersionUID = 1L;
    private final User user;
    private final String creatorName;
    
    public AccountImpl(String creatorName, User user) {
        super();
        this.creatorName = creatorName;
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }
    
    @Override
    public String getCreatorName() {
        return creatorName;
    }

    @Override
    public String toString() {
        return getUser().toString();
    }
}
