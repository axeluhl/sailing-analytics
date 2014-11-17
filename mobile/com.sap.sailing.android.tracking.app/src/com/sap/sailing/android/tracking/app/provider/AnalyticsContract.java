package com.sap.sailing.android.tracking.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class AnalyticsContract {

	interface CompetitorColumns {
		String COMPETITOR_ID = "id";
		String COMPETITOR_DISPLAY_NAME = "display_name";
		String COMPETITOR_COUNTRY_CODE = "country_code";
		String COMPETITOR_NATIONALITY = "nationality";
		String COMPETITOR_SAIL_ID = "saild_id";
		String COMPETITOR_LEADERBOARD_FK = "leaderboard_id";
	}

	interface LeaderboardColumns {
		String LEADERBOARD_NAME = "name";
	}

	interface EventColumns {
		String EVENT_ID = "id";
		String EVENT_DATE_END = "date_end";
		String EVENT_DATE_START = "date_start";
		String EVENT_SERVER = "server";
		String EVENT_IMAGE_URL = "image_url";
		String EVENT_NAME = "name";
		String EVENT_LEADERBOARD_FK = "leaderboard_id";
	}

	interface SensorGpsColumns {
		String GPS_ACCURACY = "gps_accuracy";
		String GPS_ALTITUDE = "gps_altitude";
		String GPS_BEARING = "gps_bearing";
		String GPS_DEVICE = "gps_device";
		String GPS_ELAPSED_REALTIME = "gps_elapsedrealtime";
		String GPS_LATITUDE = "gps_latitude";
		String GPS_LONGITUDE = "gps_longitude";
		String GPS_PROVIDER = "gps_provider";
		String GPS_SYNCED = "gps_synced";
		String GPS_SPEED = "gps_speed";
		String GPS_TIME = "gps_time";
		String GPS_EVENT_FK = "event_id";
	}

	public static final String CONTENT_AUTHORITY = "com.sap.sailing.android.tracking.app.provider.db";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	private static final String PATH_COMPETITOR = "competitors";
	private static final String PATH_EVENT = "events";
	private static final String PATH_LEADERBOARD = "leaderboards";
	private static final String PATH_SENSOR_GPS = "sensor_gps";

	public static class CheckEventLeaderboardCompetitorExists {
		public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath("check_event_leaderboard_competitor_exists")
				.build();
	}

	public static class Competitor implements CompetitorColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_COMPETITOR).build();

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.competitor";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.competitor";
		public static final String DEFAULT_SORT = CompetitorColumns.COMPETITOR_ID
				+ " COLLATE NOCASE ASC";

		public static Uri buildCompetitorUri(String competitorId) {
			return CONTENT_URI.buildUpon().appendPath(competitorId).build();
		}

		public static Uri buildEventsDirUri(String competitorId) {
			return CONTENT_URI.buildUpon().appendPath(competitorId)
					.appendPath(PATH_EVENT).build();
		}

		public static String getCompetitorId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	public static class Event implements EventColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_EVENT).build();

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.event";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.event";
		public static final String DEFAULT_SORT = EventColumns.EVENT_NAME
				+ " COLLATE NOCASE ASC ";

		public static Uri buildEventUri(String eventId) {
			return CONTENT_URI.buildUpon().appendPath(eventId).build();
		}

		public static Uri buildCompetitorsDirUri(String evevntId) {
			return CONTENT_URI.buildUpon().appendPath(evevntId)
					.appendPath(PATH_COMPETITOR).build();
		}

		public static String getEventId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	public static class SensorGps implements SensorGpsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_SENSOR_GPS).build();

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.sensor.gps";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.sensor.gps";
		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";

		public static Uri buildSensorGpsUri(String gpsId) {
			return CONTENT_URI.buildUpon().appendPath(gpsId).build();
		}

		public static String getGpsId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	public static class Leaderboard implements LeaderboardColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_LEADERBOARD).build();

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.leaderboard";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.sap_sailing_analytics.leaderboard";
		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";

		public static Uri buildLeaderboardUri(String leaderboardId) {
			return CONTENT_URI.buildUpon().appendPath(leaderboardId).build();
		}

		public static String getLeaderboardId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	private AnalyticsContract() {

	}
}
