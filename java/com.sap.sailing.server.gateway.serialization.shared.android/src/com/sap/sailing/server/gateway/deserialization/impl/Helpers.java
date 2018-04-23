package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;

public class Helpers {
    private final static Logger logger = Logger.getLogger(Helpers.class.getName());

    public static JSONArray toJSONArraySafe(Object object) throws JsonDeserializationException {
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw new JsonDeserializationException(String.format("Expected a JSONArray, got %s.", object != null ? object.getClass()
                .getName() : ""));
    }

    public static JSONObject toJSONObjectSafe(Object object) throws JsonDeserializationException {
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JsonDeserializationException(String.format("Expected a JSONObject, got %s.", object.getClass()
                .getName()));
    }

    public static JSONObject getNestedObjectSafe(JSONObject parent, String fieldName)
            throws JsonDeserializationException {
        Object childObject = parent.get(fieldName);
        if (!(childObject instanceof JSONObject)) {
            throw new JsonDeserializationException(String.format("Field %s with %s wasn't a nested JSON object.",
                    fieldName, childObject.toString()));
        }
        return (JSONObject) childObject;
    }

    public static JSONArray getNestedArraySafe(JSONObject parent, String fieldName) throws JsonDeserializationException {
        Object childObject = parent.get(fieldName);
        if (!(childObject instanceof JSONArray)) {
            throw new JsonDeserializationException(String.format("Field %s with %s wasn't a nested JSON array.",
                    fieldName, childObject.toString()));
        }
        return (JSONArray) childObject;
    }

    public static URL getURLField(JSONObject parent, String fieldName) {
        URL result = null;
        String urlAsString = (String) parent.get(fieldName);
        if (urlAsString != null) {
            try {
                result = new URL(urlAsString);
            } catch (MalformedURLException e) {
                logger.severe("Error deserializing URL " + urlAsString);
            }
        }
        return result;
    }
}
