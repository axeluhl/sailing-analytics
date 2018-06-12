package com.sap.sailing.server.gateway.trackfiles.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.dto.ImportConstants;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResult.ErrorImportDTO;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResult.TrackImportDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Utility class providing convenience methods to serialize import result objects into JSON objects.
 */
class ImportResultSerializer {

    static JSONObject serializeImportResult(ImportResult result) {
        final JSONObject json = new JSONObject();
        json.put(ImportConstants.ERRORS, serializeErrorList(result.getErrorList()));
        json.put(ImportConstants.UPLOADS, serializeTrackList(result.getImportResult()));
        return json;
    }

    static <R> JSONArray serializeIterable(Iterable<? extends R> list, Function<? super R, ?> mapping) {
        final JSONArray json = new JSONArray();
        StreamSupport.stream(list.spliterator(), /* parallel */ false).map(mapping).forEach(json::add);
        return json;
    }

    static JSONArray serializeTrackList(List<TrackImportDTO> trackList) {
        return serializeIterable(trackList, track -> track.getDevice().toString());
    }

    static JSONArray serializeErrorList(List<ErrorImportDTO> errorList) {
        return serializeIterable(errorList, ImportResultSerializer::serializeError);
    }
    
    public static JSONArray serializeRaceList(List<Triple<String, String, String>> raceNameRaceColumnNameFleetnameList) {
        return serializeIterable(raceNameRaceColumnNameFleetnameList, ImportResultSerializer::serializeRaceEntry);
    }

    private static JSONObject serializeRaceEntry(Triple<String, String, String> entry) {
        final JSONObject json = new JSONObject();
        json.put(ImportConstants.RACE_NAME, entry.getA());
        json.put(ImportConstants.RACE_COLUMN_NAME, entry.getB());
        json.put(ImportConstants.FLEET_NAME, entry.getC());
        return json;
    }

    private static JSONObject serializeError(ErrorImportDTO error) {
        final JSONObject json = new JSONObject();
        json.put(ImportConstants.FILENAME, error.getFilename());
        json.put(ImportConstants.REQUESTED_IMPORTER, error.getRequestedImporter());
        json.put(ImportConstants.EX_UUID, error.getExUUID());
        json.put(ImportConstants.CLASS_NAME, error.getName());
        json.put(ImportConstants.MESSAGE, error.getMessage());
        return json;
    }


}