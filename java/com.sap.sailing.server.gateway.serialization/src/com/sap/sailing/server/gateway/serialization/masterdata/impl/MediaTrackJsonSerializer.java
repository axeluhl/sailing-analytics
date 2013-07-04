package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MediaTrackJsonSerializer implements JsonSerializer<MediaTrack> {

    public static final String FIELD_DB_ID = "dbId";
    public static final String FIELD_DURATION = "duration";
    public static final String FIELD_MIME_TYPE = "mimeType";
    public static final String FIELD_START_TIME = "startTime";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_URL = "url";

    @Override
    public JSONObject serialize(MediaTrack mediaTrack) {
        JSONObject result = new JSONObject();
        result.put(FIELD_DB_ID, mediaTrack.dbId);
        result.put(FIELD_DURATION, mediaTrack.durationInMillis);
        result.put(FIELD_MIME_TYPE, mediaTrack.mimeType.name());
        result.put(FIELD_START_TIME, mediaTrack.startTime.getTime());
        result.put(FIELD_STATUS, mediaTrack.status.name());
        result.put(FIELD_TITLE, mediaTrack.title);
        result.put(FIELD_URL, mediaTrack.url);
        return result;
    }

}
