package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;

public class SeriesDataJsonSerializer extends ExtendableJsonSerializer<SeriesData> {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_IS_MEDAL = "isMedal";
	
	public SeriesDataJsonSerializer(ExtensionJsonSerializer<SeriesData, ?> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	protected JSONObject serializeFields(SeriesData object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_IS_MEDAL, object.isMedal());
		
		return result;
	}

}
