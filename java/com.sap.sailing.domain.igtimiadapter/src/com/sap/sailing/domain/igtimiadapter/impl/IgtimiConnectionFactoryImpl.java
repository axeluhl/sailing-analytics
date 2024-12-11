package com.sap.sailing.domain.igtimiadapter.impl;

import java.net.URL;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

public class IgtimiConnectionFactoryImpl implements IgtimiConnectionFactory {
    public IgtimiConnectionFactoryImpl() {
    }

    @Override
    public IgtimiConnection createConnection(URL baseUrl, String bearerToken) {
        return new IgtimiConnectionImpl(baseUrl, bearerToken);
    }
}
