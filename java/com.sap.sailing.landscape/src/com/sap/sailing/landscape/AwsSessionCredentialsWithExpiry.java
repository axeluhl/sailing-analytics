package com.sap.sailing.landscape;

import com.sap.sse.common.TimePoint;

public interface AwsSessionCredentialsWithExpiry {
    String getAccessKeyId();
    String getSecretAccessKey();
    String getSessionToken();
    TimePoint getExpiration();
}
