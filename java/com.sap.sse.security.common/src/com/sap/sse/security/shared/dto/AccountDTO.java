package com.sap.sse.security.shared.dto;

import java.io.Serializable;

public abstract class AccountDTO implements Serializable {
    private static final long serialVersionUID = 1L;
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
