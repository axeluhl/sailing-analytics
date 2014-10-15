package com.sap.sailing.domain.common.impl;

import java.net.URLEncoder;

import com.sap.sse.common.Util;

/**
 * This class is used by our backend, in GWT-client code and by the Android app. Therefore we cannot use classes like
 * {@link URLEncoder} to help us with the encoding.
 */
public class DeviceConfigurationQRCodeUtils {

    private static final String fragmentKey = "identifier";

    public static String composeQRContent(String deviceIdentifier, String apkUrl) {
        // poor man's uri fragment encoding: ' ' as '%20'
        String encodedIdentifier = deviceIdentifier.replaceAll(" ", "%20");
        return apkUrl + "#" + fragmentKey + "=" + encodedIdentifier;
    }

    public static Util.Pair<String, String> splitQRContent(String qrCodeContent) {
        int fragmentIndex = qrCodeContent.lastIndexOf('#');
        if (fragmentIndex == -1 || fragmentIndex == 0 || qrCodeContent.length() == fragmentIndex + 1) {
            throw new IllegalArgumentException("There is no server or identifier.");
        }
        String deviceIdentifier = qrCodeContent.substring(fragmentIndex + 1, qrCodeContent.length());
        if (!deviceIdentifier.startsWith(fragmentKey + "=")) {
            throw new IllegalArgumentException("The identifier is malformed");
        }
        deviceIdentifier = deviceIdentifier.substring((fragmentKey + "=").length());
        deviceIdentifier = deviceIdentifier.replaceAll("%20", " ");

        String apkUrl = qrCodeContent.substring(0, fragmentIndex);
        return new Util.Pair<String, String>(deviceIdentifier, apkUrl);
    }

}
