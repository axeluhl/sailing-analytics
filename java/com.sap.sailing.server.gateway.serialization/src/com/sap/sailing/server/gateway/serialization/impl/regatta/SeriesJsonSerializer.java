package com.sap.sailing.server.gateway.serialization.impl.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Series;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;

public class SeriesJsonSerializer extends ExtendableJsonSerializer<Series> {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_IS_MEDAL = "isMedal";
	
	public SeriesJsonSerializer(ExtensionJsonSerializer<Series, ?> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	protected JSONObject serializeFields(Series object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_IS_MEDAL, object.isMedal());
		
		return result;
	}

}
