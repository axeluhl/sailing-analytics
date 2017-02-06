package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Client;

public class ClientImpl implements Client {
    private final String id;
    private final String secret;
    private final String defaultRedirectProtocol;
    private final String defaultRedirectHostname;
    private final String defaultRedirectPort;
    private final String redirectUriPath;
    
    public ClientImpl(String id, String secret, String defaultRedirectProtocol, String defaultRedirectHostname, String defaultRedirectPort, String redirectUriPath) {
        super();
        this.id = id;
        this.secret = secret;
        this.defaultRedirectProtocol = defaultRedirectProtocol;
        this.defaultRedirectHostname = defaultRedirectHostname;
        this.defaultRedirectPort = defaultRedirectPort;
        this.redirectUriPath = redirectUriPath;
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
    public String getDefaultRedirectUri() {
        return getRedirectUri(defaultRedirectProtocol, defaultRedirectHostname, defaultRedirectPort);
    }

    @Override
    public String getRedirectUri(String redirectProtocol, String redirectHost, String redirectPort) {
        return redirectProtocol+"://"+redirectHost+(redirectPort==null||redirectPort.isEmpty()?"":(":"+redirectPort))+
                (redirectUriPath.startsWith("/")?redirectUriPath:"/"+redirectUriPath);
    }
}
