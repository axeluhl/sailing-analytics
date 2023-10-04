package com.sap.sailing.landscape.ui.client;

import com.sap.sailing.landscape.ui.client.AwsMfaLoginWidget.AwsMfaLoginListener;

public interface AwsAccessKeyProvider {
    boolean hasValidSessionCredentials();
    void addListener(AwsMfaLoginListener listener);
}
