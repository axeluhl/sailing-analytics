package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONObject;


public interface JsonDeserializer<T> {
	
	T deserialize(JSONObject object) throws JsonDeserializationException;

}
