package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Client;

public class ClientImpl implements Client {
    private final String id;
    private final String secret;
    private final String redirectUri;
    
    public ClientImpl(String id, String secret, String redirectUri) {
        super();
        this.id = id;
        this.secret = secret;
        this.redirectUri = redirectUri;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }
}
