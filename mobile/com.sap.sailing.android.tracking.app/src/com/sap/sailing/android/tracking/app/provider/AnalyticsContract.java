package com.sap.sailing.android.tracking.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sap.sailing.android.tracking.app.provider.AnalyticsDatabase.Tables;

public class AnalyticsContract {

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

    interface markColums {
        String MARK_ID = "mark_id";
        String MARK_NAME = "mark_name";
        String MARK_CHECKIN_DIGEST = "mark_checkin_digest";
    }

    interface CheckinColumns {
        String CHECKIN_URI_VALUE = "uri_value";
        String CHECKIN_URI_CHECKIN_DIGEST = "uri_checkin_digest";
        String CHECKIN_TYPE = "checkin_type";
    }

    public static final String CONTENT_AUTHORITY = "com.sap.sailing.android.tracking.app.provider.db";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static class LeaderboardsEventsCompetitorsMarksJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("leaderboards_events_competitors_marks_joined")
                .build();
    }

    public static class EventLeaderboardCompetitorJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath("event_leaderboard_competitor_joined").build();
    }

    public static class EventLeaderboardMarkJoined {
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
            .appendPath("event_leaderboard_mark_joined").build();
    }

    public static class Competitor implements CompetitorColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.COMPETITORS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics.competitor";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics.competitor";
    }

    public static class Event implements EventColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.EVENTS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics.event";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics.event";
    }

    public static class Leaderboard implements LeaderboardColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.LEADERBOARDS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics.leaderboard";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics.leaderboard";
    }

    public static class Checkin implements CheckinColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.CHECKIN_URIS).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/vnd.sap_sailing_analytics.uri";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/vnd.sap_sailing_analytics.uri";
    }

    public static class Mark implements markColums, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Tables.MARKS).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/vnd.sap_sailing_analytics.Mark";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/vnd.sap_sailing_analytics.Mark";
    }

    private AnalyticsContract() {

    }
}
