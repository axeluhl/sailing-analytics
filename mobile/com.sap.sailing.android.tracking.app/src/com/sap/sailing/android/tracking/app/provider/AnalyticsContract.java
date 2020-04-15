package com.sap.sailing.android.tracking.app.provider;

import com.sap.sailing.android.tracking.app.provider.AnalyticsDatabase.Tables;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class AnalyticsContract {

    final static String VND_STRING = "/vnd.sap.sailing_analytics";

    interface CompetitorColumns {
        String COMPETITOR_ID = "competitor_id";
        String COMPETITOR_DISPLAY_NAME = "competitor_display_name";
        String COMPETITOR_COUNTRY_CODE = "competitor_country_code";
        String COMPETITOR_NATIONALITY = "competitor_nationality";
        String COMPETITOR_SAIL_ID = "competitor_sail_id";
        String COMPETITOR_CHECKIN_DIGEST = "competitor_checkin_digest";
    }

    interface LeaderboardColumns {
        String LEADERBOARD_NAME = "leaderboard_name";
        String LEADERBOARD_DISPLAY_NAME = "leaderboard_display_name";
        String LEADERBOARD_CHECKIN_DIGEST = "leaderboard_checkin_digest";
    }

    interface EventColumns {
        String EVENT_ID = "event_id";
        String EVENT_DATE_END = "date_end";
        String EVENT_DATE_START = "date_start";
        String EVENT_SERVER = "event_server";
        String EVENT_IMAGE_URL = "image_url";
        String EVENT_NAME = "event_name";
        String EVENT_CHECKIN_DIGEST = "event_checkin_digest";
    }

    interface MarkColumns {
        String MARK_ID = "mark_id";
        String MARK_NAME = "mark_name";
        String MARK_CHECKIN_DIGEST = "mark_checkin_digest";
    }

    interface BoatColumns {
        String BOAT_ID = "boat_id";
        String BOAT_NAME = "boat_name";
        String BOAT_CHECKIN_DIGEST = "boat_checkin_digest";
        String BOAT_COLOR = "boat_color";
    }

    interface CheckinColumns {
        String CHECKIN_URI_VALUE = "uri_value";
        String CHECKIN_URI_CHECKIN_DIGEST = "uri_checkin_digest";
        String CHECKIN_TYPE = "checkin_type";
    }

    public static final String CONTENT_AUTHORITY = "com.sap.sailing.android.tracking.app.provider.db";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static class LeaderboardsEventsCompetitorsMarksBoatsJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(AnalyticsProvider.ALL_JOINED)
                .build();
    }

    public static class EventLeaderboardCompetitorJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath("event_leaderboard_competitor_joined").build();
    }

    public static class EventLeaderboardMarkJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("event_leaderboard_mark_joined")
                .build();
    }

    public static class Competitor implements CompetitorColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.COMPETITORS).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND_STRING + ".competitor";
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND_STRING + ".competitor";
    }

    public static class Event implements EventColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.EVENTS).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND_STRING + ".event";
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND_STRING + ".event";
    }

    public static class Leaderboard implements LeaderboardColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.LEADERBOARDS).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND_STRING + ".leaderboard";
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND_STRING + ".leaderboard";
    }

    public static class Checkin implements CheckinColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.CHECKIN_URIS).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND_STRING + ".uri";
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND_STRING + ".uri";
    }

    public static class Mark implements MarkColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.MARKS).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND_STRING + ".mark";
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND_STRING + ".mark";
    }

    public static class Boat implements BoatColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.BOATS).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + VND_STRING + ".boat";
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + VND_STRING + ".boat";
    }

    private AnalyticsContract() {

    }
}
