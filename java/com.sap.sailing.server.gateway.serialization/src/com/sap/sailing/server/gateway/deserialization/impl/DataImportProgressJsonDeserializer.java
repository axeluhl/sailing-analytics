package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.server.gateway.serialization.impl.DataImportProgressJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class DataImportProgressJsonDeserializer implements JsonDeserializer<DataImportProgress> {
    @Override
    public DataImportProgress deserialize(JSONObject object) throws JsonDeserializationException {
        // TODO Implement JsonDeserializer<DataImportProgress>.deserialize(...)
        final DataImportProgress result = new DataImportProgressImpl(UUID.fromString(object.get(DataImportProgressJsonSerializer.OPERATION_ID).toString()));
        result.setOverAllProgressPct(((Number) object.get(DataImportProgressJsonSerializer.OVERALL_PROGRESS_PERCENT)).doubleValue());
        result.setCurrentSubProgress(DataImportSubProgress.valueOf(object.get(DataImportProgressJsonSerializer.CURRENT_SUB_PROGRESS).toString()));
        result.setCurrentSubProgressPct(((Number) object.get(DataImportProgressJsonSerializer.CURRENT_SUB_PROGRESS_PERCENT)).doubleValue());
        if ((Boolean) object.get(DataImportProgressJsonSerializer.FAILED)) {
            result.setFailed();
        }
        if (object.get(DataImportProgressJsonSerializer.ERROR_MESSAGE) != null) {
            result.setErrorMessage(object.get(DataImportProgressJsonSerializer.ERROR_MESSAGE).toString());
        }
        final JSONObject objectCountsJson = (JSONObject) object.get(DataImportProgressJsonSerializer.RESULT);
        if (objectCountsJson != null) {
            final MasterDataImportObjectCreationCount objectCounts = new MasterDataImportObjectCreationCount() {
                private static final long serialVersionUID = 4004959619812197098L;

                @Override
                public int getLeaderboardCount() {
                    return ((Number) objectCountsJson.get(DataImportProgressJsonSerializer.LEADERBOARD_COUNT)).intValue();
                }
    
                @Override
                public int getLeaderboardGroupCount() {
                    return ((Number) objectCountsJson.get(DataImportProgressJsonSerializer.LEADERBOARD_GROUP_COUNT)).intValue();
                }
    
                @Override
                public int getEventCount() {
                    return ((Number) objectCountsJson.get(DataImportProgressJsonSerializer.EVENT_COUNT)).intValue();
                }
    
                @Override
                public int getRegattaCount() {
                    return ((Number) objectCountsJson.get(DataImportProgressJsonSerializer.REGATTA_COUNT)).intValue();
                }
    
                @Override
                public int getMediaTrackCount() {
                    return ((Number) objectCountsJson.get(DataImportProgressJsonSerializer.MEDIA_TRACK_COUNT)).intValue();
                }
    
                @Override
                public Iterable<String> getOverwrittenRegattaNames() {
                    return Util.map(((JSONArray) objectCountsJson.get(DataImportProgressJsonSerializer.OVERWRITTEN_REGATTA_NAMES)), o->o.toString());
                }
    
                @Override
                public int getTrackedRacesCount() {
                    return ((Number) objectCountsJson.get(DataImportProgressJsonSerializer.TRACKED_RACES_COUNT)).intValue();
                }
            };
            result.setResult(objectCounts);
        }
        return result;
    }
}
