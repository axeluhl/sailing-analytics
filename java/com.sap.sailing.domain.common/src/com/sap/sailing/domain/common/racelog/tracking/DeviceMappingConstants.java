package com.sap.sailing.domain.common.racelog.tracking;


/**
 * Shared between GWT and Android. Used for creating and deciphering the URL encoded by the QRCode, which gives the
 * tracking app all necessary information for creating the device mapping of either the race or leaderboard/regatta.
 * <p>
 * The field that server for mapping a device for an individual race are deprecated (see {@link DeviceMappingOnRaceQRCodeWidget}).
 * <p>
 * The structure of the URL is documented in the <a href="http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft">Wiki</a>.
 * {@code http://<host>/tracking/checkin?event_id=<e>&leaderboard_name=<l>&competitor_id=<c>}
 * 
 * @author Fredrik Teschke
 */
public interface DeviceMappingConstants {
    static final String URL_BASE = "/tracking/checkin";
    static final String EVENT_ID = "event_id";
    static final String LEADERBOARD_NAME = "leaderboardName";
    static final String COMPETITOR_ID_AS_STRING = "competitorId";
    static final String MARK_ID_AS_STRING = "markId";
    static final String DEVICE_UUID = "deviceUuid";
    static final String DEVICE_TYPE = "deviceType";
    static final String PUSH_DEVICE_ID = "pushDeviceId";
    static final String FROM_MILLIS = "fromMillis";
    static final String TO_MILLIS = "toMillis";
    @Deprecated
    static final String APK_PATH = "/apps/com.sap.sailing.android.tracking.app.apk";
}
