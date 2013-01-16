package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Helpers {
	
	public static JSONObject toJSONObjectSafe(Object object)
			throws JsonDeserializationException {
		if (object instanceof JSONObject) {
			return (JSONObject) object;
		}
		throw new JsonDeserializationException(
				String.format("Expected a JSONObject, got %s.", object.getClass().getName()));
	}
	
	public static JSONObject getNestedObjectSafe(JSONObject parent, String fieldName) 
			throws JsonDeserializationException {
		Object childObject = parent.get(fieldName);
		if (!(childObject instanceof JSONObject)) {
			throw new JsonDeserializationException(
					String.format("Field %s with %s wasn't a nested JSON object.", 
							fieldName, childObject.toString()));
		}
		return (JSONObject) childObject;
	}
	
	public static JSONArray getNestedArraySafe(JSONObject parent, String fieldName) 
			throws JsonDeserializationException {
		Object childObject = parent.get(fieldName);
		if (!(childObject instanceof JSONArray)) {
			throw new JsonDeserializationException(
					String.format("Field %s with %s wasn't a nested JSON array.", 
							fieldName, childObject.toString()));
		}
		return (JSONArray) childObject;
	}

}
