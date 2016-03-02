package com.sap.sailing.domain.common.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by our backend, in GWT-client code and by the Android app. Therefore we cannot use classes like
 * {@link URLEncoder} to help us with the encoding.
 */
public class DeviceConfigurationQRCodeUtils {
    public static final String deviceIdentifierKey = "identifier";
    public static final String accessTokenKey = "token";
    
    public static class DeviceConfigurationDetails {
        private final String apkUrl;
        private final String deviceIdentifier;
        private final String accessToken;
        public DeviceConfigurationDetails(String apkUrl, String deviceIdentifier, String accessToken) {
            super();
            this.apkUrl = apkUrl;
            this.deviceIdentifier = deviceIdentifier;
            this.accessToken = accessToken;
        }
        public String getApkUrl() {
            return apkUrl;
        }
        public String getDeviceIdentifier() {
            return deviceIdentifier;
        }
        public String getAccessToken() {
            return accessToken;
        }
    }

    public static String composeQRContent(String deviceIdentifier, String apkUrl, String accessToken) {
        // poor man's uri fragment encoding: ' ' as '%20'
        String encodedIdentifier = deviceIdentifier.replaceAll(" ", "%20");
        return apkUrl + "#" + deviceIdentifierKey + "=" + encodedIdentifier+"&"+accessTokenKey + "=" + accessToken;
    }

    public static DeviceConfigurationDetails splitQRContent(String qrCodeContent) {
        int fragmentIndex = qrCodeContent.lastIndexOf('#');
        if (fragmentIndex == -1 || fragmentIndex == 0 || qrCodeContent.length() == fragmentIndex + 1) {
            throw new IllegalArgumentException("There is no server or identifier.");
        }
        String fragment = qrCodeContent.substring(fragmentIndex + 1, qrCodeContent.length());
        final String[] params = fragment.split("\\?");
        final Map<String, String> paramMap = new HashMap<>();
        for (String param : params) {
            final String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1].replaceAll("%20", " "));
            }
        }
        if (!fragment.startsWith(deviceIdentifierKey + "=")) {
            throw new IllegalArgumentException("The identifier is malformed");
        }
        String apkUrl = qrCodeContent.substring(0, fragmentIndex);
        return new DeviceConfigurationDetails(apkUrl, paramMap.get(deviceIdentifierKey), paramMap.get(accessTokenKey));
    }

}
