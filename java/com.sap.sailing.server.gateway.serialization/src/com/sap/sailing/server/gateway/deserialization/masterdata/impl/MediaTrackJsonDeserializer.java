package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.Date;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.MediaTrackJsonSerializer;

public class MediaTrackJsonDeserializer implements JsonDeserializer<MediaTrack> {

    @Override
    public MediaTrack deserialize(JSONObject object) throws JsonDeserializationException {
        String dbId = (String) object.get(MediaTrackJsonSerializer.FIELD_DB_ID);
        int durationInMillis = ((Long) object.get(MediaTrackJsonSerializer.FIELD_DURATION)).intValue();
        MediaTrack.MimeType mimeType = MediaTrack.MimeType.valueOf((String) object
                .get(MediaTrackJsonSerializer.FIELD_MIME_TYPE));
        long startTimeInMillis = (Long) object.get(MediaTrackJsonSerializer.FIELD_START_TIME);
        Date startTime = new Date(startTimeInMillis);
        MediaTrack.Status status = MediaTrack.Status
                .valueOf((String) object.get(MediaTrackJsonSerializer.FIELD_STATUS));
        String title = (String) object.get(MediaTrackJsonSerializer.FIELD_TITLE);
        String url = (String) object.get(MediaTrackJsonSerializer.FIELD_URL);
        MediaTrack mediaTrack = new MediaTrack(dbId, title, url, startTime, durationInMillis, mimeType);
        mediaTrack.status = status;
        return mediaTrack;
    }

}
