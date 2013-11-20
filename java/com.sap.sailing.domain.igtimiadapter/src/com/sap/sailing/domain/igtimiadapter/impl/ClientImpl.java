package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Client;

public class ClientImpl implements Client {
    private final String id;
    private final String secret;
    
    public ClientImpl(String id, String secret) {
        super();
        this.id = id;
        this.secret = secret;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSecret() {
        return secret;
    }

}
