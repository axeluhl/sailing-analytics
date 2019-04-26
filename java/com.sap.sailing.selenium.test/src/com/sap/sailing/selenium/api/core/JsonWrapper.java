package com.sap.sailing.selenium.api.core;

import java.lang.reflect.InvocationTargetException;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Wrapper for {@link JSONObject} or {@link JSONArray} to wrap results of API requests.
 *
 */
public class JsonWrapper {

    private final JSONObject json;

    /**
     * Create {@link JsonWrapper} by passing a {@link JSONObject}.
     * 
     * @param json
     *            JSON source
     */
    public JsonWrapper(JSONObject json) {
        this.json = json;
    }

    /**
     * Get JSON source.
     * 
     * @return JSON source
     */
    public JSONObject getJson() {
        return json;
    }

    /**
     * Check id source json contains no data.
     * 
     * @return true if empty
     */
    public boolean isEmpty() {
        return json.isEmpty();
    }

    /**
     * Get the value to the corresponding key.
     * 
     * @param key
     *            JSON attribute name
     * @return value for JSON key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) json.get(key);
    }

    /**
     * Get a typed array of the JSON Value for the corresponding key. Returns null if JSON value is not an array.
     * 
     * @param key
     *            JSON attribute name
     * @param type
     *            array type to produce
     * @return typed array containg the elements of the {@link JSONArray}
     */
    public <T extends JsonWrapper> T[] getArray(String key, Class<T> type) {
        Object object = json.get(key);
        if (object instanceof JSONArray) {
            JSONArray jsonArray = ((JSONArray) object);
            JSONAware[] objectArray = (JSONAware[]) jsonArray.toArray();
            @SuppressWarnings("unchecked")
            T[] result = (T[]) new Object[objectArray.length];
            for (int i = 0; i < objectArray.length; i++) {
                if (objectArray[i] instanceof JSONObject) {
                    try {
                        result[i] = type.getDeclaredConstructor(JSONObject.class).newInstance(objectArray[i]);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public String toString() {
        return json.toJSONString();
    }

}
