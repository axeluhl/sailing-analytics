package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.Client;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;

public class IgtimiConnectionImpl implements IgtimiConnection {
    private final Client client;
    private final Account account;
    
    public IgtimiConnectionImpl(Client client, Account account) {
        this.client = client;
        this.account = account;
    }

}
