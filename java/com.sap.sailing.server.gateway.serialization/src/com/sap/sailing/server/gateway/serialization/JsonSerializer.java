package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

public interface JsonSerializer<T>
{
	JSONObject serialize(T object);
}
