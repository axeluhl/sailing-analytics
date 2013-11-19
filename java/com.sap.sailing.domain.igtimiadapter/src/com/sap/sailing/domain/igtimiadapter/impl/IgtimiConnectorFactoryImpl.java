package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectorFactory;

public class IgtimiConnectorFactoryImpl implements IgtimiConnectorFactory {
    private final Map<String, String> codeAndAccessTokens;
    
    public IgtimiConnectorFactoryImpl() {
        this.codeAndAccessTokens = new HashMap<>();
    }
    
    @Override
    public void storeCodeAndAccessToken(String code, String accessToken) {
        codeAndAccessTokens.put(code, accessToken);
    }
}
