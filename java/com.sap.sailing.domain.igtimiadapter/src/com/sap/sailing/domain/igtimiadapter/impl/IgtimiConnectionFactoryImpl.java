package com.sap.sailing.domain.igtimiadapter.impl;

import java.net.URL;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

public class IgtimiConnectionFactoryImpl implements IgtimiConnectionFactory {
    private final URL baseUrl;
    
    /**
     * Used when {@code null} is passed to {@link #createConnection(String)} as a bearer token. If this field is
     * {@code null}, too, requests to the REST API will be made through the connections returned from
     * {@link #createConnection(String)} without an authenticated user.
     */
    private final String defaultBearerToken;

    /**
     * @param baseUrl
     *            base URL of the service where Igtimi wind data can be requested from; example:
     *            {@code https://wind.sapsailing.com}
     * @param defaultBearerToken if no explicit 
     */
    public IgtimiConnectionFactoryImpl(URL baseUrl, String defaultBearerToken) {
        this.baseUrl = baseUrl;
        this.defaultBearerToken = defaultBearerToken;
    }

    @Override
    public URL getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean hasCredentials() {
        return defaultBearerToken != null;
    }
    
    @Override
    public IgtimiConnection createConnection(String bearerToken) {
        return new IgtimiConnectionImpl(baseUrl, bearerToken);
    }
    
    @Override
    public IgtimiConnection createConnection() {
        return new IgtimiConnectionImpl(baseUrl, defaultBearerToken);
    }
}
