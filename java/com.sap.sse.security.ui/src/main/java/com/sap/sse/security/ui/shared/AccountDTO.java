package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class AccountDTO implements IsSerializable {

    private String accountType;

    AccountDTO() {} // for serialization only
    
    public AccountDTO(String accountType) {
        super();
        this.accountType = accountType;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
