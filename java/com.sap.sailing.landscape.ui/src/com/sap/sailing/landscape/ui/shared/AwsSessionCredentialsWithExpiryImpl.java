package com.sap.sailing.landscape.ui.shared;

import com.sap.sse.common.TimePoint;

public class AwsSessionCredentialsWithExpiryImpl implements AwsSessionCredentialsWithExpiry {
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final TimePoint expiration;

    public AwsSessionCredentialsWithExpiryImpl(String accessKeyId, String secretAccessKey, String sessionToken,
            TimePoint expiration) {
        super();
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expiration = expiration;
    }

    @Override
    public String getAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    @Override
    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public TimePoint getExpiration() {
        return expiration;
    }
}
