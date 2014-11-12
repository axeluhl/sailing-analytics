package com.sap.sailing.android.tracking.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class AnalyticsContract {

    interface CompetitorColumns {
        String COMPETITOR_ID = "competitor_id";
        String COMPETITOR_NAME = "competitor_name";
        String COMPETITOR_PROFILE_IMAGE_URL = "profile_image_url";
    }
    
    interface EventColumns {
        String EVENT_ID = "event_id";
        String EVENT_DATE_END = "event_date_end";
        String EVENT_DATE_START = "event_date_start";
        String EVENT_SERVER = "event_server";
        String EVENT_TITLE = "event_title";
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
    }
    
    interface MessageColumns {
        String MESSAGE_URL = "message_url";
        String MESSAGE_CALLBACK_PAYLOAD = "message_callback_payload";
        String MESSAGE_PAYLOAD = "message_payload";
        String MESSAGE_CALLBACK_CLASS_STRING = "message_callback_class_string";
    }
    
    public static final String CONTENT_AUTHORITY = "com.sap.sailing.android.tracking.app.provider.db";
    
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
    private static final String PATH_COMPETITOR = "competitors";
    private static final String PATH_EVENT = "events";
    private static final String PATH_MESSAGE = "messages";
    private static final String PATH_SENSOR_GPS = "sensor_gps";
    
    public static class Competitor implements CompetitorColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMPETITOR).build();
        
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap_sailing_analytics.competitor";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.sap_sailing_analytics.competitor";
        
        public static final String DEFAULT_SORT = CompetitorColumns.COMPETITOR_NAME + " COLLATE NOCASE ASC";
        
        public static Uri buildCompetitorUri(String competitorId) {
            return CONTENT_URI.buildUpon().appendPath(competitorId).build();
        }
        
        public static Uri buildEventsDirUri(String competitorId) {
            return CONTENT_URI.buildUpon().appendPath(competitorId).appendPath(PATH_EVENT).build();
        }
        
        public static String getCompetitorId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
    
    public static class Event implements EventColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();
        
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap_sailing_analytics.event";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.sap_sailing_analytics.event";
        
        public static final String DEFAULT_SORT = EventColumns.EVENT_TITLE + " COLLATE NOCASE ASC ";
        
        public static Uri buildEventUri(String eventId) {
            return CONTENT_URI.buildUpon().appendPath(eventId).build();
        }
        
        public static Uri buildCompetitorsDirUri(String evevntId) {
            return CONTENT_URI.buildUpon().appendPath(evevntId).appendPath(PATH_COMPETITOR).build();
        }
        
        public static String getEventId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
    
    public static class EventCompetitor implements EventColumns, CompetitorColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).appendPath(PATH_COMPETITOR).build();
        
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap_sailing_analytics.event.competitor";
    }
    
    public static class Message implements MessageColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGE).build();
        
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap_sailing_analytics.message";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.sap_sailing_analytics.message";
        
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";
        
        public static Uri buildMessageUri(String messageId) {
            return CONTENT_URI.buildUpon().appendPath(messageId).build();
        }
        
        public static String getMessageId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
   
    public static class SensorGps implements SensorGpsColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SENSOR_GPS).build();
        
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap_sailing_analytics.sensor.gps";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.sap_sailing_analytics.sensor.gps";
        
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";
        
        public static Uri buildSensorGpsUri(String gpsId) {
            return CONTENT_URI.buildUpon().appendPath(gpsId).build();
        }
        
        public static String getGpsId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
    
    private AnalyticsContract() {
        
    }
}
