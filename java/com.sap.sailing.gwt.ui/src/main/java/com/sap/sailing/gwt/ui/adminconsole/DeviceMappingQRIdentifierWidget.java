package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.http.client.URL;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.QRCodeComposite;

public class DeviceMappingQRIdentifierWidget extends BaseQRIdentifierWidget {
    
    public static final String apkPath = "/apps/com.sap.sailing.android.tracking.app.apk";
    
    private static final int qrCodeSize = 320;
    private static final int errorCorrectionLevel = QRCodeComposite.ERROR_CORRECTION_LEVEL_L;
    
    private final String leaderboard;
    private final String raceColumn;
    private final String fleet;
    
    private Long fromMillis;
    private Long toMillis;
    private String mappedItemId;
    private String mappedItemType;

    public DeviceMappingQRIdentifierWidget(String leaderboard, String raceColumn, String fleet,
            StringMessages stringMessages) {
        super(qrCodeSize, stringMessages, errorCorrectionLevel);
        
        this.leaderboard = URL.encode(leaderboard);
        this.raceColumn = URL.encode(raceColumn);
        this.fleet = URL.encode(fleet);
    }
    
    /**
     * @param mappedItemType one of the param names in {@link DeviceMappingConstants}
     */
    public void setMappedItem(String mappedItemType, String mappedItemIdAsString) {
        this.mappedItemId = URL.encode(mappedItemIdAsString);
        this.mappedItemType = mappedItemType;
        generateQRCode();
    }
    
    public void setFromMillis(long fromMillis) {
        this.fromMillis = fromMillis;
        generateQRCode();
    }
    
    public void setToMillis(long toMillis) {
        this.toMillis = toMillis;
        generateQRCode();
    }
    
    @Override
    protected String generateEncodedQRCodeContent() {
        String serverUrl = getServerUrlWithoutFinalSlash();
        if (serverUrl.isEmpty()) {
            setError("Server URL empty");
            return null;
        }
        if (mappedItemId == null) {
            setError("No item selected for mapping");
            return null;
        }
        if (fromMillis == null || toMillis == null) {
            setError("from/to not set");
            return null;
        }
        
        String url = serverUrl + apkPath
                + "?" + RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "=" + leaderboard
                + "&" + RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME + "=" + raceColumn
                + "&" + RaceLogServletConstants.PARAMS_RACE_FLEET_NAME + "=" + fleet
                + "&" + mappedItemType + "=" + mappedItemId
                + "&" + DeviceMappingConstants.FROM_MILLIS + "=" + fromMillis
                + "&" + DeviceMappingConstants.TO_MILLIS + "=" + toMillis;
        return url;
    }    
}
