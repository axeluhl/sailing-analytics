package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.impl.IgtimiConnectorFactoryImpl;

public interface IgtimiConnectorFactory {
    static IgtimiConnectorFactory INSTANCE = new IgtimiConnectorFactoryImpl();

    void storeCodeAndAccessToken(String code, String accessToken);
}
