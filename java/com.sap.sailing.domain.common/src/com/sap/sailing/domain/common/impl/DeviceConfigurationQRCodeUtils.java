package com.sap.sailing.domain.common.impl;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is used by our backend, in GWT-client code and by the Android app. Therefore we cannot use classes like
 * {@link URLEncoder} to help us with the encoding.<p>
 * 
 * Example URL:<pre>
 * https://racemanager-app.sapsailing.com/invite?server_url=https://dev.sapsailing.com/&device_config_identifier=D-Labs+Test&device_config_uuid=6fb0e2e3-23e6-4d2e-b66a-77d98feb7fdd&token=KY5G48LdOiTysCCiRfvFIVO87Ljbfzo3a4%2Bal63H%2Fvw%3D&event_id=8c3bdd16-a3d1-4a38-bf82-29d98ce18bec&course_area_uuid=fe96623a-d570-4dc5-8cc4-7fe8c5e7e2c0&priority=2
 * </pre>
 */
public class DeviceConfigurationQRCodeUtils {
    private static final String BASE_INVITATION_URL = "https://racemanager-app.sapsailing.com/invite";
    public static final String serverUrl = "server_url";
    public static final String eventId = "event_id";
    public static final String courseAreaId = "course_area_uuid";
    public static final String priority = "priority";
    public static final String deviceIdentifierKey = "device_config_identifier";
    public static final String deviceUuidKey = "device_config_uuid";
    public static final String accessTokenKey = "token";

    public static class DeviceConfigurationDetails {
        private final String url;
        private final String deviceIdentifier;
        private final UUID uuid;
        private final String accessToken;

        public DeviceConfigurationDetails(String url, UUID uuid, String deviceConfigurationName, String accessToken) {
            super();
            this.url = url;
            this.uuid = uuid;
            this.deviceIdentifier = deviceConfigurationName;
            this.accessToken = accessToken;
        }

        public String getUrl() {
            return url;
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

    public static String composeQRContent(String serverUrlWithoutFinalSlash, String urlEncodedDeviceConfigName,
            String urlEncodedDeviceIdAsString, UUID eventId, UUID courseAreaId, Integer priority,
            String accessToken) {
        return BASE_INVITATION_URL
                + "?"+serverUrl+"="+serverUrlWithoutFinalSlash
                + "&" + deviceIdentifierKey + "=" + urlEncodedDeviceConfigName
                + "&" + deviceUuidKey + "=" + urlEncodedDeviceIdAsString
                + (eventId != null ? ("&" + DeviceConfigurationQRCodeUtils.eventId + "=" + eventId) : "")
                + (courseAreaId != null ? ("&" + DeviceConfigurationQRCodeUtils.courseAreaId + "=" + courseAreaId) : "")
                + (priority != null ? ("&" + DeviceConfigurationQRCodeUtils.priority + "=" + priority) : "")
                + (accessToken != null ? ("&" + accessTokenKey + "=" + accessToken) : "");
    }

    public static DeviceConfigurationDetails splitQRContent(String qrCodeContent, URLDecoder urlDecoder) {
        int fragmentIndex = qrCodeContent.lastIndexOf('#');
        if (fragmentIndex == -1 || fragmentIndex == 0 || qrCodeContent.length() == fragmentIndex + 1) {
            throw new IllegalArgumentException("There is no server or identifier.");
        }
        String fragment = qrCodeContent.substring(fragmentIndex + 1);
        final String[] params = fragment.split("&");
        final Map<String, String> paramMap = new HashMap<>();
        for (String param : params) {
            final String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], urlDecoder.decode(keyValue[1]));
            }
        }
        if (!paramMap.containsKey(deviceIdentifierKey) && !paramMap.containsKey(deviceUuidKey)) {
            throw new IllegalArgumentException("Device identifier parameter " + deviceIdentifierKey
                    + " and device UUID parameter " + deviceUuidKey + " are both missing from QR code contents");
        }
        String apkUrl = qrCodeContent.substring(0, fragmentIndex);
        return new DeviceConfigurationDetails(apkUrl,
                paramMap.get(deviceUuidKey) == null ? null : UUID.fromString(paramMap.get(deviceUuidKey)),
                paramMap.get(deviceIdentifierKey) == null ? null : paramMap.get(deviceIdentifierKey),
                paramMap.get(accessTokenKey));
    }

}
