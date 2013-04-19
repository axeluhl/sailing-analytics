package com.sap.sailing.gwt.ui.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * Interface for a GWT specific serializer to Json.
 * 
 * @param <T>
 *            Object class to serialize.
 */
public interface GwtJsonDeSerializer<T> {
    /**
     * Serializes given object to a Json object.
     * 
     * @param object
     *            to be serialized.
     * @return serialized Json object.
     */
    JSONObject serialize(T object);
    
    T deserialize(JSONValue object);
}