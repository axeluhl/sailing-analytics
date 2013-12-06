package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.impl.Util.Pair;

public class QRCodeUtils {

    private static final String fragmentKey = "identifier";

    public static String composeQRContent(String deviceIdentifier, String apkUrl) {
        // poor man's uri fragment encoding: ' ' as '+'
        String encodedIdentifier = deviceIdentifier.replaceAll(" ", "%20");
        return apkUrl + "#" + fragmentKey + "=" + encodedIdentifier;
    }

    public static Pair<String, String> splitQRContent(String qrCodeContent) {
        int index = qrCodeContent.lastIndexOf('#');
        if (index == -1 || index == 0 || qrCodeContent.length() == index + 1) {
            throw new IllegalArgumentException("There is no server or identifier.");
        }
        String deviceIdentifier = qrCodeContent.substring(qrCodeContent.lastIndexOf('#') + 1, qrCodeContent.length());
        if (!deviceIdentifier.startsWith(fragmentKey + "=")) {
            throw new IllegalArgumentException("The identifier is malformed");
        }
        deviceIdentifier = deviceIdentifier.substring((fragmentKey + "=").length());
        deviceIdentifier = deviceIdentifier.replaceAll("%20", " ");

        String apkUrl = qrCodeContent.substring(0, qrCodeContent.lastIndexOf('#'));
        return new Pair<String, String>(deviceIdentifier, apkUrl);
    }

}
