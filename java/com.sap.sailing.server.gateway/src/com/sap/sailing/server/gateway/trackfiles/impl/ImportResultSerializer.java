package com.sap.sailing.server.gateway.trackfiles.impl;

import java.util.List;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.ErrorImportDTO;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.TrackImportDTO;

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

    static <R> JSONArray serializeList(List<? extends R> list, Function<? super R, ?> mapping) {
        final JSONArray json = new JSONArray();
        list.stream().map(mapping).forEach(json::add);
        return json;
    }

    static JSONArray serializeTrackList(List<TrackImportDTO> trackList) {
        return serializeList(trackList, track -> track.getDevice().toString());
    }

    static JSONArray serializeErrorList(List<ErrorImportDTO> errorList) {
        return serializeList(errorList, ImportResultSerializer::serializeError);
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