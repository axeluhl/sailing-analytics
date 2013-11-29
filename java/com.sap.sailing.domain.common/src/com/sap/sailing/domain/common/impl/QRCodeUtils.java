package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.impl.Util.Pair;

public class QRCodeUtils {

    public static String composeQRContent(String deviceIdentifier, String serverUrl) {
        if (deviceIdentifier.contains(";")) {
            throw new IllegalArgumentException("Identifier must not contain ';'!");
        }
        return deviceIdentifier + ";" + serverUrl;
    }
    
    public static Pair<String, String> splitQRContent(String qrCodeContent) {
        if (!qrCodeContent.contains(";")) {
            throw new IllegalArgumentException("QRCode must contain ';'!");
        }
        String deviceIdentifier = qrCodeContent.substring(0, qrCodeContent.indexOf(';'));
        String serverUrl = qrCodeContent.substring(deviceIdentifier.length() + 1);
        return new Pair<String, String>(deviceIdentifier, serverUrl);
    }
    
}
