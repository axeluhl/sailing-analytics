package com.sap.sailing.domain.common.racelog.tracking;

/**
 * Shared between GWT and Android. Used for creating and deciphering the URL encoded by the QRCode, which gives the
 * tracking app all necessary information for creating the device mapping of either the race or leaderboard/regatta.
 * <p>
 * The field that server for mapping a device for an individual race are deprecated (see
 * {@link DeviceMappingOnRaceQRCodeWidget}).
 * <p>
 * The structure of the URL is documented in the <a
 * href="http://wiki.sapsailing.com/wiki/tracking-app-api-v1-draft">Wiki</a>.
 * {@code http://<host>/tracking/checkin?event_id=<e>&leaderboard_name=<l>&competitor_id=<c>}
 * 
 * @author Fredrik Teschke
 */
public interface DeviceMappingConstants {
    // According to the HTTP protocol definition (http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.2.3), only
    // schema and host are case insensitive.
    // But to adhere to best practices, and due to the deficiencies of some web servers and clients,
    // all URL components should be treated as case insensitive, preferring _underscores_ to CamelCase.
    static final String URL_BASE = "/tracking/checkin";
    static final String URL_EVENT_ID = "event_id";
    static final String URL_LEADERBOARD_NAME = "leaderboard_name";
    static final String URL_COMPETITOR_ID_AS_STRING = "competitor_id";
    static final String URL_MARK_ID_AS_STRING = "mark_id";
    static final String URL_FROM_MILLIS = "from_millis";
    static final String URL_TO_MILLIS = "to_millis";

    static final String JSON_COMPETITOR_ID_AS_STRING = "competitorId";
    static final String JSON_MARK_ID_AS_STRING = "markId";
    static final String JSON_DEVICE_UUID = "deviceUuid";
    static final String JSON_DEVICE_TYPE = "deviceType";
    static final String JSON_PUSH_DEVICE_ID = "pushDeviceId";
    static final String JSON_FROM_MILLIS = "fromMillis";
    static final String JSON_TO_MILLIS = "toMillis";
    static final String JSON_TEAM_IMAGE_URI = "teamImageUri";

    @Deprecated
    static final String APK_PATH = "/apps/com.sap.sailing.android.tracking.app.apk";
}
