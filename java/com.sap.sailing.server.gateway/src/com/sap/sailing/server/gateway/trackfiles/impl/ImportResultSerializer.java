package com.sap.sailing.server.gateway.trackfiles.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.ErrorImportDTO;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.TrackImportDTO;
import com.sap.sse.common.Util.Triple;

/**
 * Utility class providing convenience methods to serialize import result objects into JSON objects.
 */
class ImportResultSerializer {

    static JSONObject serializeImportResult(ImportResultDTO result) {
        final JSONObject json = new JSONObject();
        json.put("errors", serializeErrorList(result.getErrorList()));
        json.put("uploads", serializeTrackList(result.getImportResult()));
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
        json.put("raceName", entry.getA());
        json.put("raceColumnName", entry.getB());
        json.put("fleetName", entry.getC());
        return json;
    }

    private static JSONObject serializeError(ErrorImportDTO error) {
        final JSONObject json = new JSONObject();
        json.put("filename", error.getFilename());
        json.put("requestedImporter", error.getRequestedImporter());
        json.put("exUUID", error.getExUUID());
        json.put("className", error.getName());
        json.put("message", error.getMessage());
        return json;
    }


}