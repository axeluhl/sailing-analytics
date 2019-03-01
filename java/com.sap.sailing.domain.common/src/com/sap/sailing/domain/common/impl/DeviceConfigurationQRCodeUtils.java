package com.sap.sailing.domain.common.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is used by our backend, in GWT-client code and by the Android app. Therefore we cannot use classes like
 * {@link URLEncoder} to help us with the encoding.
 */
public class DeviceConfigurationQRCodeUtils {
    public static final String deviceIdentifierKey = "identifier";
    public static final String accessTokenKey = "token";
    public static final String deviceUuidKey = "uuid";
    
    public static class DeviceConfigurationDetails {
        private final String apkUrl;
        private final String deviceIdentifier;
        private final UUID uuid;
        private final String accessToken;
        public DeviceConfigurationDetails(String apkUrl, UUID uuid, String deviceConfigurationName, String accessToken) {
            super();
            this.apkUrl = apkUrl;
            this.uuid = uuid;
            this.deviceIdentifier = deviceConfigurationName;
            this.accessToken = accessToken;
        }
        public String getApkUrl() {
            return apkUrl;
        }
        public UUID getUuid() { return uuid; }
        public String getDeviceIdentifier() { return deviceIdentifier; }
        public String getAccessToken() {
            return accessToken;
        }
    }
    
    public static interface URLDecoder {
        String decode(String encodedURL);
    }

    public static String composeQRContent(String urlEncodedDeviceConfigName, String apkUrl, String accessToken, String urlEncodedDeviceIdAsString) {
        return apkUrl + "#" + deviceIdentifierKey + "=" + urlEncodedDeviceConfigName + "&" + deviceUuidKey + "="
                + urlEncodedDeviceIdAsString + "&" + accessTokenKey + "=" + accessToken;
    }

    public static DeviceConfigurationDetails splitQRContent(String qrCodeContent, URLDecoder urlDecoder) {
        int fragmentIndex = qrCodeContent.lastIndexOf('#');
        if (fragmentIndex == -1 || fragmentIndex == 0 || qrCodeContent.length() == fragmentIndex + 1) {
            throw new IllegalArgumentException("There is no server or identifier.");
        }
        String fragment = qrCodeContent.substring(fragmentIndex + 1, qrCodeContent.length());
        final String[] params = fragment.split("&");
        final Map<String, String> paramMap = new HashMap<>();
        for (String param : params) {
            final String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], urlDecoder.decode(keyValue[1]));
            }
        }
        if (!paramMap.containsKey(deviceIdentifierKey)
        && !paramMap.containsKey(deviceUuidKey)) {
            throw new IllegalArgumentException("Device identifier parameter "+deviceIdentifierKey+
                     " and device UUID parameter "+deviceUuidKey+" are both missing from QR code contents");
        }
        String apkUrl = qrCodeContent.substring(0, fragmentIndex);
        return new DeviceConfigurationDetails(apkUrl,
                paramMap.get(deviceUuidKey) == null ? null : UUID.fromString(paramMap.get(deviceUuidKey)),
                paramMap.get(deviceIdentifierKey) == null ? null : paramMap.get(deviceIdentifierKey),
                paramMap.get(accessTokenKey));
    }

}
